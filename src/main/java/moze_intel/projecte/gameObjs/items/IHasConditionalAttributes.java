package moze_intel.projecte.gameObjs.items;

import java.util.function.BiConsumer;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

/**
 * Implemented by items whose attribute modifiers depend on the stack rather than being fixed, such as a tool
 * whose damage rises with its charge.
 * <p>
 * NeoForge fired an event while gathering a stack's modifiers. Fabric has no such event, so
 * {@code ItemStackAttributesMixin} calls this while vanilla walks the stack's modifiers.
 */
public interface IHasConditionalAttributes {

	/**
	 * Contributes any extra modifiers this stack should have.
	 *
	 * @param stack     The stack being asked about.
	 * @param slotGroup The slots the query covers, so an item only answers for the slots it acts in.
	 * @param consumer  Accepts each extra modifier.
	 */
	void adjustAttributes(ItemStack stack, EquipmentSlotGroup slotGroup, BiConsumer<Holder<Attribute>, AttributeModifier> consumer);
}
