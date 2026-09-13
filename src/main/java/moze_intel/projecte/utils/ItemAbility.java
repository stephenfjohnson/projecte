package moze_intel.projecte.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A named thing a tool can do to a block, such as stripping a log or tilling soil.
 * <p>
 * NeoForge had this concept built in, along with a hook letting any mod ask a stack whether it can perform one.
 * Fabric has no equivalent, so abilities are ProjectE's own: they still drive the mod's own AOE tools, but other
 * mods can no longer ask ProjectE's tools what they are capable of, and ProjectE cannot ask theirs.
 */
public final class ItemAbility {

	private static final Map<String, ItemAbility> ABILITIES = new ConcurrentHashMap<>();

	private final String name;

	private ItemAbility(String name) {
		this.name = name;
	}

	/**
	 * Looks up the ability with this name, creating it if this is the first mention of it.
	 */
	public static ItemAbility get(String name) {
		return ABILITIES.computeIfAbsent(name, ItemAbility::new);
	}

	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
