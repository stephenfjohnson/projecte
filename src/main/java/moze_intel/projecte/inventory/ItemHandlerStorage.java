package moze_intel.projecte.inventory;

import java.util.ArrayList;
import java.util.List;
import moze_intel.projecte.api.inventory.IItemHandler;
import moze_intel.projecte.api.inventory.IItemHandlerModifiable;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedSlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;

/**
 * Exposes an {@link IItemHandler} as a Fabric {@code Storage<ItemVariant>}, which is how other Fabric mods -
 * and, via Fabric API's hopper patch, vanilla hoppers - move items in and out of a block.
 * <p>
 * The two models differ in how they undo work: ProjectE's handlers either apply a change or simulate it,
 * while Fabric's storages apply changes inside a transaction that may later be rolled back. Each slot is
 * therefore a {@link SnapshotParticipant} that remembers the stack it held before the transaction touched it,
 * and restores it if the transaction aborts.
 */
public class ItemHandlerStorage extends CombinedSlottedStorage<ItemVariant, SingleSlotStorage<ItemVariant>> {

	public ItemHandlerStorage(IItemHandlerModifiable handler) {
		super(slotsOf(handler));
	}

	private static List<SingleSlotStorage<ItemVariant>> slotsOf(IItemHandlerModifiable handler) {
		List<SingleSlotStorage<ItemVariant>> slots = new ArrayList<>(handler.getSlots());
		for (int slot = 0, count = handler.getSlots(); slot < count; slot++) {
			slots.add(new SlotStorage(handler, slot));
		}
		return List.copyOf(slots);
	}

	private static class SlotStorage extends SnapshotParticipant<ItemStack> implements SingleSlotStorage<ItemVariant> {

		private final IItemHandlerModifiable handler;
		private final int slot;

		private SlotStorage(IItemHandlerModifiable handler, int slot) {
			this.handler = handler;
			this.slot = slot;
		}

		@Override
		protected ItemStack createSnapshot() {
			return handler.getStackInSlot(slot).copy();
		}

		@Override
		protected void readSnapshot(ItemStack snapshot) {
			handler.setStackInSlot(slot, snapshot);
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			StoragePreconditions.notBlankNotNegative(resource, maxAmount);
			int toInsert = (int) Math.min(maxAmount, Integer.MAX_VALUE);
			//Ask the handler what it would take before committing, so a refusal costs no snapshot
			ItemStack remainder = handler.insertItem(slot, resource.toStack(toInsert), true);
			int inserted = toInsert - remainder.getCount();
			if (inserted <= 0) {
				return 0;
			}
			updateSnapshots(transaction);
			handler.insertItem(slot, resource.toStack(inserted), false);
			return inserted;
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			StoragePreconditions.notBlankNotNegative(resource, maxAmount);
			if (!resource.matches(handler.getStackInSlot(slot))) {
				return 0;
			}
			int toExtract = (int) Math.min(maxAmount, Integer.MAX_VALUE);
			ItemStack simulated = handler.extractItem(slot, toExtract, true);
			if (simulated.isEmpty()) {
				return 0;
			}
			updateSnapshots(transaction);
			return handler.extractItem(slot, simulated.getCount(), false).getCount();
		}

		@Override
		public boolean isResourceBlank() {
			return handler.getStackInSlot(slot).isEmpty();
		}

		@Override
		public ItemVariant getResource() {
			return ItemVariant.of(handler.getStackInSlot(slot));
		}

		@Override
		public long getAmount() {
			return handler.getStackInSlot(slot).getCount();
		}

		@Override
		public long getCapacity() {
			ItemStack stack = handler.getStackInSlot(slot);
			int slotLimit = handler.getSlotLimit(slot);
			return stack.isEmpty() ? slotLimit : Math.min(slotLimit, stack.getMaxStackSize());
		}
	}
}
