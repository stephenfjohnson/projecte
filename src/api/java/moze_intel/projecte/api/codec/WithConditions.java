package moze_intel.projecte.api.codec;

import java.util.List;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.minecraft.core.HolderLookup;

/**
 * A value loaded from a data file, together with the load conditions that were attached to it.
 *
 * @param conditions Conditions that must all hold for the value to be used. Empty means unconditional.
 * @param carrier    The value itself.
 */
public record WithConditions<T>(List<ResourceCondition> conditions, T carrier) {

	public WithConditions(T carrier, ResourceCondition... conditions) {
		this(List.of(conditions), carrier);
	}

	/**
	 * Evaluates this entry's conditions.
	 *
	 * @param registries Registries the conditions are checked against.
	 *
	 * @return {@code true} if every condition holds, so the value should be loaded.
	 */
	public boolean conditionsMet(HolderLookup.Provider registries) {
		for (ResourceCondition condition : conditions) {
			if (!condition.test(registries)) {
				return false;
			}
		}
		return true;
	}
}
