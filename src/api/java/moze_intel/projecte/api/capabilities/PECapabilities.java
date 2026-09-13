package moze_intel.projecte.api.capabilities;

import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.api.capabilities.item.IAlchBagItem;
import moze_intel.projecte.api.capabilities.item.IAlchChestItem;
import moze_intel.projecte.api.capabilities.item.IExtraFunction;
import moze_intel.projecte.api.capabilities.item.IItemCharge;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.api.capabilities.item.IModeChanger;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import moze_intel.projecte.api.capabilities.item.IProjectileShooter;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class PECapabilities {

	private PECapabilities() {
	}

	private static ResourceLocation rl(String path) {
		return ResourceLocation.fromNamespaceAndPath(ProjectEAPI.PROJECTE_MODID, path);
	}

	/**
	 * The capability object for IEmcStorage
	 */
	public static final BlockApiLookup<IEmcStorage, @Nullable Direction> EMC_STORAGE_CAPABILITY = BlockApiLookup.get(rl("emc_storage"), IEmcStorage.class, Direction.class);

	/**
	 * The capability object for IAlchBagProvider
	 */
	public static final EntityApiLookup<IAlchBagProvider, Void> ALCH_BAG_CAPABILITY = EntityApiLookup.get(rl("alchemical_bag"), IAlchBagProvider.class, Void.class);

	/**
	 * The capability object for IKnowledgeProvider
	 */
	public static final EntityApiLookup<IKnowledgeProvider, Void> KNOWLEDGE_CAPABILITY = EntityApiLookup.get(rl("knowledge"), IKnowledgeProvider.class, Void.class);

	/**
	 * The capability object for IAlchBagItem
	 */
	public static final ItemApiLookup<IAlchBagItem, Void> ALCH_BAG_ITEM_CAPABILITY = ItemApiLookup.get(rl("alchemical_bag"), IAlchBagItem.class, Void.class);

	/**
	 * The capability object for IAlchChestItem
	 */
	public static final ItemApiLookup<IAlchChestItem, Void> ALCH_CHEST_ITEM_CAPABILITY = ItemApiLookup.get(rl("alchemical_chest"), IAlchChestItem.class, Void.class);

	/**
	 * The capability object for IExtraFunction
	 */
	public static final ItemApiLookup<IExtraFunction, Void> EXTRA_FUNCTION_ITEM_CAPABILITY = ItemApiLookup.get(rl("extra_function"), IExtraFunction.class, Void.class);

	/**
	 * The capability object for IItemCharge
	 */
	public static final ItemApiLookup<IItemCharge, Void> CHARGE_ITEM_CAPABILITY = ItemApiLookup.get(rl("charge"), IItemCharge.class, Void.class);

	/**
	 * The capability object for IItemEmcHolder
	 */
	public static final ItemApiLookup<IItemEmcHolder, Void> EMC_HOLDER_ITEM_CAPABILITY = ItemApiLookup.get(rl("emc_holder"), IItemEmcHolder.class, Void.class);

	/**
	 * The capability object for IModeChanger
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static final ItemApiLookup<IModeChanger<?>, Void> MODE_CHANGER_ITEM_CAPABILITY = ItemApiLookup.get(rl("mode_changer"), (Class) IModeChanger.class, Void.class);

	/**
	 * The capability object for IPedestalItem
	 */
	public static final ItemApiLookup<IPedestalItem, Void> PEDESTAL_ITEM_CAPABILITY = ItemApiLookup.get(rl("pedestal"), IPedestalItem.class, Void.class);

	/**
	 * The capability object for IProjectileShooter
	 */
	public static final ItemApiLookup<IProjectileShooter, Void> PROJECTILE_SHOOTER_ITEM_CAPABILITY = ItemApiLookup.get(rl("projectile_shooter"), IProjectileShooter.class, Void.class);
}