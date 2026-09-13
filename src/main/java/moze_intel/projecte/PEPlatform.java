package moze_intel.projecte;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

/**
 * Answers the questions NeoForge answered through FMLEnvironment, ModList and ServerLifecycleHooks.
 */
public final class PEPlatform {

	@Nullable
	private static MinecraftServer currentServer;

	private PEPlatform() {
	}

	/**
	 * Starts tracking the running server, so that {@link #getCurrentServer()} can answer later. Called once
	 * during mod initialisation.
	 */
	public static void init() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> currentServer = server);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> currentServer = null);
	}

	/**
	 * @return The running server, or {@code null} if none is running. On a client this is the integrated server,
	 * and so is {@code null} outside a singleplayer world.
	 */
	@Nullable
	public static MinecraftServer getCurrentServer() {
		return currentServer;
	}

	/**
	 * @return {@code true} if this is a client installation, whether or not a world is open. This says nothing
	 * about which thread is running.
	 */
	public static boolean isClient() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
	}

	/**
	 * @return {@code true} if this is a dedicated server installation.
	 */
	public static boolean isDedicatedServer() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
	}

	/**
	 * @return {@code true} when running from a development environment rather than a packaged install.
	 */
	public static boolean isDevelopment() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	/**
	 * Whether the calling thread is the server thread.
	 * <p>
	 * Stands in for NeoForge's EffectiveSide, which reported which logical side the current thread belonged to.
	 * Fabric has nothing equivalent, so this asks the running server directly.
	 */
	public static boolean isServerThread() {
		MinecraftServer server = currentServer;
		return server != null && server.isSameThread();
	}

	public static boolean isModLoaded(String modid) {
		return FabricLoader.getInstance().isModLoaded(modid);
	}
}
