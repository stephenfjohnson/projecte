package moze_intel.projecte.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Everything ProjectE needs to ask the running client, kept behind one door.
 * <p>
 * Fabric refuses to load a client-only class on a dedicated server, and a class is checked as a whole: a single
 * mention of {@link Minecraft} in a class the server also loads brings the server down as that class is linked.
 * Common code therefore comes through here, and only while it is already running on the client.
 *
 * @apiNote Every method on this class must only be called from code that is running on the client.
 */
public final class ClientAccess {

	private ClientAccess() {
	}

	/**
	 * @return The player this client is playing as, or {@code null} if there is not one yet.
	 */
	@Nullable
	public static Player player() {
		return Minecraft.getInstance().player;
	}

	/**
	 * @return The level this client is in, or {@code null} if it is not in one.
	 */
	@Nullable
	public static Level level() {
		return Minecraft.getInstance().level;
	}

	/**
	 * @return {@code true} if the given player is this client's own and is holding jump.
	 */
	public static boolean isJumpPressed(Player player) {
		return player instanceof LocalPlayer localPlayer && localPlayer.input.jumping;
	}
}
