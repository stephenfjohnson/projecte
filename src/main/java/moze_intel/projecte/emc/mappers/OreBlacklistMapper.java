package moze_intel.projecte.emc.mappers;

import java.util.Optional;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.config.PEConfigTranslations;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;

@EMCMapper
public class OreBlacklistMapper implements IEMCMapper<NormalizedSimpleStack, Long> {

	@EMCMapper.Instance
	public static final OreBlacklistMapper INSTANCE = new OreBlacklistMapper();

	@Override
	public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper, ReloadableServerResources serverResources,
			RegistryAccess registryAccess, ResourceManager resourceManager) {
		Optional<Named<Item>> tag = BuiltInRegistries.ITEM.getTag(ConventionalItemTags.ORES);
		if (tag.isPresent()) {
			for (Holder<Item> holder : tag.get()) {
				NSSItem ore = NSSItem.createItem(holder);
				mapper.setValueBefore(ore, 0L);
				mapper.setValueAfter(ore, 0L);
			}
		}
	}

	@Override
	public String getName() {
		return PEConfigTranslations.MAPPING_BLACKLIST_ORE_MAPPER.title();
	}

	@Override
	public String getTranslationKey() {
		return PEConfigTranslations.MAPPING_BLACKLIST_ORE_MAPPER.getTranslationKey();
	}

	@Override
	public String getDescription() {
		return PEConfigTranslations.MAPPING_BLACKLIST_ORE_MAPPER.tooltip();
	}
}