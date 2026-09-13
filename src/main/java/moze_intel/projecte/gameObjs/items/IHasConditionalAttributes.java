package moze_intel.projecte.gameObjs.items;

import java.util.function.BiConsumer;
import net.minecraft.core.Holder;
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
	 * @param stack             The stack being asked about.
	 * @param mainHandQuery     Whether the query covers the main hand, since most of these only apply there.
	 * @param consumer          Accepts each extra modifier.
	 */
	void adjustAttributes(ItemStack stack, boolean mainHandQuery, BiConsumer<Holder<Attribute>, AttributeModifier> consumer);
}
