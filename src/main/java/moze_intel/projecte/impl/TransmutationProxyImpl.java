package moze_intel.projecte.impl;

import java.util.Objects;
import java.util.UUID;
import moze_intel.projecte.PEPlatform;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.proxy.ITransmutationProxy;
import moze_intel.projecte.client.ClientAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class TransmutationProxyImpl implements ITransmutationProxy {

	@NotNull
	@Override
	public IKnowledgeProvider getKnowledgeProviderFor(@NotNull UUID playerUUID) {
		if (PEPlatform.isServerThread()) {
			Objects.requireNonNull(playerUUID);
			MinecraftServer server = Objects.requireNonNull(PEPlatform.getCurrentServer(), "Server must be running to query knowledge!");
			Player player = server.getPlayerList().getPlayer(playerUUID);
			if (player != null) {
				return Objects.requireNonNull(PECapabilities.KNOWLEDGE_CAPABILITY.find(player, null));
			}
			return TransmutationOffline.forPlayer(server, playerUUID);
		} else if (PEPlatform.isClient()) {
			Player clientPlayer = Objects.requireNonNull(ClientAccess.player(), "Client player doesn't exist!");
			return Objects.requireNonNull(PECapabilities.KNOWLEDGE_CAPABILITY.find(clientPlayer, null));
		}
		throw new IllegalStateException("unreachable");
	}
}