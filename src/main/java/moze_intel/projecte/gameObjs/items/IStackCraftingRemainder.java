package moze_intel.projecte.gameObjs.items;

import net.minecraft.world.item.ItemStack;

/**
 * Marks an item that survives being used as a crafting ingredient, coming back out of the grid with all its data
 * intact.
 * <p>
 * Vanilla's crafting remainder is a plain item, set once at construction, so it can neither point at the item itself
 * nor carry the ingredient's components. NeoForge had stack-sensitive overrides for this; on Fabric it is
 * {@code RecipeMixin} that hands the remainder back, and {@link moze_intel.projecte.utils.ItemHelper#getCraftingRemainder(ItemStack)} that
 * answers for the places ProjectE asks itself.
 */
public interface IStackCraftingRemainder {

	/**
	 * @param stack The stack being consumed by the recipe.
	 *
	 * @return What to leave in the ingredient's place.
	 */
	default ItemStack getCraftingRemainder(ItemStack stack) {
		return stack.copy();
	}
}
