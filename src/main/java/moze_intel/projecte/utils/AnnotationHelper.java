package moze_intel.projecte.utils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import moze_intel.projecte.PECore;
import moze_intel.projecte.PEPlatform;
import moze_intel.projecte.api.components.DataComponentProcessor;
import moze_intel.projecte.api.components.IDataComponentProcessor;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.recipe.IRecipeTypeMapper;
import moze_intel.projecte.api.mapper.recipe.RecipeTypeMapper;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import org.jetbrains.annotations.Nullable;

/**
 * Finds the EMC mappers, recipe mappers and data component processors that ProjectE and its addons provide.
 * <p>
 * NeoForge could scan every mod's classes for annotations, which is how these used to be discovered. Fabric has
 * no such scanning, so each one is declared as an entrypoint in its mod's {@code fabric.mod.json} instead, and
 * the annotation is still read off the loaded class for its priority and required mods. A mod adding one needs
 * to list it under the matching entrypoint key as well as annotating it.
 */
public class AnnotationHelper {

	/** Entrypoint keys addons declare their contributions under. */
	public static final String DATA_COMPONENT_PROCESSOR_ENTRYPOINT = "projecte:data_component_processors";
	public static final String RECIPE_TYPE_MAPPER_ENTRYPOINT = "projecte:recipe_type_mappers";
	public static final String EMC_MAPPER_ENTRYPOINT = "projecte:emc_mappers";

	public static List<IDataComponentProcessor> getDataComponentProcessors() {
		return load(DATA_COMPONENT_PROCESSOR_ENTRYPOINT, IDataComponentProcessor.class, DataComponentProcessor.class,
				DataComponentProcessor::priority, DataComponentProcessor::requiredMods,
				IDataComponentProcessor::getName, "Data Component Processor");
	}

	public static List<IRecipeTypeMapper> getRecipeTypeMappers() {
		return load(RECIPE_TYPE_MAPPER_ENTRYPOINT, IRecipeTypeMapper.class, RecipeTypeMapper.class,
				RecipeTypeMapper::priority, RecipeTypeMapper::requiredMods,
				IRecipeTypeMapper::getName, "RecipeType Mapper");
	}

	@SuppressWarnings("unchecked")
	public static List<IEMCMapper<NormalizedSimpleStack, Long>> getEMCMappers() {
		return (List<IEMCMapper<NormalizedSimpleStack, Long>>) (List<?>) load(EMC_MAPPER_ENTRYPOINT, IEMCMapper.class,
				EMCMapper.class, EMCMapper::priority, EMCMapper::requiredMods,
				mapper -> ((IEMCMapper<?, ?>) mapper).getName(), "EMC Mapper");
	}

	private static <TYPE, ANNOTATION extends Annotation> List<TYPE> load(String entrypoint, Class<TYPE> type,
			Class<ANNOTATION> annotationType, ToIntFunction<ANNOTATION> priorityGetter,
			Function<ANNOTATION, String[]> requiredModsGetter, Function<TYPE, String> namer, String description) {
		List<TYPE> loaded = new ArrayList<>();
		Object2IntMap<TYPE> priorities = new Object2IntOpenHashMap<>();
		for (EntrypointContainer<TYPE> container : FabricLoader.getInstance().getEntrypointContainers(entrypoint, type)) {
			TYPE instance;
			try {
				instance = container.getEntrypoint();
			} catch (Throwable t) {
				PECore.LOGGER.error("Failed to load {} declared by {}", description,
						container.getProvider().getMetadata().getId(), t);
				continue;
			}
			ANNOTATION annotation = instance.getClass().getAnnotation(annotationType);
			if (annotation == null) {
				PECore.LOGGER.error("{} {} is missing its @{} annotation, skipping it.", description,
						instance.getClass().getName(), annotationType.getSimpleName());
				continue;
			} else if (!requiredModsLoaded(instance, requiredModsGetter.apply(annotation))) {
				continue;
			}
			int priority = priorityGetter.applyAsInt(annotation);
			loaded.add(instance);
			priorities.put(instance, priority);
			PECore.debugLog("Found and loaded {}: {}, with priority {}", description, namer.apply(instance), priority);
		}
		loaded.sort(Comparator.comparingInt(priorities::getInt).reversed());
		return loaded;
	}

	private static boolean requiredModsLoaded(Object instance, @Nullable String[] requiredMods) {
		if (requiredMods == null) {
			return true;
		}
		for (String modid : requiredMods) {
			//The annotations default to a single empty string rather than an empty array
			if (!modid.isEmpty() && !PEPlatform.isModLoaded(modid)) {
				PECore.debugLog("Skipped {}, as its required mods ({}) are not loaded.", instance.getClass().getName(),
						Arrays.toString(requiredMods));
				return false;
			}
		}
		return true;
	}
}
