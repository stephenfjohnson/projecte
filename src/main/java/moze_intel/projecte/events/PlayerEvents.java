package moze_intel.projecte.events;

import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.IAlchBagProvider;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.inventory.IItemHandler;
import moze_intel.projecte.gameObjs.items.AlchemicalBag;
import moze_intel.projecte.inventory.ItemHandlerHelper;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.text.PELang;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * ProjectE's reactions to what players do.
 * <p>
 * Where Fabric has an event of its own these are registered in {@link #register()}; the rest are called from
 * mixins, since NeoForge's equivalents have no Fabric counterpart.
 */
public class PlayerEvents {

	private PlayerEvents() {
	}

	public static void register() {
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> syncToClient(newPlayer));
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> playerChangeDimension(player));
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			playerConnect(handler.getPlayer());
			announceHighAlchemist(handler.getPlayer());
		});
	}

	// On death or return from end, sync to the client
	private static void syncToClient(ServerPlayer player) {
		IKnowledgeProvider knowledge = PECapabilities.KNOWLEDGE_CAPABILITY.find(player, null);
		if (knowledge != null) {
			knowledge.sync(player);
		}
		IAlchBagProvider bagProvider = PECapabilities.ALCH_BAG_CAPABILITY.find(player, null);
		if (bagProvider != null) {
			bagProvider.syncAllBags(player);
		}
	}

	private static void playerChangeDimension(ServerPlayer player) {
		// Sync to the client for "normal" interdimensional teleports (nether portal, etc.)
		syncToClient(player);
	}

	private static void playerConnect(ServerPlayer player) {
		IKnowledgeProvider knowledge = PECapabilities.KNOWLEDGE_CAPABILITY.find(player, null);
		if (knowledge != null) {
			knowledge.sync(player);
			PlayerHelper.updateScore(player, PlayerHelper.SCOREBOARD_EMC, knowledge.getEmc());
		}

		IAlchBagProvider alchBagProvider = PECapabilities.ALCH_BAG_CAPABILITY.find(player, null);
		if (alchBagProvider != null) {
			alchBagProvider.syncAllBags(player);
		}

		PECore.debugLog("Sent knowledge and bag data to {}", player.getName());
	}

	private static void announceHighAlchemist(ServerPlayer player) {
		if (PECore.uuids.contains(player.getUUID().toString())) {
			MinecraftServer server = player.getServer();
			if (server != null) {
				Component joinMessage = PELang.HIGH_ALCHEMIST.translateColored(ChatFormatting.BLUE, ChatFormatting.GOLD, player.getDisplayName());
				server.getPlayerList().broadcastSystemMessage(joinMessage, false);
			}
		}
	}

	/**
	 * Offers an item on the ground to an alchemical bag holding a black hole band before the player picks it up.
	 *
	 * @return {@code true} if the bag took some of it, in which case the normal pickup must not also happen.
	 */
	public static boolean pickupItem(ItemEntity itemEntity, Player player) {
		if (itemEntity.level().isClientSide || itemEntity.hasPickUpDelay() || itemEntity.target != null && !player.getUUID().equals(itemEntity.target)) {
			return false;
		}
		ItemStack bag = AlchemicalBag.getFirstBagWithSuctionItem(player, player.getInventory().items);
		if (!bag.isEmpty()) {
			IAlchBagProvider bagProvider = PECapabilities.ALCH_BAG_CAPABILITY.find(player, null);
			if (bagProvider != null) {
				ItemStack stack = itemEntity.getItem();
				IItemHandler handler = bagProvider.getBag(((AlchemicalBag) bag.getItem()).color);
				ItemStack remainder = ItemHandlerHelper.insertItemStacked(handler, stack, false);

				int pickedUpCount = stack.getCount() - remainder.getCount();
				if (pickedUpCount > 0) {
					player.take(itemEntity, pickedUpCount);
					if (remainder.isEmpty()) {
						itemEntity.discard();
						//Update to the picked up count so that onItemPickup knows how much got picked up
						stack.setCount(pickedUpCount);
					}
					player.awardStat(Stats.ITEM_PICKED_UP.get(stack.getItem()), pickedUpCount);
					player.onItemPickup(itemEntity);
					return true;
				}
			}
		}
		return false;
	}


}