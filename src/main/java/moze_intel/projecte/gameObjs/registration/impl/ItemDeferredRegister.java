package moze_intel.projecte.gameObjs.registration.impl;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.item.IAlchBagItem;
import moze_intel.projecte.api.capabilities.item.IAlchChestItem;
import moze_intel.projecte.api.capabilities.item.IExtraFunction;
import moze_intel.projecte.api.capabilities.item.IItemCharge;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.api.capabilities.item.IModeChanger;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import moze_intel.projecte.api.capabilities.item.IProjectileShooter;
import moze_intel.projecte.gameObjs.items.ICapabilityAware;
import moze_intel.projecte.gameObjs.registration.PEDeferredRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class ItemDeferredRegister extends PEDeferredRegister<Item> {

	public ItemDeferredRegister(String modid) {
		super(Registries.ITEM, modid, ItemRegistryObject::new);
	}

	@Override
	public void register() {
		super.register();
		//Capabilities are registered straight after the items themselves, as Fabric has no separate
		// registration event to wait for
		registerCapabilities();
	}

	private void registerCapabilities() {
		for (Holder<Item> entry : getEntries()) {
			Item item = entry.value();
			if (item instanceof IAlchBagItem) {
				PECapabilities.ALCH_BAG_ITEM_CAPABILITY.registerForItems((stack, context) -> (IAlchBagItem) stack.getItem(), item);
			}
			if (item instanceof IAlchChestItem) {
				PECapabilities.ALCH_CHEST_ITEM_CAPABILITY.registerForItems((stack, context) -> (IAlchChestItem) stack.getItem(), item);
			}
			if (item instanceof IExtraFunction) {
				PECapabilities.EXTRA_FUNCTION_ITEM_CAPABILITY.registerForItems((stack, context) -> (IExtraFunction) stack.getItem(), item);
			}
			if (item instanceof IItemCharge) {
				PECapabilities.CHARGE_ITEM_CAPABILITY.registerForItems((stack, context) -> (IItemCharge) stack.getItem(), item);
			}
			if (item instanceof IItemEmcHolder) {
				PECapabilities.EMC_HOLDER_ITEM_CAPABILITY.registerForItems((stack, context) -> (IItemEmcHolder) stack.getItem(), item);
			}
			if (item instanceof IModeChanger<?>) {
				PECapabilities.MODE_CHANGER_ITEM_CAPABILITY.registerForItems((stack, context) -> (IModeChanger<?>) stack.getItem(), item);
			}
			if (item instanceof IPedestalItem) {
				PECapabilities.PEDESTAL_ITEM_CAPABILITY.registerForItems((stack, context) -> (IPedestalItem) stack.getItem(), item);
			}
			if (item instanceof IProjectileShooter) {
				PECapabilities.PROJECTILE_SHOOTER_ITEM_CAPABILITY.registerForItems((stack, context) -> (IProjectileShooter) stack.getItem(), item);
			}
			if (item instanceof ICapabilityAware capabilityAware) {
				capabilityAware.attachCapabilities();
			}
		}
	}

	public ItemRegistryObject<Item> register(String name) {
		return registerSimple(name, Item::new);
	}

	public ItemRegistryObject<Item> registerFireImmune(String name) {
		return registerFireImmune(name, Item::new);
	}

	public <ITEM extends Item> ItemRegistryObject<ITEM> registerSimple(String name, Function<Item.Properties, ITEM> sup) {
		return register(name, sup, UnaryOperator.identity());
	}

	public <ITEM extends Item> ItemRegistryObject<ITEM> registerFireImmune(String name, Function<Item.Properties, ITEM> sup) {
		return register(name, sup, Item.Properties::fireResistant);
	}

	public <ITEM extends Item> ItemRegistryObject<ITEM> registerNoStack(String name, Function<Item.Properties, ITEM> sup) {
		return register(name, sup, properties -> properties.stacksTo(1));
	}

	public <ITEM extends Item> ItemRegistryObject<ITEM> registerNoStackFireImmune(String name, Function<Item.Properties, ITEM> sup) {
		return register(name, sup, properties -> properties.stacksTo(1).fireResistant());
	}

	public <ITEM extends Item> ItemRegistryObject<ITEM> registerTool(String name, Function<Item.Properties, ITEM> sup) {
		return register(name, () -> sup.apply(new NoDurabilityItemProperties().stacksTo(1).fireResistant()));
	}

	public <ITEM extends Item> ItemRegistryObject<ITEM> register(String name, Function<Item.Properties, ITEM> sup, UnaryOperator<Item.Properties> propertyModifier) {
		return register(name, () -> sup.apply(propertyModifier.apply(new Item.Properties())));
	}

	@NotNull
	@Override
	@SuppressWarnings("unchecked")
	public <ITEM extends Item> ItemRegistryObject<ITEM> register(@NotNull String name, @NotNull Supplier<? extends ITEM> sup) {
		return (ItemRegistryObject<ITEM>) super.register(name, sup);
	}

	private static class NoDurabilityItemProperties extends Item.Properties {

		@NotNull
		@Override
		public Item.Properties durability(int maxDamage) {
			//NO-OP super setting durability components
			return this;
		}
	}
}