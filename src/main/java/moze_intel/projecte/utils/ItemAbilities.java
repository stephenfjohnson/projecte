package moze_intel.projecte.utils;

import java.util.Set;

/**
 * The tool abilities ProjectE cares about, named to match what NeoForge called them so the behaviour of each
 * ProjectE tool is unchanged.
 */
public final class ItemAbilities {

	private ItemAbilities() {
	}

	public static final ItemAbility AXE_STRIP = ItemAbility.get("axe_strip");
	public static final ItemAbility AXE_SCRAPE = ItemAbility.get("axe_scrape");
	public static final ItemAbility AXE_WAX_OFF = ItemAbility.get("axe_wax_off");
	public static final ItemAbility AXE_DIG = ItemAbility.get("axe_dig");
	public static final ItemAbility HOE_TILL = ItemAbility.get("hoe_till");
	public static final ItemAbility HOE_DIG = ItemAbility.get("hoe_dig");
	public static final ItemAbility SHOVEL_FLATTEN = ItemAbility.get("shovel_flatten");
	public static final ItemAbility SHOVEL_DIG = ItemAbility.get("shovel_dig");
	public static final ItemAbility PICKAXE_DIG = ItemAbility.get("pickaxe_dig");
	public static final ItemAbility SWORD_DIG = ItemAbility.get("sword_dig");
	public static final ItemAbility SWORD_SWEEP = ItemAbility.get("sword_sweep");
	public static final ItemAbility SHEARS_DIG = ItemAbility.get("shears_dig");
	public static final ItemAbility SHEARS_HARVEST = ItemAbility.get("shears_harvest");
	public static final ItemAbility SHEARS_CARVE = ItemAbility.get("shears_carve");
	public static final ItemAbility SHEARS_DISARM = ItemAbility.get("shears_disarm");
	public static final ItemAbility SHEARS_TRIM = ItemAbility.get("shears_trim");
	public static final ItemAbility FIRESTARTER_LIGHT = ItemAbility.get("firestarter_light");

	public static final Set<ItemAbility> DEFAULT_AXE_ACTIONS = Set.of(AXE_DIG, AXE_STRIP, AXE_SCRAPE, AXE_WAX_OFF);
	public static final Set<ItemAbility> DEFAULT_HOE_ACTIONS = Set.of(HOE_DIG, HOE_TILL);
	public static final Set<ItemAbility> DEFAULT_SHOVEL_ACTIONS = Set.of(SHOVEL_DIG, SHOVEL_FLATTEN);
	public static final Set<ItemAbility> DEFAULT_PICKAXE_ACTIONS = Set.of(PICKAXE_DIG);
	public static final Set<ItemAbility> DEFAULT_SWORD_ACTIONS = Set.of(SWORD_DIG, SWORD_SWEEP);
	public static final Set<ItemAbility> DEFAULT_SHEARS_ACTIONS = Set.of(SHEARS_DIG, SHEARS_HARVEST, SHEARS_CARVE, SHEARS_DISARM, SHEARS_TRIM);
}
