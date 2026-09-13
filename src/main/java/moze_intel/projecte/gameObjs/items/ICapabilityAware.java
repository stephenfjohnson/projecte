package moze_intel.projecte.gameObjs.items;

/**
 * Implemented by items that expose capabilities beyond the ones {@code ItemDeferredRegister} infers from the
 * interfaces an item implements.
 * <p>
 * NeoForge gathered these through a registration event; on Fabric each item registers itself directly against
 * the relevant lookups, so this is called once during mod initialisation.
 */
@FunctionalInterface
public interface ICapabilityAware {

	void attachCapabilities();
}
