package moze_intel.projecte.api.inventory;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * An {@link IItemHandler} whose slots can be overwritten outright, bypassing the insertion rules.
 */
public interface IItemHandlerModifiable extends IItemHandler {

	/**
	 * Overwrites the contents of the given slot.
	 * <p>
	 * This bypasses every check {@link #insertItem(int, ItemStack, boolean)} would make, including
	 * {@link #isItemValid(int, ItemStack)} and the slot limit, so callers are responsible for passing
	 * something the slot can legitimately hold.
	 *
	 * @param slot  Slot index, in {@code [0, getSlots())}.
	 * @param stack Stack to store, or {@link ItemStack#EMPTY} to clear the slot. Ownership passes to the
	 *              handler, so do not keep using the instance afterwards.
	 */
	void setStackInSlot(int slot, @NotNull ItemStack stack);
}
