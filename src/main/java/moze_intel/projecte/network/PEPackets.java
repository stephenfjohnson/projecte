package moze_intel.projecte.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * Sending helpers, standing in for NeoForge's PacketDistributor.
 */
public final class PEPackets {

	private PEPackets() {
	}

	/**
	 * Sends one or more payloads to a single player. Fabric sends one payload per call, so this keeps the call
	 * sites that had several to send from having to loop.
	 */
	public static void sendTo(ServerPlayer player, CustomPacketPayload... payloads) {
		for (CustomPacketPayload payload : payloads) {
			ServerPlayNetworking.send(player, payload);
		}
	}

	/**
	 * Sends a payload to the server.
	 *
	 * @apiNote Client only - there is no server to send to from a server.
	 */
	public static void sendToServer(CustomPacketPayload payload) {
		ClientPlayNetworking.send(payload);
	}
}
