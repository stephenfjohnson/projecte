package moze_intel.projecte.gameObjs.items;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Implemented by weapons that sweep wider than vanilla does, such as ProjectE's swords and katars, whose reach grows
 * with their charge.
 * <p>
 * NeoForge asked the item for the box; on Fabric {@code PlayerSweepMixin} asks this while vanilla gathers what the
 * sweep hits.
 */
public interface ISweepHitBoxProvider {

	/**
	 * @param stack        The weapon being swung.
	 * @param player       The player swinging it.
	 * @param targetBounds The bounding box of what was hit.
	 *
	 * @return The area the sweep should cover.
	 */
	AABB getSweepHitBox(ItemStack stack, Player player, AABB targetBounds);
}
