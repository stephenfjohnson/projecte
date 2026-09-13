package moze_intel.projecte.api.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

/**
 * An amount of a fluid, together with any data components attached to it.
 * <p>
 * Fabric models fluids as a {@code FluidVariant} plus a separate droplet count, where one bucket is
 * {@value #DROPLETS_PER_BUCKET} droplets. ProjectE's EMC values, conversion files and world transmutations are
 * all written in millibuckets, so amounts here stay in millibuckets ({@value #BUCKET_VOLUME} per bucket) and
 * are converted at the point where Fabric's fluid storage is actually touched. Keeping millibuckets means no
 * EMC value in the data files has to be rescaled.
 */
public final class FluidStack {

	/** Millibuckets in one bucket, the unit every ProjectE fluid amount is expressed in. */
	public static final int BUCKET_VOLUME = 1_000;
	/** Droplets in one bucket, the unit Fabric's fluid storage is expressed in. */
	public static final long DROPLETS_PER_BUCKET = 81_000L;

	public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY.builtInRegistryHolder(), 0, DataComponentPatch.EMPTY);

	private final Holder<Fluid> fluid;
	private final int amount;
	private final DataComponentPatch componentsPatch;

	public FluidStack(@NotNull Fluid fluid, int amount) {
		this(fluid.builtInRegistryHolder(), amount, DataComponentPatch.EMPTY);
	}

	public FluidStack(@NotNull Holder<Fluid> fluid, int amount) {
		this(fluid, amount, DataComponentPatch.EMPTY);
	}

	public FluidStack(@NotNull Holder<Fluid> fluid, int amount, @NotNull DataComponentPatch componentsPatch) {
		this.fluid = fluid;
		this.amount = amount;
		this.componentsPatch = componentsPatch;
	}

	@NotNull
	public Fluid getFluid() {
		return fluid.value();
	}

	@NotNull
	public Holder<Fluid> getFluidHolder() {
		return fluid;
	}

	/**
	 * @return This stack's size, in millibuckets.
	 */
	public int getAmount() {
		return amount;
	}

	@NotNull
	public DataComponentPatch getComponentsPatch() {
		return componentsPatch;
	}

	public boolean isEmpty() {
		return amount <= 0 || getFluid() == Fluids.EMPTY;
	}

	public FluidStack withAmount(int amount) {
		return new FluidStack(fluid, amount, componentsPatch);
	}

	/**
	 * @return This stack's size in droplets, the unit Fabric's fluid storage uses.
	 */
	public long getDroplets() {
		return dropletsFromMillibuckets(amount);
	}

	public static long dropletsFromMillibuckets(int millibuckets) {
		return millibuckets * (DROPLETS_PER_BUCKET / BUCKET_VOLUME);
	}

	public static int millibucketsFromDroplets(long droplets) {
		return (int) (droplets / (DROPLETS_PER_BUCKET / BUCKET_VOLUME));
	}

	public static boolean isSameFluidSameComponents(@NotNull FluidStack a, @NotNull FluidStack b) {
		return a.getFluid() == b.getFluid() && a.componentsPatch.equals(b.componentsPatch);
	}

	public int hashFluidAndComponents() {
		return 31 * getFluid().hashCode() + componentsPatch.hashCode();
	}

	/**
	 * A codec that reads and writes only which fluid this is, pinning every decoded stack to a fixed size.
	 * Used where the amount is implied by context rather than stored, such as a bucket's worth.
	 *
	 * @param amount Size, in millibuckets, to give every decoded stack.
	 */
	public static Codec<FluidStack> fixedAmountCodec(int amount) {
		return RecordCodecBuilder.create(instance -> instance.group(
				BuiltInRegistries.FLUID.holderByNameCodec().fieldOf("fluid").forGetter(FluidStack::getFluidHolder),
				DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(FluidStack::getComponentsPatch)
		).apply(instance, (fluid, patch) -> new FluidStack(fluid, amount, patch)));
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof FluidStack other && amount == other.amount && isSameFluidSameComponents(this, other);
	}

	@Override
	public int hashCode() {
		return 31 * hashFluidAndComponents() + amount;
	}

	@Override
	public String toString() {
		return amount + "mB " + BuiltInRegistries.FLUID.getKey(getFluid());
	}
}
