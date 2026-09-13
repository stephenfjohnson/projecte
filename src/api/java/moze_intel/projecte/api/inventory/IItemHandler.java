package moze_intel.projecte.api.inventory;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A slot-indexed view of an item inventory.
 * <p>
 * Fabric's own item transfer abstraction ({@code Storage<ItemVariant>}) is transaction based and has no
 * notion of a stable slot index, which the bulk of ProjectE's inventory logic is written against. This
 * interface keeps that slot-indexed contract so the mod's own logic stays as it was, and interoperability
 * with other Fabric mods is handled separately by bridging to {@code Storage<ItemVariant>} at the edges.
 */
public interface IItemHandler {

	/**
	 * @return The number of slots this handler exposes.
	 */
	int getSlots();

	/**
	 * Returns the stack in the given slot.
	 * <p>
	 * The returned stack <strong>must not</strong> be modified by the caller. Copy it first if you need to
	 * change it.
	 *
	 * @param slot Slot index, in {@code [0, getSlots())}.
	 *
	 * @return The stack in the slot, or {@link ItemStack#EMPTY}.
	 */
	@NotNull
	ItemStack getStackInSlot(int slot);

	/**
	 * Inserts as much of the given stack as the slot will accept.
	 *
	 * @param slot     Slot index, in {@code [0, getSlots())}.
	 * @param stack    Stack to insert. Never modified by this method.
	 * @param simulate If {@code true}, the insertion is only reported and nothing is actually changed.
	 *
	 * @return The portion of the stack that was <em>not</em> accepted, or {@link ItemStack#EMPTY} if all of it
	 * was.
	 */
	@NotNull
	ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate);

	/**
	 * Extracts up to {@code amount} items from the given slot.
	 *
	 * @param slot     Slot index, in {@code [0, getSlots())}.
	 * @param amount   Maximum number of items to extract. May be larger than the stack or its max stack size.
	 * @param simulate If {@code true}, the extraction is only reported and nothing is actually changed.
	 *
	 * @return The extracted stack, or {@link ItemStack#EMPTY} if nothing could be extracted.
	 */
	@NotNull
	ItemStack extractItem(int slot, int amount, boolean simulate);

	/**
	 * @param slot Slot index, in {@code [0, getSlots())}.
	 *
	 * @return The maximum number of items the slot can hold, ignoring per-item stack size limits.
	 */
	int getSlotLimit(int slot);

	/**
	 * Whether the slot would ever accept the given stack, ignoring how full it currently is. Used to filter
	 * insertion attempts early; it must not depend on the slot's current contents.
	 *
	 * @param slot  Slot index, in {@code [0, getSlots())}.
	 * @param stack Stack to test. Never modified by this method.
	 */
	boolean isItemValid(int slot, @NotNull ItemStack stack);
}
