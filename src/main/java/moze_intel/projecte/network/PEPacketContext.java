package moze_intel.projecte.network;

import net.minecraft.world.entity.player.Player;

/**
 * The bit of packet-handling context ProjectE's packets actually need.
 * <p>
 * Fabric hands a different context type to server-side and client-side handlers, while ProjectE's packets share
 * one {@code handle} method. Since all any of them want is the player the packet concerns, this narrows both
 * sides to that.
 */
@FunctionalInterface
public interface PEPacketContext {

	/**
	 * @return On the server, the player the packet came from; on the client, the player receiving it.
	 */
	Player player();
}
