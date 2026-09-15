package moze_intel.projecte.gameObjs.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProjectETNT extends TntBlock {

	/**
	 * How readily fire consumes this block, which ProjectE's TNT is as happy to catch as vanilla's.
	 *
	 * @implNote NeoForge asked the block; on Fabric the odds live in Fabric's flammable block registry, which
	 * {@code PECore} fills in with this once the blocks exist.
	 */
	public static final int BURN_CHANCE = 100;

	private final TNTEntityCreator tntEntityCreator;

	public ProjectETNT(Properties properties, TNTEntityCreator tntEntityCreator) {
		super(properties);
		this.tntEntityCreator = tntEntityCreator;
	}

	/**
	 * Primes this TNT's own entity, whatever set it off.
	 *
	 * @implNote NeoForge routed every ignition path through {@code onCaughtFire}; on Fabric, {@code TntBlockMixin} and
	 * {@code FireBlockMixin} send vanilla's ignition paths here instead of spawning vanilla's primed TNT.
	 */
	public void prime(@NotNull Level level, @NotNull BlockPos pos, @Nullable LivingEntity igniter) {
		if (!level.isClientSide) {
			createAndAddEntity(level, pos, igniter);
			level.gameEvent(igniter, GameEvent.PRIME_FUSE, pos);
		}
	}

	public void createAndAddEntity(@NotNull Level level, @NotNull BlockPos pos, @Nullable LivingEntity igniter) {
		PrimedTnt tnt = tntEntityCreator.create(level, pos.getX() + 0.5F, pos.getY(), pos.getZ() + 0.5F, igniter);
		level.addFreshEntity(tnt);
		level.playSound(null, tnt.getX(), tnt.getY(), tnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	public DispenseItemBehavior createDispenseItemBehavior() {
		//[VanillaCopy] Based off vanilla's TNT behavior
		return new DefaultDispenseItemBehavior() {
			@NotNull
			@Override
			protected ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
				BlockPos blockpos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
				createAndAddEntity(source.level(), blockpos, null);
				source.level().gameEvent(null, GameEvent.ENTITY_PLACE, blockpos);
				stack.shrink(1);
				return stack;
			}
		};
	}

	@Override
	public void wasExploded(Level level, @NotNull BlockPos pos, @NotNull Explosion explosion) {
		if (!level.isClientSide) {
			PrimedTnt tnt = tntEntityCreator.create(level, (float) pos.getX() + 0.5F, pos.getY(), (float) pos.getZ() + 0.5F, explosion.getIndirectSourceEntity());
			int fuse = tnt.getFuse();
			tnt.setFuse((short) (level.random.nextInt(fuse / 4) + fuse / 8));
			level.addFreshEntity(tnt);
		}
	}

	@FunctionalInterface
	public interface TNTEntityCreator {

		PrimedTnt create(Level level, double posX, double posY, double posZ, @Nullable LivingEntity igniter);
	}
}