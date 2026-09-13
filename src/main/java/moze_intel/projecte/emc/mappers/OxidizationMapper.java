package moze_intel.projecte.emc.mappers;

import java.util.Map;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.config.PEConfigTranslations;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;

@EMCMapper
public class OxidizationMapper implements IEMCMapper<NormalizedSimpleStack, Long> {

	@Override
	public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper, ReloadableServerResources serverResources,
			RegistryAccess registryAccess, ResourceManager resourceManager) {
		int recipeCount = 0;
		//NeoForge exposed oxidation as a data map; vanilla's own table is the source either way
		for (Map.Entry<Block, Block> entry : WeatheringCopper.NEXT_BY_BLOCK.get().entrySet()) {
			//Add conversions both directions due to scraping
			NSSItem unweathered = NSSItem.createItem(entry.getKey());
			NSSItem weathered = NSSItem.createItem(entry.getValue());
			mapper.addConversion(1, weathered, EMCHelper.intMapOf(unweathered, 1));
			mapper.addConversion(1, unweathered, EMCHelper.intMapOf(weathered, 1));
			recipeCount += 2;
		}
		PECore.debugLog("{} Statistics:", getName());
		PECore.debugLog("Found {} Oxidizable Conversions", recipeCount);
	}

	@Override
	public String getName() {
		return PEConfigTranslations.MAPPING_OXIDATION_MAPPER.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.MAPPING_OXIDATION_MAPPER.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.MAPPING_OXIDATION_MAPPER.tooltip();
	}
}