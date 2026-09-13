package moze_intel.projecte.capability;

import moze_intel.projecte.api.inventory.IItemHandler;
import moze_intel.projecte.api.inventory.IItemHandlerModifiable;
import moze_intel.projecte.inventory.ItemHandlerStorage;
import moze_intel.projecte.inventory.StorageItemHandler;
import moze_intel.projecte.inventory.wrapper.InvWrapper;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * The general-purpose capabilities NeoForge provided for everyone, restated as ProjectE-owned Fabric lookups.
 * <p>
 * These are ProjectE's own lookups rather than Fabric's storage API directly, because the mod is written against
 * a slot-indexed inventory view. {@link #init()} bridges them to Fabric's storage API in both directions, so
 * ProjectE's tools can read a vanilla chest and other mods can read a ProjectE machine.
 */
public final class Capabilities {

	private Capabilities() {
	}

	private static ResourceLocation rl(String path) {
		return ResourceLocation.fromNamespaceAndPath("projecte", path);
	}

	public static final class ItemHandler {

		private ItemHandler() {
		}

		public static final BlockApiLookup<IItemHandler, Direction> BLOCK =
				BlockApiLookup.get(rl("item_handler"), IItemHandler.class, Direction.class);
		public static final ItemApiLookup<IItemHandler, Void> ITEM =
				ItemApiLookup.get(rl("item_handler"), IItemHandler.class, Void.class);
		public static final EntityApiLookup<IItemHandler, Void> ENTITY =
				EntityApiLookup.get(rl("item_handler"), IItemHandler.class, Void.class);
	}

	/**
	 * Connects ProjectE's inventory lookups to Fabric's storage API. Called once during mod initialisation.
	 */
	public static void init() {
		//Anything that is not a ProjectE block - a vanilla chest, another mod's machine - exposes itself
		// through Fabric's storage API, so fall back to that and wrap the result
		ItemHandler.BLOCK.registerFallback((level, pos, state, blockEntity, direction) -> {
			Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, state, blockEntity, direction);
			return storage == null ? null : new StorageItemHandler(storage);
		});
		//A player's carried items, for the tools that scan or repair an inventory
		ItemHandler.ENTITY.registerFallback((entity, context) ->
				entity instanceof Player player ? new InvWrapper(player.getInventory()) : null);
	}

	/**
	 * Exposes a ProjectE block entity's inventory through Fabric's storage API as well, so that hoppers, pipes
	 * and other mods can move items in and out of it.
	 * <p>
	 * Registering both directions explicitly, rather than having each lookup fall back to the other, is what
	 * keeps the two from calling into each other forever.
	 */
	public static <BE extends BlockEntity> void bridgeItemStorage(BlockEntityType<BE> type,
			ICapabilityProvider<? super BE, Direction, IItemHandler> provider) {
		ItemStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> {
			IItemHandler handler = provider.getCapability(blockEntity, direction);
			//Only a modifiable handler can back a storage, since a transaction has to be able to roll a slot back
			return handler instanceof IItemHandlerModifiable modifiable ? new ItemHandlerStorage(modifiable) : null;
		}, type);
	}
}
