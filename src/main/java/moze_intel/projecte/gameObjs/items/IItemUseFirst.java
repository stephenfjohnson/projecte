package moze_intel.projecte.gameObjs.items;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/**
 * Implemented by items that act on a block before the block gets a say, which is how the philosopher's stone can
 * transmute things that have their own right-click behaviour, like signs.
 * <p>
 * NeoForge had a hook on the item for this; on Fabric it is Fabric's own use-block event, which
 * {@code PECore#registerUseFirst} wires up.
 */
public interface IItemUseFirst {

	InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context);
}
