package moze_intel.projecte.events;

import java.util.List;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.api.capabilities.item.IPedestalItem;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.text.PELang;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Adds ProjectE's EMC lines to item tooltips. Registered by the client entrypoint.
 */
public class ToolTipEvent {

	private ToolTipEvent() {
	}

	public static void register() {
		ItemTooltipCallback.EVENT.register(ToolTipEvent::tTipEvent);
	}

	private static void tTipEvent(ItemStack current, Item.TooltipContext tooltipContext, TooltipFlag flag, List<Component> tooltip) {
		if (current.isEmpty()) {
			return;
		}
		if (ProjectEConfig.client.pedestalToolTips.get()) {
			IPedestalItem pedestalItem = PECapabilities.PEDESTAL_ITEM_CAPABILITY.find(current, null);
			if (pedestalItem != null) {
				tooltip.add(PELang.PEDESTAL_ON.translateColored(ChatFormatting.DARK_PURPLE));
				List<Component> description = pedestalItem.getPedestalDescription(tooltipContext.tickRate());
				if (description.isEmpty()) {
					tooltip.add(PELang.PEDESTAL_DISABLED.translateColored(ChatFormatting.RED));
				} else {
					tooltip.addAll(description);
				}
			}
		}

		if (ProjectEConfig.client.tagToolTips.get()) {
			current.getTags().forEach(tag -> tooltip.add(Component.literal("#" + tag.location())));
		}

		if (ProjectEConfig.client.emcToolTips.get() && (!ProjectEConfig.client.shiftEmcToolTips.get() || Screen.hasShiftDown())) {
			long value = IEMCProxy.INSTANCE.getValue(current);
			if (value > 0) {
				tooltip.add(EMCHelper.getEmcTextComponent(value, 1));
				if (current.getCount() > 1) {
					tooltip.add(EMCHelper.getEmcTextComponent(value, current.getCount()));
				}
				Player player = Minecraft.getInstance().player;
				if (player != null && (!ProjectEConfig.client.shiftLearnedToolTips.get() || Screen.hasShiftDown())) {
					IKnowledgeProvider knowledgeProvider = PECapabilities.KNOWLEDGE_CAPABILITY.find(player, null);
					if (knowledgeProvider != null && knowledgeProvider.hasKnowledge(current)) {
						tooltip.add(PELang.EMC_HAS_KNOWLEDGE.translateColored(ChatFormatting.YELLOW));
					} else {
						tooltip.add(PELang.EMC_NO_KNOWLEDGE.translateColored(ChatFormatting.RED));
					}
				}
			}
		}

		long value = current.getOrDefault(PEDataComponentTypes.STORED_EMC.get(), 0L);
		if (value == 0) {
			IItemEmcHolder emcHolder = PECapabilities.EMC_HOLDER_ITEM_CAPABILITY.find(current, null);
			if (emcHolder != null) {
				value = emcHolder.getStoredEmc(current);
			}
		}
		if (value > 0) {
			tooltip.add(PELang.EMC_STORED.translateColored(ChatFormatting.YELLOW, ChatFormatting.WHITE, EMCHelper.formatEmc(value)));
		}
	}
}