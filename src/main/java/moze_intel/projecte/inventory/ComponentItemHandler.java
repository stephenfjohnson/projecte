package moze_intel.projecte.inventory;

import moze_intel.projecte.api.inventory.IItemHandlerModifiable;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.NotNull;

/**
 * An item handler whose contents live in an {@link ItemContainerContents} data component on a parent stack,
 * so that an item can carry an inventory around with it.
 * <p>
 * {@link ItemContainerContents} is immutable, so every write reads the component out, changes one slot and
 * stores a fresh component back. That also means {@link #getStackInSlot(int)} hands back a copy rather than a
 * live stack, so container slots over this handler must be {@link ItemHandlerCopySlot}s.
 */
public class ComponentItemHandler implements IItemHandlerModifiable {

	private final ItemStack parent;
	private final DataComponentType<ItemContainerContents> componentType;
	private final int size;

	public ComponentItemHandler(ItemStack parent, DataComponentType<ItemContainerContents> componentType, int size) {
		this.parent = parent;
		this.componentType = componentType;
		this.size = size;
	}

	protected ItemContainerContents getContents() {
		return parent.getOrDefault(componentType, ItemContainerContents.EMPTY);
	}

	private NonNullList<ItemStack> copyOut() {
		NonNullList<ItemStack> stacks = NonNullList.withSize(size, ItemStack.EMPTY);
		getContents().copyInto(stacks);
		return stacks;
	}

	@Override
	public int getSlots() {
		return size;
	}

	@NotNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		validateSlotIndex(slot);
		return copyOut().get(slot);
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		validateSlotIndex(slot);
		updateContents(getContents(), stack, slot);
	}

	/**
	 * Stores {@code stack} into {@code slot} and writes the resulting component back onto the parent stack.
	 * Subclasses override this to normalise what actually gets persisted.
	 */
	protected void updateContents(@NotNull ItemContainerContents contents, @NotNull ItemStack stack, int slot) {
		NonNullList<ItemStack> stacks = NonNullList.withSize(size, ItemStack.EMPTY);
		contents.copyInto(stacks);
		stacks.set(slot, stack);
		parent.set(componentType, ItemContainerContents.fromItems(stacks));
	}

	@NotNull
	@Override
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		} else if (!isItemValid(slot, stack)) {
			return stack;
		}
		validateSlotIndex(slot);
		ItemContainerContents contents = getContents();
		NonNullList<ItemStack> stacks = NonNullList.withSize(size, ItemStack.EMPTY);
		contents.copyInto(stacks);
		ItemStack existing = stacks.get(slot);
		int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
		if (!existing.isEmpty()) {
			if (!ItemStack.isSameItemSameComponents(stack, existing)) {
				return stack;
			}
			limit -= existing.getCount();
		}
		if (limit <= 0) {
			return stack;
		}
		boolean reachedLimit = stack.getCount() > limit;
		int inserted = reachedLimit ? limit : stack.getCount();
		if (!simulate) {
			updateContents(contents, stack.copyWithCount(existing.getCount() + inserted), slot);
		}
		return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
	}

	@NotNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount == 0) {
			return ItemStack.EMPTY;
		}
		validateSlotIndex(slot);
		ItemContainerContents contents = getContents();
		NonNullList<ItemStack> stacks = NonNullList.withSize(size, ItemStack.EMPTY);
		contents.copyInto(stacks);
		ItemStack existing = stacks.get(slot);
		if (existing.isEmpty()) {
			return ItemStack.EMPTY;
		}
		int toExtract = Math.min(amount, existing.getMaxStackSize());
		if (existing.getCount() <= toExtract) {
			if (!simulate) {
				updateContents(contents, ItemStack.EMPTY, slot);
			}
			return existing;
		}
		if (!simulate) {
			updateContents(contents, existing.copyWithCount(existing.getCount() - toExtract), slot);
		}
		return existing.copyWithCount(toExtract);
	}

	@Override
	public int getSlotLimit(int slot) {
		return Item.ABSOLUTE_MAX_STACK_SIZE;
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		return true;
	}

	protected void validateSlotIndex(int slot) {
		if (slot < 0 || slot >= size) {
			throw new IllegalArgumentException("Slot " + slot + " not in valid range - [0," + size + ")");
		}
	}
}
