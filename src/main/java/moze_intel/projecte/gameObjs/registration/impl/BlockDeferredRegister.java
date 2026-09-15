package moze_intel.projecte.gameObjs.registration.impl;

import java.util.function.Function;
import java.util.function.Supplier;
import moze_intel.projecte.gameObjs.registration.DeferredHolder;
import moze_intel.projecte.gameObjs.registration.DoubleDeferredRegister;
import moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject.WallOrFloorBlockRegistryObject;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockDeferredRegister extends DoubleDeferredRegister<Block, Item> {

	public BlockDeferredRegister(String modid) {
		super(Registries.BLOCK, new ItemDeferredRegister(modid), modid);
	}

	public BlockRegistryObject<Block, BlockItem> register(String name, BlockBehaviour.Properties properties) {
		return register(name, () -> new Block(properties));
	}

	public <BLOCK extends Block> BlockRegistryObject<BLOCK, BlockItem> register(String name, Supplier<? extends BLOCK> blockSupplier) {
		return register(name, blockSupplier, block -> new BlockItem(block, new Item.Properties()));
	}

	public <BLOCK extends Block, WALL_BLOCK extends Block> WallOrFloorBlockRegistryObject<BLOCK, WALL_BLOCK, StandingAndWallBlockItem> registerWallOrFloorItem(String name,
			Function<BlockBehaviour.Properties, BLOCK> blockSupplier, Function<BlockBehaviour.Properties, WALL_BLOCK> wallBlockSupplier,
			BlockBehaviour.Properties baseProperties) {
		DeferredHolder<Block, BLOCK> primaryObject = primaryRegister.register(name, () -> blockSupplier.apply(baseProperties));
		//Note: The wall variant drops what the floor variant does. NeoForge could take the block itself, which does
		// not exist yet here, so the loot table is named the way vanilla derives it from a block's own id
		DeferredHolder<Block, WALL_BLOCK> wallObject = primaryRegister.register("wall_" + name, () -> {
			baseProperties.drops = ResourceKey.create(Registries.LOOT_TABLE, primaryObject.getId().withPrefix("blocks/"));
			return wallBlockSupplier.apply(baseProperties);
		});
		return new WallOrFloorBlockRegistryObject<>(primaryObject, wallObject, secondaryRegister.register(name, () -> new StandingAndWallBlockItem(primaryObject.get(), wallObject.get(),
				new Item.Properties(), Direction.DOWN)));
	}

	public <BLOCK extends Block, ITEM extends BlockItem> BlockRegistryObject<BLOCK, ITEM> register(String name, Supplier<? extends BLOCK> blockSupplier,
			Function<BLOCK, ITEM> itemCreator) {
		return register(name, blockSupplier, itemCreator, BlockRegistryObject::new);
	}
}