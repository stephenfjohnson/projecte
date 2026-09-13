package moze_intel.projecte.api.capabilities;

import java.util.Set;
import moze_intel.projecte.api.inventory.IItemHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;

/**
 * This interface defines the contract for some object that exposes sixteen colored inventories, for the purpose of usage as Alchemical Bags.
 * <p>
 * This is exposed through the Capability system.
 * <p>
 * Acquire an instance of this using {@code PECapabilities.ALCH_BAG_CAPABILITY.find(entity, null)}.
 */
public interface IAlchBagProvider {

	/**
	 * Note: modifying this clientside is not advised
	 *
	 * @param color The bag color to acquire
	 *
	 * @return The inventory representing this alchemical bag
	 */
	@NotNull
	IItemHandler getBag(@NotNull DyeColor color);

	/**
	 * Syncs the bag inventories associated with the provided colors to the player provided (usually the owner of this capability instance)
	 *
	 * @param player The player to sync the bags to.
	 * @param colors The bag colors to sync.
	 */
	void sync(@NotNull ServerPlayer player, @NotNull Set<DyeColor> colors);

	/**
	 * Syncs all bag inventories associated to the player provided (usually the owner of this capability instance)
	 *
	 * @param player The player to sync the bags to.
	 */
	void syncAllBags(@NotNull ServerPlayer player);
}