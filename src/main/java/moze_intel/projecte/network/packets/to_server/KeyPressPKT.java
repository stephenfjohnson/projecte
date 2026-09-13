package moze_intel.projecte.network.packets.to_server;

import io.netty.buffer.ByteBuf;
import java.util.Optional;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.item.IExtraFunction;
import moze_intel.projecte.api.capabilities.item.IItemCharge;
import moze_intel.projecte.api.capabilities.item.IModeChanger;
import moze_intel.projecte.api.capabilities.item.IProjectileShooter;
import moze_intel.projecte.api.inventory.IItemHandler;
import moze_intel.projecte.api.inventory.IItemHandlerModifiable;
import moze_intel.projecte.api.item.ITransmutationTablet;
import moze_intel.projecte.attachment.PEAttachments;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.container.TransmutationContainer;
import moze_intel.projecte.gameObjs.items.armor.GemArmorBase;
import moze_intel.projecte.gameObjs.items.armor.GemChest;
import moze_intel.projecte.gameObjs.items.armor.GemFeet;
import moze_intel.projecte.gameObjs.items.armor.GemHelmet;
import moze_intel.projecte.gameObjs.registries.PEAttachmentTypes;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.integration.IntegrationHelper;
import moze_intel.projecte.network.PEPacketContext;
import moze_intel.projecte.network.packets.IPEPacket;
import moze_intel.projecte.utils.PEKeybind;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.text.ILangEntry;
import moze_intel.projecte.utils.text.PELang;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record KeyPressPKT(PEKeybind key) implements IPEPacket {

	public static final CustomPacketPayload.Type<KeyPressPKT> TYPE = new CustomPacketPayload.Type<>(PECore.rl("key_press"));
	public static final StreamCodec<ByteBuf, KeyPressPKT> STREAM_CODEC = PEKeybind.STREAM_CODEC.map(KeyPressPKT::new, KeyPressPKT::key);

	@NotNull
	@Override
	public CustomPacketPayload.Type<KeyPressPKT> type() {
		return TYPE;
	}

	@Override
	public void handle(PEPacketContext context) {
		Player player = context.player();
		if (player.isSpectator()) {
			return;
		}
		if (key == PEKeybind.HELMET_TOGGLE) {
			ItemStack helm = player.getItemBySlot(EquipmentSlot.HEAD);
			if (!helm.isEmpty() && helm.is(PEItems.GEM_HELMET)) {
				GemHelmet.toggleNightVision(helm, player);
			}
			return;
		} else if (key == PEKeybind.BOOTS_TOGGLE) {
			ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
			if (!boots.isEmpty() && boots.is(PEItems.GEM_BOOTS)) {
				GemFeet.toggleStepAssist(boots, player);
			}
			return;
		} else if (key == PEKeybind.TRANSMUTATION_TABLET) {
            if (!(player instanceof ServerPlayer)) return;
            //Looks for a tablet worn in an accessory slot. Nothing provides this lookup until the Trinkets
            // integration is written, so for now this finds nothing and the keybind does nothing.
            IItemHandler accessories = IntegrationHelper.ACCESSORY_ITEM_HANDLER.find(player, null);
            if (accessories == null) return;
            for (int i = 0; i < accessories.getSlots(); i++) {
                ItemStack stack = accessories.getStackInSlot(i);
                if (stack.getItem() instanceof ITransmutationTablet tablet) {
                    tablet.openContainer(player);
                    break;
                }
            }
        }
		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack stack = player.getItemInHand(hand);
			switch (key) {
				case CHARGE -> {
					if (tryPerformCapability(player, stack, hand, PECapabilities.CHARGE_ITEM_CAPABILITY, IItemCharge::changeCharge)) {
						return;
					} else if (hand == InteractionHand.MAIN_HAND && isSafe(stack) && GemArmorBase.hasAnyPiece(player)) {
						PEAttachments.set(player, PEAttachmentTypes.GEM_ARMOR_STATE, !PEAttachments.get(player, PEAttachmentTypes.GEM_ARMOR_STATE));
						ILangEntry langEntry = PEAttachments.get(player, PEAttachmentTypes.GEM_ARMOR_STATE) ? PELang.GEM_ACTIVATE : PELang.GEM_DEACTIVATE;
						player.sendSystemMessage(langEntry.translate());
						return;
					}
				}
				case EXTRA_FUNCTION -> {
					if (tryPerformCapability(player, stack, hand, PECapabilities.EXTRA_FUNCTION_ITEM_CAPABILITY, IExtraFunction::doExtraFunction)) {
						return;
					} else if (hand == InteractionHand.MAIN_HAND && isSafe(stack) && PEAttachments.get(player, PEAttachmentTypes.GEM_ARMOR_STATE)) {
						ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
						if (!chestplate.isEmpty() && chestplate.is(PEItems.GEM_CHESTPLATE) &&
							PlayerHelper.checkCooldown(player, PEItems.GEM_CHESTPLATE.get(), ProjectEConfig.server.cooldown.player.gemChest)) {
							GemChest.doExplode(player);
							return;
						}
					}
				}
				case FIRE_PROJECTILE -> {
					if (!stack.isEmpty() && PlayerHelper.checkCooldown(player, stack.getItem(), ProjectEConfig.server.cooldown.player.projectile)
						&& tryPerformCapability(player, stack, hand, PECapabilities.PROJECTILE_SHOOTER_ITEM_CAPABILITY, IProjectileShooter::shootProjectile)) {
						PlayerHelper.swingItem(player, hand);
					}
					if (hand == InteractionHand.MAIN_HAND && isSafe(stack) && PEAttachments.get(player, PEAttachmentTypes.GEM_ARMOR_STATE)) {
						ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
						if (!helmet.isEmpty() && helmet.is(PEItems.GEM_HELMET)) {
							GemHelmet.doZap(player);
							return;
						}
					}
				}
				case MODE -> {
					if (tryPerformCapability(player, stack, hand, PECapabilities.MODE_CHANGER_ITEM_CAPABILITY, IModeChanger::changeMode)) {
						return;
					}
				}
			}
		}
	}

	private static <CAPABILITY> boolean tryPerformCapability(Player player, ItemStack stack, InteractionHand hand, ItemApiLookup<CAPABILITY, Void> capability,
			CapabilityProcessor<CAPABILITY> processor) {
		CAPABILITY impl = stack.getCapability(capability);
		return impl != null && processor.process(impl, player, stack, hand);
	}

	private static boolean isSafe(ItemStack stack) {
		return ProjectEConfig.server.misc.unsafeKeyBinds.get() || stack.isEmpty();
	}

	@FunctionalInterface
	private interface CapabilityProcessor<CAPABILITY> {

		boolean process(CAPABILITY capability, Player player, ItemStack stack, InteractionHand hand);
	}

    private static class TransmutationTabletContainerProvider implements MenuProvider {
		@Override
		public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player player) {
			return new TransmutationContainer(windowId, playerInventory);
		}

		@NotNull
		@Override
		public Component getDisplayName() {
			return PELang.TRANSMUTATION_TRANSMUTE.translate();
		}
	}
}