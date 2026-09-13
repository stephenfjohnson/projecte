package moze_intel.projecte.api.capabilities.item;

import moze_intel.projecte.api.inventory.IItemHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * This interfaces specifies items that perform a specific function every tick when inside an Alchemical Bag, on a player
 * <p>
 * This is exposed through the Capability system.
 * <p>
 * Acquire an instance of this using {@code PECapabilities.ALCH_BAG_ITEM_CAPABILITY.find(stack, null)}.
 *
 * @author williewillus
 */
public interface IAlchBagItem {

	/**
	 * Called on both client and server every time the alchemical bag ticks this item
	 *
	 * @param inv    The inventory of the bag
	 * @param player The player whose bag is being ticked
	 * @param stack  The ItemStack being ticked
	 *
	 * @return Whether the inventory was changed by this item ticking
	 */
	boolean updateInAlchBag(@NotNull IItemHandler inv, @NotNull Player player, @NotNull ItemStack stack);
}