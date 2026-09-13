package moze_intel.projecte.integration;

import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Implemented by items that should still apply their attribute modifiers while worn in an accessory slot rather
 * than held.
 *
 * @implNote Nothing calls this yet - it is the accessory mod's integration that would, and the Trinkets
 * integration is still to be written. It is kept so the items that have such attributes already say so.
 */
public interface IExposesAccessoryAttributes {

	void addAttributes(Multimap<Holder<Attribute>, AttributeModifier> attributes);
}
