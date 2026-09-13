package moze_intel.projecte.integration;

import moze_intel.projecte.api.inventory.IItemHandler;
import net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class IntegrationHelper {

	/**
	 * Fabric's counterpart to Curios. ProjectE's rings and amulets are meant to work while equipped in an
	 * accessory slot rather than held.
	 */
	public static final String ACCESSORY_MODID = "trinkets";
	public static final String JADE_MODID = "jade";

	/**
	 * Lookup for the inventory of accessories a player is wearing, so that a worn ring or amulet still counts
	 * as carried.
	 * <p>
	 * ProjectE owns this lookup rather than borrowing one from the accessory mod, so the rest of the mod can
	 * ask for worn items without depending on which mod provides them. It has no provider registered until the
	 * Trinkets integration is wired up, so it currently resolves to {@code null} and only the normal inventory
	 * is consulted.
	 */
	public static final EntityApiLookup<IItemHandler, Void> ACCESSORY_ITEM_HANDLER = EntityApiLookup.get(
			ResourceLocation.fromNamespaceAndPath("projecte", "accessory_item_handler"), IItemHandler.class, Void.class);

	public static boolean isLoaded(String modid) {
		return FabricLoader.getInstance().isModLoaded(modid);
	}

	/**
	 * Hook for exposing an item's capabilities to the accessory mod's slots.
	 *
	 * @implNote Not implemented yet: the Trinkets integration is still to be written, so an item equipped in an
	 * accessory slot does not yet act as if it were in the player's inventory.
	 */
	public static void registerAccessoryCapability(Item item) {
		//TODO - Trinkets integration: register this item's capabilities for accessory slots
	}
}
