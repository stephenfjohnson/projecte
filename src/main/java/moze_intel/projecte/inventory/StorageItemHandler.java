package moze_intel.projecte.inventory;

import java.util.ArrayList;
import java.util.List;
import moze_intel.projecte.api.inventory.IItemHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Presents a Fabric {@code Storage<ItemVariant>} as a slot-indexed {@link IItemHandler}.
 * <p>
 * This is the direction that matters for ProjectE's tools: a black hole band or repair talisman looks up the
 * inventory of whatever block is in front of it, which is usually a vanilla chest or another mod's machine
 * rather than a ProjectE block. Those expose themselves through Fabric's storage API, so this wraps one back
 * into the slot-indexed view the rest of the mod is written against.
 * <p>
 * Each operation runs in its own transaction; a simulated one is simply never committed.
 */
public class StorageItemHandler implements IItemHandler {

	private final Storage<ItemVariant> storage;
	/** The storage's slots when it has them, otherwise empty and the storage is treated as one bucket. */
	private final List<SingleSlotStorage<ItemVariant>> slots;

	public StorageItemHandler(Storage<ItemVariant> storage) {
		this.storage = storage;
		this.slots = storage instanceof SlottedStorage<ItemVariant> slotted ? List.copyOf(slotted.getSlots()) : List.of();
	}

	@Override
	public int getSlots() {
		if (!slots.isEmpty()) {
			return slots.size();
		}
		//Not slot based, so expose each view it currently reports as a slot
		return views().size();
	}

	private List<StorageView<ItemVariant>> views() {
		List<StorageView<ItemVariant>> views = new ArrayList<>();
		for (StorageView<ItemVariant> view : storage) {
			views.add(view);
		}
		return views;
	}

	@NotNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		if (!slots.isEmpty()) {
			return slot < slots.size() ? toStack(slots.get(slot)) : ItemStack.EMPTY;
		}
		List<StorageView<ItemVariant>> views = views();
		return slot < views.size() ? toStack(views.get(slot)) : ItemStack.EMPTY;
	}

	private static ItemStack toStack(StorageView<ItemVariant> view) {
		if (view.isResourceBlank() || view.getAmount() <= 0) {
			return ItemStack.EMPTY;
		}
		return view.getResource().toStack((int) Math.min(view.getAmount(), Integer.MAX_VALUE));
	}

	@NotNull
	@Override
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		//A slotted storage can be targeted precisely; anything else takes the stack wherever it fits
		Storage<ItemVariant> target = !slots.isEmpty() && slot < slots.size() ? slots.get(slot) : storage;
		if (!slots.isEmpty() && slot >= slots.size()) {
			return stack;
		}
		ItemVariant variant = ItemVariant.of(stack);
		try (Transaction transaction = Transaction.openOuter()) {
			long inserted = target.insert(variant, stack.getCount(), transaction);
			if (!simulate) {
				transaction.commit();
			}
			return inserted >= stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - (int) inserted);
		}
	}

	@NotNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount <= 0) {
			return ItemStack.EMPTY;
		}
		ItemStack existing = getStackInSlot(slot);
		if (existing.isEmpty()) {
			return ItemStack.EMPTY;
		}
		Storage<ItemVariant> target = !slots.isEmpty() && slot < slots.size() ? slots.get(slot) : storage;
		ItemVariant variant = ItemVariant.of(existing);
		try (Transaction transaction = Transaction.openOuter()) {
			long extracted = target.extract(variant, Math.min(amount, existing.getMaxStackSize()), transaction);
			if (extracted <= 0) {
				return ItemStack.EMPTY;
			}
			if (!simulate) {
				transaction.commit();
			}
			return variant.toStack((int) extracted);
		}
	}

	@Override
	public int getSlotLimit(int slot) {
		if (!slots.isEmpty()) {
			return slot < slots.size() ? (int) Math.min(slots.get(slot).getCapacity(), Integer.MAX_VALUE) : 0;
		}
		List<StorageView<ItemVariant>> views = views();
		return slot < views.size() ? (int) Math.min(views.get(slot).getCapacity(), Integer.MAX_VALUE) : 0;
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		//The storage API has no "would you ever accept this" question, so ask whether any of it fits right now
		return insertItem(slot, stack, true).getCount() < stack.getCount();
	}
}
