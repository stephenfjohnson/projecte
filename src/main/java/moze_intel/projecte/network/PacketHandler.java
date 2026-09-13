package moze_intel.projecte.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.gameObjs.items.rings.ArchangelSmite;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.network.packets.IPEPacket;
import moze_intel.projecte.network.packets.to_client.NovaExplosionSyncPKT;
import moze_intel.projecte.network.packets.to_client.SyncEmcPKT;
import moze_intel.projecte.network.packets.to_client.SyncFuelMapperPKT;
import moze_intel.projecte.network.packets.to_client.SyncWorldTransmutations;
import moze_intel.projecte.network.packets.to_client.alch_bag.SyncAllBagDataPKT;
import moze_intel.projecte.network.packets.to_client.alch_bag.SyncBagsDataPKT;
import moze_intel.projecte.network.packets.to_client.container.SyncOffhandPkt;
import moze_intel.projecte.network.packets.to_client.container.UpdateCondenserLockPKT;
import moze_intel.projecte.network.packets.to_client.container.UpdateWindowLongPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncChangePKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncEmcPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncInputsAndLocksPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncPKT;
import moze_intel.projecte.network.packets.to_server.KeyPressPKT;
import moze_intel.projecte.network.packets.to_server.SearchUpdatePKT;
import moze_intel.projecte.network.packets.to_server.UpdateGemModePKT;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Heavily based off of Mekanism's packet handler
 */
public final class PacketHandler {

	/**
	 * Types of packets bound for the client, kept so their handlers can be attached once the client starts.
	 */
	private final List<CustomPacketPayload.Type<? extends IPEPacket>> toClientPackets = new ArrayList<>();

	//Client to server instanced packets
	private SimplePacketPayLoad activateArchangel;

	//Server to client instanced packets
	private SimplePacketPayLoad clearKnowledge;
	private SimplePacketPayLoad updateTransmutationTargets;

	private SimplePacketPayLoad resetCooldown;

	/**
	 * Declares every payload's type and codec, and hooks up the handlers for packets travelling to the server.
	 * <p>
	 * Both sides have to agree on the types, so this runs during common initialisation. Handlers for packets
	 * travelling to the client are client-only and are registered separately by
	 * {@link #registerClientReceivers()}.
	 */
	public PacketHandler() {
		registerClientToServer(new PacketRegistrar(true));
		registerServerToClient(new PacketRegistrar(false));
	}

	private void registerClientToServer(PacketRegistrar registrar) {
		registrar.play(KeyPressPKT.TYPE, KeyPressPKT.STREAM_CODEC);
		activateArchangel = registrar.playInstanced(PECore.rl("activate_archangel"), context -> {
			Player player = context.player();
			ItemStack main = player.getMainHandItem();
			if (!main.isEmpty() && main.is(PEItems.ARCHANGEL_SMITE)) {
				ArchangelSmite.fireVolley(main, player);
			}
		});
		registrar.play(SearchUpdatePKT.TYPE, SearchUpdatePKT.STREAM_CODEC);
		registrar.play(UpdateGemModePKT.TYPE, UpdateGemModePKT.STREAM_CODEC);
	}

	private void registerServerToClient(PacketRegistrar registrar) {
		resetCooldown = registrar.playInstanced(PECore.rl("reset_cooldown"), context -> context.player().resetAttackStrengthTicker());
		clearKnowledge = registrar.playInstanced(PECore.rl("clear_knowledge"), PacketHandler::clearKnowledgeHandler);
		registrar.play(KnowledgeSyncPKT.TYPE, KnowledgeSyncPKT.STREAM_CODEC);
		registrar.play(KnowledgeSyncEmcPKT.TYPE, KnowledgeSyncEmcPKT.STREAM_CODEC);
		registrar.play(KnowledgeSyncInputsAndLocksPKT.TYPE, KnowledgeSyncInputsAndLocksPKT.STREAM_CODEC);
		registrar.play(KnowledgeSyncChangePKT.TYPE, KnowledgeSyncChangePKT.STREAM_CODEC);
		registrar.play(NovaExplosionSyncPKT.TYPE, NovaExplosionSyncPKT.STREAM_CODEC);
		registrar.play(SyncAllBagDataPKT.TYPE, SyncAllBagDataPKT.STREAM_CODEC);
		registrar.play(SyncBagsDataPKT.TYPE, SyncBagsDataPKT.STREAM_CODEC);
		registrar.play(SyncEmcPKT.TYPE, SyncEmcPKT.STREAM_CODEC);
		registrar.play(SyncOffhandPkt.TYPE, SyncOffhandPkt.STREAM_CODEC);
		registrar.play(SyncFuelMapperPKT.TYPE, SyncFuelMapperPKT.STREAM_CODEC);
		registrar.play(SyncWorldTransmutations.TYPE, SyncWorldTransmutations.STREAM_CODEC);
		registrar.play(UpdateCondenserLockPKT.TYPE, UpdateCondenserLockPKT.STREAM_CODEC);
		updateTransmutationTargets = registrar.playInstanced(PECore.rl("update_transmutation_targets"), PacketHandler::updateTargetsHandler);
		registrar.play(UpdateWindowLongPKT.TYPE, UpdateWindowLongPKT.STREAM_CODEC);
	}

