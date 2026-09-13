package moze_intel.projecte;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.ProjectERegistries;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.fluid.FluidStack;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.attachment.PEAttachments;
import moze_intel.projecte.capability.Capabilities;
import moze_intel.projecte.config.CustomEMCParser;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.emc.EMCMappingHandler;
import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.gameObjs.items.IHasConditionalAttributes;
import moze_intel.projecte.gameObjs.registries.PEArmorMaterials;
import moze_intel.projecte.gameObjs.registries.PEAttachmentTypes;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEBlockTypes;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEContainerTypes;
import moze_intel.projecte.gameObjs.registries.PECreativeTabs;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.gameObjs.registries.PENormalizedSimpleStacks;
import moze_intel.projecte.gameObjs.registries.PERecipeConditions;
import moze_intel.projecte.gameObjs.registries.PERecipeSerializers;
import moze_intel.projecte.gameObjs.registries.PESoundEvents;
import moze_intel.projecte.impl.TransmutationOffline;
import moze_intel.projecte.impl.capability.AlchBagImpl;
import moze_intel.projecte.impl.capability.KnowledgeImpl;
import moze_intel.projecte.integration.IntegrationHelper;
import moze_intel.projecte.network.PEPackets;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.ThreadCheckUUID;
import moze_intel.projecte.network.commands.EMCCMD;
import moze_intel.projecte.network.commands.KnowledgeCMD;
import moze_intel.projecte.network.commands.RemoveEmcCMD;
import moze_intel.projecte.network.commands.ResetEmcCMD;
import moze_intel.projecte.network.commands.SetEmcCMD;
import moze_intel.projecte.network.commands.ShowBagCMD;
import moze_intel.projecte.network.packets.to_client.SyncEmcPKT;
import moze_intel.projecte.network.packets.to_client.SyncFuelMapperPKT;
import moze_intel.projecte.network.packets.to_client.SyncWorldTransmutations;
import moze_intel.projecte.utils.ItemAbilities;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.world_transmutation.WorldTransmutationManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class PECore implements ModInitializer {

	public static final String MODID = ProjectEAPI.PROJECTE_MODID;
	public static final String MODNAME = "ProjectE";
	public static final GameProfile FAKEPLAYER_GAMEPROFILE = new GameProfile(UUID.fromString("590e39c7-9fb6-471b-a4c2-c0e539b2423d"), "[" + MODNAME + "]");
	public static final Logger LOGGER = LogUtils.getLogger();

	public static final List<String> uuids = new ArrayList<>();

	public static void debugLog(String msg, Object... args) {
		if (PEPlatform.isDevelopment() || ProjectEConfig.common.debugLogging.get()) {
			LOGGER.info(msg, args);
		} else {
			LOGGER.debug(msg, args);
		}
	}

	public static ResourceLocation rl(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}

	private static PECore instance;

	@Nullable
	private EmcUpdateData emcUpdateResourceManager;
	private final PacketHandler packetHandler;

	@Override
	public void onInitialize() {
		instance = this;

		//Fabric registers entries as soon as it is asked to, so ordering here is the ordering that matters:
		// blocks before the block entities and items that reference them
		PEArmorMaterials.ARMOR_MATERIALS.register();
		PEBlocks.BLOCKS.register();
		PEBlockTypes.BLOCK_TYPES.register();
		PEBlockEntityTypes.BLOCK_ENTITY_TYPES.register();
		PEContainerTypes.CONTAINER_TYPES.register();
		PEDataComponentTypes.DATA_COMPONENT_TYPES.register();
		PEEntityTypes.ENTITY_TYPES.register();
		PEItems.ITEMS.register();
		PENormalizedSimpleStacks.NSS_SERIALIZERS.register();
		PERecipeConditions.register();
		PERecipeSerializers.RECIPE_SERIALIZERS.register();
		PESoundEvents.SOUND_EVENTS.register();
		//Creative tabs go last, as they name the items they display
		PECreativeTabs.CREATIVE_TABS.register();

		ProjectEConfig.register();
		PEPlatform.init();
		PEAttachments.init();
		Capabilities.init();
		registerCapabilities();

		this.packetHandler = new PacketHandler();

		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(WorldTransmutationManager.INSTANCE);
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
			if (success) {
				//Remember what EMC mapping will need, to be done once the sync that follows begins
				emcUpdateResourceManager = new EmcUpdateData(server.getServerResources(), server.registryAccess(), resourceManager);
			}
			WorldHelper.clearCachedAgeProperties();
		});
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(this::dataPackSync);
		ServerLifecycleEvents.SERVER_STARTING.register(this::serverStarting);
		ServerLifecycleEvents.SERVER_STOPPED.register(this::serverQuit);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher, registryAccess));

		commonSetup();
	}

	public static PacketHandler packetHandler() {
		return instance.packetHandler;
	}

	private void registerCapabilities() {
		PECapabilities.ALCH_BAG_CAPABILITY.registerForType((player, context) -> new AlchBagImpl(player), EntityType.PLAYER);
		PECapabilities.KNOWLEDGE_CAPABILITY.registerForType((player, context) -> new KnowledgeImpl(player), EntityType.PLAYER);
	}

	private void commonSetup() {
		EMCMappingHandler.loadMappers();
		{
			//Dispenser Behavior
			registerDispenseBehavior(new ShearsDispenseItemBehavior(), PEItems.DARK_MATTER_SHEARS, PEItems.RED_MATTER_SHEARS, PEItems.RED_MATTER_KATAR);
			DispenserBlock.registerBehavior(PEBlocks.NOVA_CATALYST, PEBlocks.NOVA_CATALYST.getBlock().createDispenseItemBehavior());
			DispenserBlock.registerBehavior(PEBlocks.NOVA_CATACLYSM, PEBlocks.NOVA_CATACLYSM.getBlock().createDispenseItemBehavior());
			registerDispenseBehavior(new OptionalDispenseItemBehavior() {
				@NotNull
				@Override
				protected ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
					//[VanillaCopy] Based off the flint and steel dispense behavior
					if (!ToolActions.canPerformAction(stack, ItemAbilities.FIRESTARTER_LIGHT)) {
						//Only allow using the arcana ring to ignite things when on ignition mode
						setSuccess(false);
						return super.execute(source, stack);
					}
					Level level = source.level();
					setSuccess(true);
					Direction direction = source.state().getValue(DispenserBlock.FACING);
					BlockPos pos = source.pos().relative(direction);
					BlockState state = level.getBlockState(pos);
					if (BaseFireBlock.canBePlacedAt(level, pos, direction)) {
						level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
						level.gameEvent(null, GameEvent.BLOCK_PLACE, pos);
					} else {
						Direction opposite = direction.getOpposite();
						BlockHitResult hitResult = new BlockHitResult(pos.getCenter(), opposite, pos, false);
						UseOnContext context = new UseOnContext(level, null, InteractionHand.MAIN_HAND, stack, hitResult);
						BlockState modifiedState = ToolActions.getModifiedState(state, context, ItemAbilities.FIRESTARTER_LIGHT, false);
						if (modifiedState != null) {
							level.setBlockAndUpdate(pos, modifiedState);
							level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
						} else if (state.isFlammable(level, pos, opposite)) {
							state.onCaughtFire(level, pos, opposite, null);
							if (state.getBlock() instanceof TntBlock) {
								level.removeBlock(pos, false);
							}
						} else {
							setSuccess(false);
						}
					}
					return stack;
				}
			}, PEItems.IGNITION_RING, PEItems.ARCANA_RING);
			DispenserBlock.registerBehavior(PEItems.EVERTIDE_AMULET, new DefaultDispenseItemBehavior() {
				@NotNull
				@Override
				public ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
					//Based off of vanilla's bucket dispense behaviors
					// Note: We only do evertide, not volcanite, as placing lava requires EMC
					Level level = source.level();
					Direction direction = source.state().getValue(DispenserBlock.FACING);
					BlockPos pos = source.pos().relative(direction);
					if (WorldHelper.fillTank(level, pos, direction.getOpposite(), Fluids.WATER, FluidStack.BUCKET_VOLUME)) {
						return stack;
					}
					WorldHelper.placeFluid(null, level, pos, Fluids.WATER, !ProjectEConfig.server.items.opEvertide.get());
					level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), PESoundEvents.WATER_MAGIC.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
					return stack;
				}
			});
		}
	}

	private static void registerDispenseBehavior(DispenseItemBehavior behavior, ItemLike... items) {
		for (ItemLike item : items) {
			DispenserBlock.registerBehavior(item, behavior);
		}
	}

	/**
	 * Sends a joining or reloading player the EMC data they need.
	 * <p>
	 * NeoForge fired this once for everyone on a reload and once per player on join; Fabric fires it per player
	 * either way, so the remap below runs on the first player through and the send is always for one player.
	 */
	private void dataPackSync(ServerPlayer player, boolean joined) {
		if (emcUpdateResourceManager != null) {
			long start = System.currentTimeMillis();
			//Clear the cached created tags
			AbstractNSSTag.clearCreatedTags();
			CustomEMCParser.init(emcUpdateResourceManager.registryAccess());
			try {
				EMCMappingHandler.map(emcUpdateResourceManager.serverResources(), emcUpdateResourceManager.registryAccess(), emcUpdateResourceManager.resourceManager());
				PECore.LOGGER.info("Registered {} EMC values. (took {} ms)", EMCMappingHandler.getEmcMapSize(), System.currentTimeMillis() - start);
			} catch (Throwable t) {
				PECore.LOGGER.error("Error calculating EMC values", t);
			}
			emcUpdateResourceManager = null;
		}
		if (!player.connection.getConnection().isMemoryConnection()) {
			PEPackets.sendTo(player, SyncEmcPKT.serializeEmcData(player.registryAccess()), FuelMapper.getSyncPacket());
			PEPackets.sendTo(player, WorldTransmutationManager.getSyncPacket());
		}
	}

	private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
		dispatcher.register(Commands.literal("projecte")
				.requires(PEPermissions.COMMAND)
				.then(RemoveEmcCMD.register(context))
				.then(ResetEmcCMD.register(context))
				.then(SetEmcCMD.register(context))
				.then(ShowBagCMD.register(context))
				.then(EMCCMD.register(context))
				.then(KnowledgeCMD.register(context))
		);
	}

	private void serverStarting(MinecraftServer server) {
		if (!ThreadCheckUUID.hasRunServer()) {
			new ThreadCheckUUID(true).start();
		}
	}

	private void serverQuit(MinecraftServer server) {
		//Ensure we save any changes to the custom emc file
		CustomEMCParser.flush(server.registryAccess());
		TransmutationOffline.cleanAll();
		EMCMappingHandler.clearEmcMap();
	}

	private record EmcUpdateData(ReloadableServerResources serverResources, RegistryAccess registryAccess, ResourceManager resourceManager) {
	}
}