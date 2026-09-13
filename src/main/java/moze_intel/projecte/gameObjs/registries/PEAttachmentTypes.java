package moze_intel.projecte.gameObjs.registries;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import moze_intel.projecte.api.inventory.IItemHandler;
import moze_intel.projecte.api.inventory.IItemHandlerModifiable;
import moze_intel.projecte.attachment.PEAttachmentType;
import moze_intel.projecte.attachment.PEAttachments;
import moze_intel.projecte.impl.capability.AlchBagImpl.AlchemicalBagAttachment;
import moze_intel.projecte.impl.capability.KnowledgeImpl.KnowledgeAttachment;
import moze_intel.projecte.inventory.ItemStackHandler;
import net.minecraft.world.item.ItemStack;

/**
 * The data ProjectE keeps on each player.
 * <p>
 * All three carry over when a player dies, which is what NeoForge's {@code copyOnDeath} did for them.
 */
public class PEAttachmentTypes {

	private PEAttachmentTypes() {
	}

	public static final PEAttachmentType<AlchemicalBagAttachment> ALCHEMICAL_BAGS = PEAttachments.register("alchemical_bags",
			AlchemicalBagAttachment::new, AlchemicalBagAttachment.CODEC,
			(existing, player, registries) -> existing.copy(player, registries));

	public static final PEAttachmentType<KnowledgeAttachment> KNOWLEDGE = PEAttachments.register("knowledge",
			player -> new KnowledgeAttachment(), KnowledgeAttachment.CODEC,
			(existing, player, registries) -> existing.copy(player, registries));

	public static final PEAttachmentType<Boolean> GEM_ARMOR_STATE = PEAttachments.register("gem_armor_state",
			player -> false, Codec.BOOL,
			//Only worth carrying over if it was switched on; otherwise let it default
			(existing, player, registries) -> existing ? Boolean.TRUE : null);

	public static <HANDLER extends IItemHandlerModifiable> HANDLER copyHandler(IItemHandler handler, Int2ObjectFunction<HANDLER> handlerCreator) {
		int slots = handler.getSlots();
		HANDLER handlerCopy = handlerCreator.get(slots);
		for (int i = 0; i < slots; i++) {
			ItemStack stack = handler.getStackInSlot(i);
			if (!stack.isEmpty()) {
				handlerCopy.setStackInSlot(i, stack.copy());
			}
		}
		return handlerCopy;
	}
}
