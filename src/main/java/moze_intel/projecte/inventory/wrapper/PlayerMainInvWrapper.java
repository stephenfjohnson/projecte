package moze_intel.projecte.inventory.wrapper;

import net.minecraft.world.entity.player.Inventory;

/**
 * Exposes only the main portion of a player's inventory (the hotbar and the three rows above it), leaving
 * armour and offhand slots out.
 */
public class PlayerMainInvWrapper extends RangedWrapper {

	public PlayerMainInvWrapper(Inventory inv) {
		super(new InvWrapper(inv), 0, inv.items.size());
	}
}
