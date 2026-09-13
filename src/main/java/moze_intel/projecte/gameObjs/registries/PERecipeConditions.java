package moze_intel.projecte.gameObjs.registries;

import com.mojang.serialization.MapCodec;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.customRecipes.FullKleinStarsCondition;
import moze_intel.projecte.gameObjs.customRecipes.TomeEnabledCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;

/**
 * Conditions that gate whether some of ProjectE's recipes load.
 * <p>
 * These were NeoForge recipe conditions in a registry of their own; on Fabric they are Fabric resource
 * conditions, which means the generated recipe files carry them under {@code fabric:load_conditions} instead of
 * {@code neoforge:conditions}.
 */
public class PERecipeConditions {

	public static final ResourceConditionType<TomeEnabledCondition> TOME_ENABLED =
			ResourceConditionType.create(PECore.rl("tome_enabled"), MapCodec.unit(TomeEnabledCondition.INSTANCE));
	public static final ResourceConditionType<FullKleinStarsCondition> FULL_KLEIN_STARS =
			ResourceConditionType.create(PECore.rl("full_klein_stars"), MapCodec.unit(FullKleinStarsCondition.INSTANCE));

	private PERecipeConditions() {
	}

	public static void register() {
		ResourceConditions.register(TOME_ENABLED);
		ResourceConditions.register(FULL_KLEIN_STARS);
	}
}
