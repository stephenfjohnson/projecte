package moze_intel.projecte.utils;

import java.math.BigInteger;
import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.inventory.IItemHandler;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.integration.IntegrationHelper;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

/**
 * Helper class for player-related methods. Notice: Please try to keep methods tidy and alphabetically ordered. Thanks!
 */
public final class PlayerHelper {

	public final static ObjectiveCriteria SCOREBOARD_EMC = new ReadOnlyScoreCriteria(PECore.MODID + ":emc_score");

	/**
	 * Tries placing a block.
	 *
	 * @return Whether the block was successfully placed
	 *
	 * @implNote NeoForge captured a snapshot of every block a placement touched and fired an event so another mod
	 * - a claim or protection mod, say - could veto it and have the snapshot rolled back. Both the capture and the
	 * event are NeoForge additions to vanilla with no Fabric counterpart, so nothing can veto a placement here and
	 * the permission checks above are the only gate.
	 */
	public static boolean checkedPlaceBlock(Player player, Level level, BlockPos pos, BlockState state) {
		return hasEditPermission(player, level, pos) && partiallyCheckedPlaceBlock(player, level, pos, state);
	}

	private static boolean partiallyCheckedPlaceBlock(Player player, Level level, BlockPos pos, BlockState state) {
		SignBlockEntity oldSign = null;
		if (state.getBlock() instanceof SignBlock && level.getBlockEntity(pos) instanceof SignBlockEntity sign) {
			oldSign = sign;
		}
		BlockState oldState = level.getBlockState(pos);
		if (!level.setBlockAndUpdate(pos, state)) {
			return false;
		}
		BlockState placed = level.getBlockState(pos);
		placed.onPlace(level, pos, oldState, false);
		//Note: setBlockAndUpdate above already marked the chunk and notified neighbours, which is what NeoForge's
		// markAndNotifyBlock did a second time here
		if (oldSign != null && placed.hasBlockEntity()) {
			WorldHelper.copySignData(level, pos, oldSign);
		}
		return true;
	}

	public static boolean checkedReplaceBlock(ServerPlayer player, Level level, BlockPos pos, BlockState state) {
		return hasBreakPermission(player, level, pos) && partiallyCheckedPlaceBlock(player, level, pos, state);
	}

	public static ItemStack findFirstItem(Player player, Holder<Item> consumeFrom) {
		for (ItemStack s : player.getInventory().items) {
			if (!s.isEmpty() && s.is(consumeFrom)) {
				return s;
			}
		}
		return ItemStack.EMPTY;
	}

	public static boolean checkHotbar(Player player, BiPredicate<Player, ItemStack> checker) {
		for (int i = 0; i < Inventory.getSelectionSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (!stack.isEmpty() && checker.test(player, stack)) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkHotbarCurios(Player player, BiPredicate<Player, ItemStack> checker) {
		if (checkHotbar(player, checker)) {
			return true;
		}
		ItemStack offhand = player.getOffhandItem();
		if (!offhand.isEmpty() && checker.test(player, offhand)) {
			return true;
		}
		IItemHandler curios = IntegrationHelper.ACCESSORY_ITEM_HANDLER.find(player, null);
		if (curios != null) {
			for (int i = 0, slots = curios.getSlots(); i < slots; i++) {
				ItemStack stack = curios.getStackInSlot(i);
				if (!stack.isEmpty() && checker.test(player, stack)) {
					return true;
				}
			}
		}
		return false;
	}

	public static BlockHitResult getBlockLookingAt(Player player, double maxDistance) {
		return (BlockHitResult) player.pick(maxDistance, 1.0F, false);
	}

	/**
	 * Returns a vec representing where the player is looking, capped at maxDistance away.
	 */
	public static Vec3 getLookTarget(Player player, double maxDistance) {
		Vec3 lookAngle = player.getLookAngle();
		return player.getEyePosition().add(lookAngle.x * maxDistance, lookAngle.y * maxDistance, lookAngle.z * maxDistance);
	}

	public static boolean hasBreakPermission(ServerPlayer player, Level level, BlockPos pos) {
		return hasEditPermission(player, level, pos) && checkBreakPermission(player, level, pos);
	}

	public static boolean checkBreakPermission(ServerPlayer player, Level level, BlockPos pos) {
		return PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, player, pos, level.getBlockState(pos), level.getBlockEntity(pos));
	}

	public static boolean hasEditPermission(Player player, Level level, BlockPos pos) {
		if (!player.mayInteract(level, pos)) {
			return false;
		}
		for (Direction e : Constants.DIRECTIONS) {
			if (!player.mayUseItemAt(pos, e, ItemStack.EMPTY)) {
				return false;
			}
		}
		return true;
	}

	public static void resetCooldown(Player player) {
		player.resetAttackStrengthTicker();
		PECore.packetHandler().resetCooldown((ServerPlayer) player);
	}

	public static void swingItem(Player player, InteractionHand hand) {
		if (player.level() instanceof ServerLevel level) {
			int action = hand == InteractionHand.MAIN_HAND ? ClientboundAnimatePacket.SWING_MAIN_HAND : ClientboundAnimatePacket.SWING_OFF_HAND;
			level.getChunkSource().broadcastAndSend(player, new ClientboundAnimatePacket(player, action));
		}
	}

	public static void updateScore(ServerPlayer player, ObjectiveCriteria objective, BigInteger value) {
		updateScore(player, objective, MathUtils.clampToInt(value));
	}

	public static void updateScore(ServerPlayer player, ObjectiveCriteria objective, int value) {
		player.getScoreboard().forAllObjectives(objective, player, score -> score.set(value));
	}

	public static boolean checkFeedCooldown(Player player) {
		return player.getFoodData().needsFood() && checkCooldown(player, PEItems.BODY_STONE.get(), ProjectEConfig.server.cooldown.player.feed);
	}

	public static boolean checkHealCooldown(Player player) {
		return player.getHealth() < player.getMaxHealth() && checkCooldown(player, PEItems.SOUL_STONE.get(), ProjectEConfig.server.cooldown.player.heal);
	}

	public static boolean checkCooldown(Player player, Item item, IntSupplier cooldownSupplier) {
		ItemCooldowns cooldowns = player.getCooldowns();
		if (cooldowns.isOnCooldown(item)) {
			return false;
		}
		int cooldown = cooldownSupplier.getAsInt();
		if (cooldown == -1) {
			return false;
		} else if (cooldown > 0) {
			cooldowns.addCooldown(item, cooldown);
		}
		return true;
	}
}