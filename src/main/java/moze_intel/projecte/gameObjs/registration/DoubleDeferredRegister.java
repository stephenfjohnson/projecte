package moze_intel.projecte.gameObjs.registration;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import moze_intel.projecte.gameObjs.registration.DeferredHolder;
import moze_intel.projecte.gameObjs.registration.DeferredRegister;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;

public class DoubleDeferredRegister<PRIMARY, SECONDARY> {

	@NotNull
	protected final DeferredRegister<PRIMARY> primaryRegister;
	@NotNull
	protected final DeferredRegister<SECONDARY> secondaryRegister;

	public DoubleDeferredRegister(@NotNull DeferredRegister<PRIMARY> primaryRegistry, @NotNull DeferredRegister<SECONDARY> secondaryRegistry) {
		this.primaryRegister = primaryRegistry;
		this.secondaryRegister = secondaryRegistry;
	}

	protected DoubleDeferredRegister(ResourceKey<? extends Registry<PRIMARY>> primaryRegistryName, ResourceKey<? extends Registry<SECONDARY>> secondaryRegistryName,
			String modid) {
		this(primaryRegistryName, PEDeferredRegister.create(secondaryRegistryName, modid), modid);
	}

	protected DoubleDeferredRegister(ResourceKey<? extends Registry<PRIMARY>> primaryRegistryName, DeferredRegister<SECONDARY> secondaryRegistry, String modid) {
		this(PEDeferredRegister.create(primaryRegistryName, modid), secondaryRegistry);
	}

	public <P extends PRIMARY, S extends SECONDARY, W extends DoubleWrappedRegistryObject<PRIMARY, P, SECONDARY, S>> W register(String name,
			Supplier<? extends P> primarySupplier, Supplier<? extends S> secondarySupplier, BiFunction<DeferredHolder<PRIMARY, P>,
			DeferredHolder<SECONDARY, S>, W> objectWrapper) {
		return objectWrapper.apply(primaryRegister.register(name, primarySupplier), secondaryRegister.register(name, secondarySupplier));
	}

	public <P extends PRIMARY, S extends SECONDARY, W extends DoubleWrappedRegistryObject<PRIMARY, P, SECONDARY, S>> W register(String name,
			Supplier<? extends P> primarySupplier, Function<P, S> secondarySupplier, BiFunction<DeferredHolder<PRIMARY, P>,
			DeferredHolder<SECONDARY, S>, W> objectWrapper) {
		return registerAdvanced(name, primarySupplier, secondarySupplier.compose(Supplier::get), objectWrapper);
	}

	public <P extends PRIMARY, S extends SECONDARY, W extends DoubleWrappedRegistryObject<PRIMARY, P, SECONDARY, S>> W registerAdvanced(String name,
			Supplier<? extends P> primarySupplier, Function<DeferredHolder<PRIMARY, P>, S> secondarySupplier, BiFunction<DeferredHolder<PRIMARY, P>,
			DeferredHolder<SECONDARY, S>, W> objectWrapper) {
		DeferredHolder<PRIMARY, P> primaryObject = primaryRegister.register(name, primarySupplier);
		return objectWrapper.apply(primaryObject, secondaryRegister.register(name, () -> secondarySupplier.apply(primaryObject)));
	}

	public void register() {
		primaryRegister.register();
		secondaryRegister.register();
	}

	public Collection<DeferredHolder<PRIMARY, ? extends PRIMARY>> getPrimaryEntries() {
		return primaryRegister.getEntries();
	}

	public Collection<DeferredHolder<SECONDARY, ? extends SECONDARY>> getSecondaryEntries() {
		return secondaryRegister.getEntries();
	}
}