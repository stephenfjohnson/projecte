package moze_intel.projecte.config;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import moze_intel.projecte.PECore;
import net.fabricmc.loader.api.FabricLoader;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;

/**
 * ProjectE's configs.
 * <p>
 * Forge Config API Port supplies NeoForge's config API on Fabric under its original package names, so the config
 * definitions themselves needed no changes at all. Only registration and the load callbacks differ, since they
 * went through the mod container and the mod event bus on NeoForge.
 */
public class ProjectEConfig {

	public static final Path CONFIG_DIR = configDir();
	private static final Map<IConfigSpec, IPEConfig> KNOWN_CONFIGS = new HashMap<>();

	public static final ServerConfig server = new ServerConfig();
	public static final CommonConfig common = new CommonConfig();
	public static final ClientConfig client = new ClientConfig();

	private static Path configDir() {
		Path dir = FabricLoader.getInstance().getConfigDir().resolve(PECore.MODNAME);
		try {
			Files.createDirectories(dir);
		} catch (IOException e) {
			throw new UncheckedIOException("Could not create ProjectE's config directory at " + dir, e);
		}
		return dir;
	}

	public static void register() {
		registerConfig(server);
		registerConfig(common);
		registerConfig(client);
		//Listen to the initial load as well as reloads, to fix up any values cached before loading finished
		NeoForgeModConfigEvents.loading(PECore.MODID).register(config -> onConfigChanged(config, false));
		NeoForgeModConfigEvents.reloading(PECore.MODID).register(config -> onConfigChanged(config, false));
		NeoForgeModConfigEvents.unloading(PECore.MODID).register(config -> onConfigChanged(config, true));
	}

	public static Collection<IPEConfig> getConfigs() {
		return Collections.unmodifiableCollection(KNOWN_CONFIGS.values());
	}

	/**
	 * Registers a config and tracks it, so that cached values can be cleared when it changes on disk.
	 */
	public static void registerConfig(IPEConfig config) {
		NeoForgeConfigRegistry.INSTANCE.register(PECore.MODID, config.getConfigType(), config.getConfigSpec(),
				PECore.MODNAME + "/" + config.getFileName() + ".toml");
		KNOWN_CONFIGS.put(config.getConfigSpec(), config);
	}

	private static void onConfigChanged(ModConfig config, boolean unloading) {
		IPEConfig peConfig = KNOWN_CONFIGS.get(config.getSpec());
		if (peConfig != null) {
			peConfig.clearCache(unloading);
		}
	}
}
