package moze_intel.projecte.client;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.container.DMFurnaceContainer;
import moze_intel.projecte.gameObjs.entity.EntitySWRGProjectile;
import moze_intel.projecte.gameObjs.gui.AbstractCollectorScreen;
import moze_intel.projecte.gameObjs.gui.AbstractCondenserScreen;
import moze_intel.projecte.gameObjs.gui.AlchBagScreen;
import moze_intel.projecte.gameObjs.gui.AlchChestScreen;
import moze_intel.projecte.gameObjs.gui.GUIDMFurnace;
import moze_intel.projecte.gameObjs.gui.GUIEternalDensity;
import moze_intel.projecte.gameObjs.gui.GUIMercurialEye;
import moze_intel.projecte.gameObjs.gui.GUIRMFurnace;
import moze_intel.projecte.gameObjs.gui.GUIRelay.GUIRelayMK1;
import moze_intel.projecte.gameObjs.gui.GUIRelay.GUIRelayMK2;
import moze_intel.projecte.gameObjs.gui.GUIRelay.GUIRelayMK3;
import moze_intel.projecte.gameObjs.gui.GUITransmutation;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEContainerTypes;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.gameObjs.sound.MovingSoundSWRG;
import moze_intel.projecte.network.commands.client.DumpMissingEmc;
import moze_intel.projecte.rendering.ChestRenderer;
import moze_intel.projecte.rendering.EntitySpriteRenderer;
import moze_intel.projecte.rendering.LayerYue;
import moze_intel.projecte.rendering.PedestalRenderer;
import moze_intel.projecte.rendering.TransmutationRenderingOverlay;
import moze_intel.projecte.utils.ClientKeyHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.client.renderer.entity.TntRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public class PEClient implements ClientModInitializer {

	public static final ResourceLocation ACTIVE_OVERRIDE = PECore.rl("active");
	public static final ResourceLocation MODE_OVERRIDE = PECore.rl("mode");

	@Override
	public void onInitializeClient() {
		//NeoForge had a config screen extension point; that is ModMenu's job on Fabric and ProjectE does not
		// depend on it, so there is no in-game config screen for now.
		registerScreens();
		registerKeybindings();
		registerOverlays();
		registerRenderers();
		addLayers();
		clientSetup();

		PECore.packetHandler().registerClientReceivers();

		ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			Minecraft mc = Minecraft.getInstance();
			if (entity instanceof EntitySWRGProjectile projectile && mc.mouseHandler.isMouseGrabbed()) {
				mc.getSoundManager().play(new MovingSoundSWRG(projectile, level.getRandom()));
			}
		});
		//Swinging at nothing is only known to the client, so the ring asks the server to fire its volley
		ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> {
			if (player.getMainHandItem().is(PEItems.ARCHANGEL_SMITE)) {
				PECore.packetHandler().activateArchangel();
			}
			return false;
		});
		//Note: We can use projecte as the base command here as it will merge the trees properly
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> dispatcher.register(
				ClientCommandManager.literal("projecte")
						.then(DumpMissingEmc.register(context))
		));
	}

	private void registerScreens() {
		MenuScreens.register(PEContainerTypes.RM_FURNACE_CONTAINER.get(), GUIRMFurnace::new);
		MenuScreens.<DMFurnaceContainer, GUIDMFurnace<DMFurnaceContainer>>register(PEContainerTypes.DM_FURNACE_CONTAINER.get(), GUIDMFurnace::new);
		MenuScreens.register(PEContainerTypes.CONDENSER_CONTAINER.get(), AbstractCondenserScreen.MK1::new);
		MenuScreens.register(PEContainerTypes.CONDENSER_MK2_CONTAINER.get(), AbstractCondenserScreen.MK2::new);
		MenuScreens.register(PEContainerTypes.ALCH_CHEST_CONTAINER.get(), AlchChestScreen::new);
		MenuScreens.register(PEContainerTypes.ALCH_BAG_CONTAINER.get(), AlchBagScreen::new);
		MenuScreens.register(PEContainerTypes.ETERNAL_DENSITY_CONTAINER.get(), GUIEternalDensity::new);
		MenuScreens.register(PEContainerTypes.TRANSMUTATION_CONTAINER.get(), GUITransmutation::new);
		MenuScreens.register(PEContainerTypes.RELAY_MK1_CONTAINER.get(), GUIRelayMK1::new);
		MenuScreens.register(PEContainerTypes.RELAY_MK2_CONTAINER.get(), GUIRelayMK2::new);
		MenuScreens.register(PEContainerTypes.RELAY_MK3_CONTAINER.get(), GUIRelayMK3::new);
		MenuScreens.register(PEContainerTypes.COLLECTOR_MK1_CONTAINER.get(), AbstractCollectorScreen.MK1::new);
		MenuScreens.register(PEContainerTypes.COLLECTOR_MK2_CONTAINER.get(), AbstractCollectorScreen.MK2::new);
		MenuScreens.register(PEContainerTypes.COLLECTOR_MK3_CONTAINER.get(), AbstractCollectorScreen.MK3::new);
		MenuScreens.register(PEContainerTypes.MERCURIAL_EYE_CONTAINER.get(), GUIMercurialEye::new);
	}

	private void clientSetup() {
		//The JEI screen-switch listener lived here; it comes back with the JEI integration.
		{
			//Property Overrides
			addPropertyOverrides(ACTIVE_OVERRIDE, (stack, level, entity, seed) -> stack.getOrDefault(PEDataComponentTypes.ACTIVE.get(), false) ? 1F : 0F,
					PEItems.GEM_OF_ETERNAL_DENSITY, PEItems.VOID_RING, PEItems.ARCANA_RING, PEItems.ARCHANGEL_SMITE, PEItems.BLACK_HOLE_BAND, PEItems.BODY_STONE,
					PEItems.HARVEST_GODDESS_BAND, PEItems.IGNITION_RING, PEItems.LIFE_STONE, PEItems.MIND_STONE, PEItems.SOUL_STONE, PEItems.WATCH_OF_FLOWING_TIME,
					PEItems.ZERO_RING);
			addPropertyOverrides(MODE_OVERRIDE, (stack, level, entity, seed) ->
					stack.getOrDefault(PEDataComponentTypes.ARCANA_MODE.get(), PEItems.ARCANA_RING.asItem().getDefaultMode()).ordinal(), PEItems.ARCANA_RING);
			addPropertyOverrides(MODE_OVERRIDE, (stack, level, entity, seed) ->
					stack.getOrDefault(PEDataComponentTypes.SWRG_MODE.get(), PEItems.ARCANA_RING.asItem().getDefaultMode()).ordinal(), PEItems.SWIFTWOLF_RENDING_GALE);
		}
	}

	private void registerKeybindings() {
		ClientKeyHelper.registerKeyBindings();
	}

	private void registerOverlays() {
		//NeoForge could place a layer above a named vanilla one; Fabric's hud callback draws after the whole hud
		TransmutationRenderingOverlay overlay = new TransmutationRenderingOverlay();
		HudRenderCallback.EVENT.register(overlay::render);
	}

	private void registerRenderers() {
		//Block Entity
		BlockEntityRendererRegistry.register(PEBlockEntityTypes.ALCHEMICAL_CHEST.get(), context -> new ChestRenderer<>(context, PECore.rl("textures/block/alchemical_chest.png"), PEBlocks.ALCHEMICAL_CHEST));
		BlockEntityRendererRegistry.register(PEBlockEntityTypes.CONDENSER.get(), context -> new ChestRenderer<>(context, PECore.rl("textures/block/condenser_mk1.png"), PEBlocks.CONDENSER));
		BlockEntityRendererRegistry.register(PEBlockEntityTypes.CONDENSER_MK2.get(), context -> new ChestRenderer<>(context, PECore.rl("textures/block/condenser_mk2.png"), PEBlocks.CONDENSER_MK2));
		BlockEntityRendererRegistry.register(PEBlockEntityTypes.DARK_MATTER_PEDESTAL.get(), PedestalRenderer::new);

		//Entities
		EntityRendererRegistry.register(PEEntityTypes.WATER_PROJECTILE.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/water_orb.png")));
		EntityRendererRegistry.register(PEEntityTypes.LAVA_PROJECTILE.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/lava_orb.png")));
		EntityRendererRegistry.register(PEEntityTypes.MOB_RANDOMIZER.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/randomizer.png")));
		EntityRendererRegistry.register(PEEntityTypes.LENS_PROJECTILE.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/lens_explosive.png")));
		EntityRendererRegistry.register(PEEntityTypes.FIRE_PROJECTILE.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/fireball.png")));
		EntityRendererRegistry.register(PEEntityTypes.SWRG_PROJECTILE.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/lightning.png")));
		EntityRendererRegistry.register(PEEntityTypes.NOVA_CATALYST_PRIMED.get(), TntRenderer::new);
		EntityRendererRegistry.register(PEEntityTypes.NOVA_CATACLYSM_PRIMED.get(), TntRenderer::new);
		EntityRendererRegistry.register(PEEntityTypes.HOMING_ARROW.get(), TippableArrowRenderer::new);
	}

	private void addLayers() {
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((type, renderer, helper, context) -> {
			if (renderer instanceof PlayerRenderer playerRenderer) {
				helper.register(new LayerYue(playerRenderer));
			}
		});
	}

	@SuppressWarnings("deprecation")
	private static void addPropertyOverrides(ResourceLocation override, ClampedItemPropertyFunction propertyGetter, ItemLike... itemProviders) {
		for (ItemLike itemProvider : itemProviders) {
			ItemProperties.register(itemProvider.asItem(), override, propertyGetter);
		}
	}
}