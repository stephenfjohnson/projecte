package moze_intel.projecte.utils;

import java.util.Map;
import java.util.WeakHashMap;
import moze_intel.projecte.PECore;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * A stand-in player for things ProjectE does with no real player behind them, such as a pedestal firing arrows.
 * <p>
 * NeoForge supplied one of these; Fabric does not, so this is the minimum that serves ProjectE's uses: something
 * that can own an entity and be told apart from a real player. It is deliberately never added to the world and
 * has no connection, so anything that tries to send it a packet will do nothing.
 */
public class PEFakePlayer extends ServerPlayer {

	private static final Map<ServerLevel, PEFakePlayer> BY_LEVEL = new WeakHashMap<>();

	private PEFakePlayer(ServerLevel level) {
		super(level.getServer(), level, PECore.FAKEPLAYER_GAMEPROFILE, ClientInformation.createDefault());
	}

	/**
	 * @return The fake player for this level, creating it the first time it is asked for.
	 */
	public static PEFakePlayer get(ServerLevel level) {
		return BY_LEVEL.computeIfAbsent(level, PEFakePlayer::new);
	}

	@Override
	public Component getDisplayName() {
		return Component.literal(PECore.FAKEPLAYER_GAMEPROFILE.getName());
	}

	@Override
	public void tick() {
		//Never ticked: it is not really in the world
	}

	@Override
	public void die(net.minecraft.world.damagesource.DamageSource source) {
		//Nothing to do, and nothing should be able to kill it
	}

	@Nullable
	@Override
	public Component getTabListDisplayName() {
		//Keep it out of the player list
		return null;
	}
}
