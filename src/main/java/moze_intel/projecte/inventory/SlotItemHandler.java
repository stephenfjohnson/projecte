package moze_intel.projecte.inventory;

import moze_intel.projecte.api.inventory.IItemHandler;
import moze_intel.projecte.api.inventory.IItemHandlerModifiable;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A container {@link Slot} that reads and writes through an {@link IItemHandler} instead of a
 * {@link Container}.
 */
public class SlotItemHandler extends Slot {

	private static final Container EMPTY_INVENTORY = new SimpleContainer(0);

	private final IItemHandler itemHandler;
	private final int index;

	public SlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(EMPTY_INVENTORY, index, xPosition, yPosition);
		this.itemHandler = itemHandler;
		this.index = index;
	}

	public IItemHandler getItemHandler() {
		return itemHandler;
	}

	public int getSlotIndex() {
		return index;
	}

	@Override
	public boolean mayPlace(@NotNull ItemStack stack) {
		return !stack.isEmpty() && itemHandler.isItemValid(index, stack);
	}

	@NotNull
	@Override
	public ItemStack getItem() {
		return itemHandler.getStackInSlot(index);
	}

	@Override
	public void set(@NotNull ItemStack stack) {
		((IItemHandlerModifiable) itemHandler).setStackInSlot(index, stack);
		setChanged();
	}

	/**
	 * Populates the slot when contents are synced in, rather than placed by a player.
	 * <p>
	 * Vanilla has no such hook - it routes syncing through {@link #set(ItemStack)} - so this exists only so
	 * that subclasses which normalise what a slot may hold can hook both paths.
	 */
	public void initialize(@NotNull ItemStack stack) {
		((IItemHandlerModifiable) itemHandler).setStackInSlot(index, stack);
		setChanged();
	}

	@Override
	public void onQuickCraft(@NotNull ItemStack oldStack, @NotNull ItemStack newStack) {
	}

	@Override
	public int getMaxStackSize() {
		return itemHandler.getSlotLimit(index);
	}

	@Override
	public int getMaxStackSize(@NotNull ItemStack stack) {
		int maxInput = stack.getMaxStackSize();
		ItemStack maxAdd = stack.copyWithCount(maxInput);
		ItemStack currentStack = itemHandler.getStackInSlot(index);
		if (itemHandler instanceof IItemHandlerModifiable modifiable) {
			//Empty the slot so the simulated insert reports what the slot could take from scratch,
			// then put the real contents back
			modifiable.setStackInSlot(index, ItemStack.EMPTY);
			ItemStack remainder = modifiable.insertItem(index, maxAdd, true);
			modifiable.setStackInSlot(index, currentStack);
			return maxInput - remainder.getCount();
		}
		ItemStack remainder = itemHandler.insertItem(index, maxAdd, true);
		return currentStack.getCount() + (maxInput - remainder.getCount());
	}

	@Override
	public boolean mayPickup(@NotNull Player player) {
		return !itemHandler.extractItem(index, 1, true).isEmpty();
	}

	@NotNull
	@Override
	public ItemStack remove(int amount) {
		return itemHandler.extractItem(index, amount, false);
	}
}
