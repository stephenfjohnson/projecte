package moze_intel.projecte.utils;

import java.util.Map;
import moze_intel.projecte.gameObjs.blocks.ProjectETNT;
import moze_intel.projecte.gameObjs.items.IItemAbilityProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

/**
 * Works out what a block turns into when a tool acts on it.
 * <p>
 * NeoForge answered this with {@code BlockState#getToolModifiedState}, which both told a tool whether it could
 * act on a block and what the result would be. Fabric has no equivalent, so this reads the same vanilla tables
 * that the vanilla tools use.
 *
 * @implNote Because these are vanilla's own tables, blocks added by other mods are covered exactly as far as
 * they register themselves with vanilla. The one exception is tilling: vanilla's table stores an action to run
 * rather than the resulting state, so the results are restated here, and a modded tillable block will not be
 * recognised.
 */
public final class ToolActions {

	/**
	 * What tilling turns a block into. Vanilla stores tilling as a world-mutating action rather than a
	 * resulting state, so it cannot be read back the way stripping and flattening can.
	 */
	private static final Map<Block, Block> TILLABLES = Map.of(
			Blocks.GRASS_BLOCK, Blocks.FARMLAND,
			Blocks.DIRT_PATH, Blocks.FARMLAND,
			Blocks.DIRT, Blocks.FARMLAND,
			Blocks.COARSE_DIRT, Blocks.DIRT,
			Blocks.ROOTED_DIRT, Blocks.DIRT
	);

	private ToolActions() {
	}

	/**
	 * @param state    The block being acted on.
	 * @param context  The use that triggered this.
	 * @param ability  What the tool is trying to do.
	 * @param simulate Present for symmetry with the call sites; nothing here changes the world either way.
	 *
	 * @return What the block would become, or {@code null} if this tool cannot act on it.
	 */
	@Nullable
	public static BlockState getModifiedState(BlockState state, UseOnContext context, ItemAbility ability, boolean simulate) {
		if (ability == ItemAbilities.AXE_STRIP) {
			Block stripped = AxeItem.STRIPPABLES.get(state.getBlock());
			//Stripping keeps the log's orientation
			return stripped == null ? null : stripped.defaultBlockState()
					.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
		} else if (ability == ItemAbilities.AXE_SCRAPE) {
			return WeatheringCopper.getPrevious(state).orElse(null);
		} else if (ability == ItemAbilities.AXE_WAX_OFF) {
			Block unwaxed = HoneycombItem.WAX_OFF_BY_BLOCK.get().get(state.getBlock());
			return unwaxed == null ? null : unwaxed.withPropertiesOf(state);
		} else if (ability == ItemAbilities.SHOVEL_FLATTEN) {
			return ShovelItem.FLATTENABLES.get(state.getBlock());
		} else if (ability == ItemAbilities.HOE_TILL) {
			return tilled(state, context);
		} else if (ability == ItemAbilities.FIRESTARTER_LIGHT) {
			return lit(state);
		}
		return null;
	}

	@Nullable
	private static BlockState tilled(BlockState state, UseOnContext context) {
		Block result = TILLABLES.get(state.getBlock());
		if (result == null) {
			return null;
		}
		//Vanilla only tills soil with nothing solid sitting on top of it
		Level level = context.getLevel();
		BlockPos above = context.getClickedPos().above();
		if (result == Blocks.FARMLAND && !level.getBlockState(above).isAir()) {
			return null;
		}
		return result.defaultBlockState();
	}

	/**
	 * Lighting only applies to things that hold a flame and are not already lit, mirroring flint and steel.
	 */
	@Nullable
	private static BlockState lit(BlockState state) {
		if (CampfireBlock.canLight(state) || CandleBlock.canLight(state) || CandleCakeBlock.canLight(state)) {
			return state.setValue(BlockStateProperties.LIT, true);
		}
		return null;
	}

	/**
	 * Whether a stack can perform an ability.
	 * <p>
	 * ProjectE's own tools answer for themselves; for anything else this recognises the vanilla tools, which is
	 * as far as it can go now that there is no cross-mod hook to ask through.
	 */
	public static boolean canPerformAction(ItemStack stack, ItemAbility ability) {
		if (stack.getItem() instanceof IItemAbilityProvider provider) {
			return provider.canPerformAction(stack, ability);
		} else if (ability == ItemAbilities.FIRESTARTER_LIGHT) {
			return stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE);
		} else if (ItemAbilities.DEFAULT_AXE_ACTIONS.contains(ability)) {
			return stack.is(net.minecraft.tags.ItemTags.AXES);
		} else if (ItemAbilities.DEFAULT_HOE_ACTIONS.contains(ability)) {
			return stack.is(net.minecraft.tags.ItemTags.HOES);
		} else if (ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(ability)) {
			return stack.is(net.minecraft.tags.ItemTags.SHOVELS);
		} else if (ItemAbilities.DEFAULT_PICKAXE_ACTIONS.contains(ability)) {
			return stack.is(net.minecraft.tags.ItemTags.PICKAXES);
		} else if (ItemAbilities.DEFAULT_SHEARS_ACTIONS.contains(ability)) {
			return stack.is(Items.SHEARS);
		} else if (ItemAbilities.DEFAULT_SWORD_ACTIONS.contains(ability)) {
			return stack.is(net.minecraft.tags.ItemTags.SWORDS);
		}
		return false;
	}

	/**
	 * Sets a block alight the way flint and steel would when fire cannot simply be placed in front of it.
	 * <p>
	 * NeoForge asked each block what to do; the only vanilla blocks that answer are the TNTs, which prime themselves.
	 *
	 * @return {@code true} if the block responded, so the caller knows the ignition did something.
	 */
	public static boolean catchFire(BlockState state, Level level, BlockPos pos, @Nullable LivingEntity igniter) {
		if (state.getBlock() instanceof ProjectETNT tnt) {
			tnt.prime(level, pos, igniter);
			return true;
		} else if (state.getBlock() instanceof TntBlock) {
			TntBlock.explode(level, pos);
			return true;
		}
		return false;
	}

	/**
	 * @return {@code true} if the block is one a hoe could till, ignoring what is above it.
	 */
	public static boolean isTillable(BlockState state) {
		return TILLABLES.containsKey(state.getBlock()) || state.is(BlockTags.DIRT);
	}
}
