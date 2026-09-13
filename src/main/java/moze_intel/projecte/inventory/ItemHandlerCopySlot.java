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
 * A {@link Slot} for handlers that hand back copies rather than live stacks, such as one backed by a data
 * component on an item.
 * <p>
 * Vanilla's slot contract assumes {@link #getItem()} returns the stack the inventory itself holds, and that
 * mutating it in place followed by {@link #setChanged()} is enough to persist the change. A copying handler
 * breaks that, so this slot keeps the copy it handed out and writes it back to the handler on
 * {@link #setChanged()}.
 */
public class ItemHandlerCopySlot extends Slot {

	private static final Container EMPTY_INVENTORY = new SimpleContainer(0);

	private final IItemHandler itemHandler;
	private final int index;
	/** The copy previously handed to callers, which they may have mutated in place. */
	private ItemStack cachedStack = ItemStack.EMPTY;
	private boolean cacheValid;

	public ItemHandlerCopySlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
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
		if (!cacheValid) {
			cachedStack = itemHandler.getStackInSlot(index).copy();
			cacheValid = true;
		}
		return cachedStack;
	}

	@Override
	public void set(@NotNull ItemStack stack) {
		cachedStack = stack;
		cacheValid = true;
		setChanged();
	}

	/**
	 * Populates the slot when contents are synced in, rather than placed by a player.
	 * <p>
	 * Vanilla has no such hook - it routes syncing through {@link #set(ItemStack)} - so this exists only so
	 * that subclasses which normalise what a slot may hold can hook both paths.
	 */
	public void initialize(@NotNull ItemStack stack) {
		set(stack);
	}

	@Override
	public void setChanged() {
		//Push whatever the caller has been mutating back into the handler, since our copy is the only
		// place the change currently exists
		if (cacheValid) {
			((IItemHandlerModifiable) itemHandler).setStackInSlot(index, cachedStack.copy());
		}
		cacheValid = false;
		super.setChanged();
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
		return Math.min(getMaxStackSize(), stack.getMaxStackSize());
	}

	@Override
	public boolean mayPickup(@NotNull Player player) {
		return !itemHandler.extractItem(index, 1, true).isEmpty();
	}

	@NotNull
	@Override
	public ItemStack remove(int amount) {
		ItemStack extracted = itemHandler.extractItem(index, amount, false);
		cacheValid = false;
		return extracted;
	}
}
