package moze_intel.projecte.gameObjs.customRecipes;

import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.registries.PERecipeConditions;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.NotNull;

public class FullKleinStarsCondition implements ResourceCondition {

	public static final FullKleinStarsCondition INSTANCE = new FullKleinStarsCondition();

	private FullKleinStarsCondition() {
	}

	@Override
	public boolean test(@NotNull HolderLookup.Provider registries) {
		return ProjectEConfig.common.fullKleinStars.get();
	}

	@NotNull
	@Override
	public ResourceConditionType<?> getType() {
		return PERecipeConditions.FULL_KLEIN_STARS;
	}
}