package moze_intel.projecte.gameObjs.customRecipes;

import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.registries.PERecipeConditions;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.NotNull;

public class TomeEnabledCondition implements ResourceCondition {

	public static final TomeEnabledCondition INSTANCE = new TomeEnabledCondition();

	private TomeEnabledCondition() {
	}

	@Override
	public boolean test(@NotNull HolderLookup.Provider registries) {
		return ProjectEConfig.common.craftableTome.get();
	}

	@NotNull
	@Override
	public ResourceConditionType<?> getType() {
		return PERecipeConditions.TOME_ENABLED;
	}
}