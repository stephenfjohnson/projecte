package moze_intel.projecte.gameObjs.items;

import net.minecraft.world.item.ItemStack;

/**
 * Implemented by items that decide for themselves when the held-item animation should replay.
 * <p>
 * Vanilla replays it whenever any part of the stack changes, which for ProjectE's items means every time their stored
 * EMC ticks over. NeoForge let the item answer; on the Fabric side {@code ItemInHandRendererMixin} asks this instead.
 */
public interface IReequipAnimation {

	boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged);
}
