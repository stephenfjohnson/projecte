package moze_intel.projecte.inventory.wrapper;

import moze_intel.projecte.api.inventory.IItemHandlerModifiable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Presents several {@link IItemHandlerModifiable}s as a single handler, concatenating their slots in the
 * order given.
 */
public class CombinedInvWrapper implements IItemHandlerModifiable {

	protected final IItemHandlerModifiable[] itemHandler;
	/** Index of the first slot belonging to each sub-handler, plus a trailing total. */
	protected final int[] baseIndex;
	protected final int slotCount;

	public CombinedInvWrapper(IItemHandlerModifiable... itemHandler) {
		this.itemHandler = itemHandler;
		this.baseIndex = new int[itemHandler.length];
		int index = 0;
		for (int i = 0; i < itemHandler.length; i++) {
			index += itemHandler[i].getSlots();
			baseIndex[i] = index;
		}
		this.slotCount = index;
	}

	/**
	 * @return The index into {@link #itemHandler} that owns the given combined slot.
	 */
	protected int getIndexForSlot(int slot) {
		if (slot < 0) {
			return -1;
		}
		for (int i = 0; i < baseIndex.length; i++) {
			if (slot < baseIndex[i]) {
				return i;
			}
		}
		return -1;
	}

	protected IItemHandlerModifiable getHandlerFromIndex(int index) {
		if (index < 0 || index >= itemHandler.length) {
			return EmptyItemHandler.INSTANCE;
		}
		return itemHandler[index];
	}

	/**
	 * Translates a combined slot into the sub-handler's own slot index.
	 */
	protected int getSlotFromIndex(int slot, int index) {
		if (index <= 0 || index >= baseIndex.length) {
			return slot;
		}
		return slot - baseIndex[index - 1];
	}

	@Override
	public int getSlots() {
		return slotCount;
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		int index = getIndexForSlot(slot);
		IItemHandlerModifiable handler = getHandlerFromIndex(index);
		handler.setStackInSlot(getSlotFromIndex(slot, index), stack);
	}

	@NotNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		int index = getIndexForSlot(slot);
		IItemHandlerModifiable handler = getHandlerFromIndex(index);
		return handler.getStackInSlot(getSlotFromIndex(slot, index));
	}

	@NotNull
	@Override
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		int index = getIndexForSlot(slot);
		IItemHandlerModifiable handler = getHandlerFromIndex(index);
		return handler.insertItem(getSlotFromIndex(slot, index), stack, simulate);
	}

	@NotNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		int index = getIndexForSlot(slot);
		IItemHandlerModifiable handler = getHandlerFromIndex(index);
		return handler.extractItem(getSlotFromIndex(slot, index), amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot) {
		int index = getIndexForSlot(slot);
		IItemHandlerModifiable handler = getHandlerFromIndex(index);
		return handler.getSlotLimit(getSlotFromIndex(slot, index));
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		int index = getIndexForSlot(slot);
		IItemHandlerModifiable handler = getHandlerFromIndex(index);
		return handler.isItemValid(getSlotFromIndex(slot, index), stack);
	}
}