	/**
	 * Hooks up the handlers for packets travelling to the client. Called from client initialisation only, as the
	 * handlers touch client-only code.
	 */
	public void registerClientReceivers() {
		ClientPlayNetworking.registerGlobalReceiver(clearKnowledge.type(), (payload, context) -> clearKnowledgeHandler(context::player));
		ClientPlayNetworking.registerGlobalReceiver(updateTransmutationTargets.type(), (payload, context) -> updateTargetsHandler(context::player));
		ClientPlayNetworking.registerGlobalReceiver(resetCooldown.type(), (payload, context) -> context.player().resetAttackStrengthTicker());
		for (CustomPacketPayload.Type<? extends IPEPacket> type : toClientPackets) {
			registerClientHandler(type);
		}
	}

	@SuppressWarnings("unchecked")
	private static <MSG extends IPEPacket> void registerClientHandler(CustomPacketPayload.Type<? extends IPEPacket> type) {
		ClientPlayNetworking.registerGlobalReceiver((CustomPacketPayload.Type<MSG>) type,
				(payload, context) -> payload.handle(context::player));
	}

	public void clearKnowledge(ServerPlayer player) {
		ServerPlayNetworking.send(player, clearKnowledge);
	}

	public void updateTransmutationTargets(ServerPlayer player) {
		ServerPlayNetworking.send(player, updateTransmutationTargets);
	}

	public void resetCooldown(ServerPlayer player) {
		ServerPlayNetworking.send(player, resetCooldown);
	}

	public void activateArchangel() {
		ClientPlayNetworking.send(activateArchangel);
	}

	protected record SimplePacketPayLoad(CustomPacketPayload.Type<CustomPacketPayload> type) implements CustomPacketPayload {

		private SimplePacketPayLoad(ResourceLocation id) {
			this(new CustomPacketPayload.Type<>(id));
		}
	}

	protected class PacketRegistrar {

		private final boolean toServer;

		PacketRegistrar(boolean toServer) {
			this.toServer = toServer;
		}

		public <MSG extends IPEPacket> void play(CustomPacketPayload.Type<MSG> type, StreamCodec<? super RegistryFriendlyByteBuf, MSG> reader) {
			if (toServer) {
				PayloadTypeRegistry.playC2S().register(type, reader);
				//Handling a packet from a client happens on the server, so it can be hooked up right away
				ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> payload.handle(context::player));
			} else {
				PayloadTypeRegistry.playS2C().register(type, reader);
				//The handler runs on the client, so only note the type here and hook it up from client init
				toClientPackets.add(type);
			}
		}

		/**
		 * Registers a payload that carries nothing but its own identity, where the handler is the whole message.
		 */
		public SimplePacketPayLoad playInstanced(ResourceLocation id, Consumer<PEPacketContext> handler) {
			SimplePacketPayLoad payload = new SimplePacketPayLoad(id);
			if (toServer) {
				PayloadTypeRegistry.playC2S().register(payload.type(), StreamCodec.unit(payload));
				ServerPlayNetworking.registerGlobalReceiver(payload.type(), (received, context) -> handler.accept(context::player));
			} else {
				//Client-side handlers for these are wired up by name in registerClientReceivers
				PayloadTypeRegistry.playS2C().register(payload.type(), StreamCodec.unit(payload));
			}
			return payload;
		}
	}

	private static void clearKnowledgeHandler(PEPacketContext context) {
		Player player = context.player();
		IKnowledgeProvider knowledge = PECapabilities.KNOWLEDGE_CAPABILITY.find(player, null);
		if (knowledge != null) {
			knowledge.clearKnowledge();
			if (player.containerMenu instanceof TransmutationContainer container) {
				container.transmutationInventory.updateClientTargets(false);
			}
		}
	}

	private static void updateTargetsHandler(PEPacketContext context) {
		if (context.player().containerMenu instanceof TransmutationContainer container) {
			container.transmutationInventory.updateClientTargets(false);
		}
	}
}
