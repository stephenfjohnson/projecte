package moze_intel.projecte.inventory;

import moze_intel.projecte.api.inventory.IItemHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Insertion helpers that spread a stack across a handler's slots rather than targeting one.
 */
public final class ItemHandlerHelper {

	private ItemHandlerHelper() {
	}

	/**
	 * Inserts the stack into the first slots that will take it.
	 *
	 * @return Whatever could not be inserted.
	 */
	@NotNull
	public static ItemStack insertItem(@Nullable IItemHandler dest, @NotNull ItemStack stack, boolean simulate) {
		if (dest == null || stack.isEmpty()) {
			return stack;
		}
		for (int i = 0, slots = dest.getSlots(); i < slots; i++) {
			stack = dest.insertItem(i, stack, simulate);
			if (stack.isEmpty()) {
				return ItemStack.EMPTY;
			}
		}
		return stack;
	}

	/**
	 * Inserts the stack, preferring slots that already hold a matching stack so that partial stacks are
	 * topped up before empty slots are used.
	 *
	 * @return Whatever could not be inserted.
	 */
	@NotNull
	public static ItemStack insertItemStacked(@Nullable IItemHandler dest, @NotNull ItemStack stack, boolean simulate) {
		if (dest == null || stack.isEmpty()) {
			return stack;
		}
		if (!stack.isStackable()) {
			return insertItem(dest, stack, simulate);
		}
		//Fill matching, non-full slots first so stacks merge instead of scattering
		int slots = dest.getSlots();
		for (int i = 0; i < slots; i++) {
			ItemStack slotStack = dest.getStackInSlot(i);
			if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(stack, slotStack)) {
				stack = dest.insertItem(i, stack, simulate);
				if (stack.isEmpty()) {
					return ItemStack.EMPTY;
				}
			}
		}
		//Then fall back to empty slots
		for (int i = 0; i < slots; i++) {
			if (dest.getStackInSlot(i).isEmpty()) {
				stack = dest.insertItem(i, stack, simulate);
				if (stack.isEmpty()) {
					return ItemStack.EMPTY;
				}
			}
		}
		return stack;
	}

	/**
	 * Computes a comparator signal from how full the handler is, matching vanilla's container behaviour.
	 */
	public static int calcRedstoneFromInventory(@Nullable IItemHandler inv) {
		if (inv == null) {
			return 0;
		}
		int itemsFound = 0;
		float proportion = 0;
		for (int slot = 0, slots = inv.getSlots(); slot < slots; slot++) {
			ItemStack stack = inv.getStackInSlot(slot);
			if (!stack.isEmpty()) {
				proportion += stack.getCount() / (float) Math.min(inv.getSlotLimit(slot), stack.getMaxStackSize());
				itemsFound++;
			}
		}
		proportion /= inv.getSlots();
		return Mth.floor(proportion * 14) + (itemsFound > 0 ? 1 : 0);
	}
}
