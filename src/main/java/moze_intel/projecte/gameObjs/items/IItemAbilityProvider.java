package moze_intel.projecte.gameObjs.items;

import moze_intel.projecte.utils.ItemAbility;
import net.minecraft.world.item.ItemStack;

/**
 * Implemented by items that can act on blocks the way a vanilla tool does - stripping, tilling, flattening and
 * so on.
 * <p>
 * NeoForge put this method on Item itself so any mod could ask any stack. Fabric has no such hook, so ProjectE's
 * tools declare it here and {@link moze_intel.projecte.utils.ToolActions#canPerformAction} does the asking.
 */
public interface IItemAbilityProvider {

	boolean canPerformAction(ItemStack stack, ItemAbility ability);
}
