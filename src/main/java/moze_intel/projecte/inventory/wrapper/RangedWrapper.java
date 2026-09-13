package moze_intel.projecte.inventory.wrapper;

import moze_intel.projecte.api.inventory.IItemHandlerModifiable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Exposes a contiguous slot range of another handler as a handler in its own right.
 */
public class RangedWrapper implements IItemHandlerModifiable {

	private final IItemHandlerModifiable compose;
	private final int minSlot;
	private final int maxSlotExclusive;

	public RangedWrapper(IItemHandlerModifiable compose, int minSlotInclusive, int maxSlotExclusive) {
		this.compose = compose;
		this.minSlot = minSlotInclusive;
		this.maxSlotExclusive = maxSlotExclusive;
	}

	@Override
	public int getSlots() {
		return maxSlotExclusive - minSlot;
	}

	private boolean checkSlot(int localSlot) {
		return localSlot + minSlot < maxSlotExclusive && localSlot >= 0;
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		if (checkSlot(slot)) {
			compose.setStackInSlot(slot + minSlot, stack);
		}
	}

	@NotNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return checkSlot(slot) ? compose.getStackInSlot(slot + minSlot) : ItemStack.EMPTY;
	}

	@NotNull
	@Override
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		return checkSlot(slot) ? compose.insertItem(slot + minSlot, stack, simulate) : stack;
	}

	@NotNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return checkSlot(slot) ? compose.extractItem(slot + minSlot, amount, simulate) : ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot) {
		return checkSlot(slot) ? compose.getSlotLimit(slot + minSlot) : 0;
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return checkSlot(slot) && compose.isItemValid(slot + minSlot, stack);
	}
}
