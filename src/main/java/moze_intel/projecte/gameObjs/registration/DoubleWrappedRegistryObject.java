package moze_intel.projecte.gameObjs.registration;

import moze_intel.projecte.gameObjs.registration.DeferredHolder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class DoubleWrappedRegistryObject<PRIMARY_REGISTRY, PRIMARY extends PRIMARY_REGISTRY, SECONDARY_REGISTRY, SECONDARY extends SECONDARY_REGISTRY> implements INamedEntry {

	protected final DeferredHolder<PRIMARY_REGISTRY, PRIMARY> primaryRO;
	protected final DeferredHolder<SECONDARY_REGISTRY, SECONDARY> secondaryRO;

	public DoubleWrappedRegistryObject(DeferredHolder<PRIMARY_REGISTRY, PRIMARY> primaryRO, DeferredHolder<SECONDARY_REGISTRY, SECONDARY> secondaryRO) {
		this.primaryRO = primaryRO;
		this.secondaryRO = secondaryRO;
	}

	@NotNull
	public PRIMARY getPrimary() {
		return primaryRO.get();
	}

	@NotNull
	public SECONDARY getSecondary() {
		return secondaryRO.get();
	}

	@Override
	public ResourceLocation getId() {
		return primaryRO.getId();
	}

	@Override
	public String getName() {
		return primaryRO.getId().getPath();
	}
}