package moze_intel.projecte.mixin;

import java.util.function.BiConsumer;
import moze_intel.projecte.gameObjs.items.IHasConditionalAttributes;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets ProjectE's items contribute attribute modifiers that depend on the stack, such as a tool whose attack
 * damage rises with its charge.
 * <p>
 * NeoForge fired an event while a stack's modifiers were gathered. Fabric has none, so these hook the two
 * methods vanilla itself walks modifiers through, which covers both damage calculations and tooltips.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackAttributesMixin {

	@Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V", at = @At("TAIL"))
	private void projecte$conditionalAttributes(EquipmentSlotGroup slotGroup, BiConsumer<Holder<Attribute>, AttributeModifier> consumer, CallbackInfo ci) {
		projecte$adjust(consumer, slotGroup == EquipmentSlotGroup.MAINHAND || slotGroup == EquipmentSlotGroup.HAND
									|| slotGroup == EquipmentSlotGroup.ANY);
	}

	@Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At("TAIL"))
	private void projecte$conditionalAttributes(EquipmentSlot slot, BiConsumer<Holder<Attribute>, AttributeModifier> consumer, CallbackInfo ci) {
		projecte$adjust(consumer, slot == EquipmentSlot.MAINHAND);
	}

	@Unique
	private void projecte$adjust(BiConsumer<Holder<Attribute>, AttributeModifier> consumer, boolean mainHandQuery) {
		ItemStack self = (ItemStack) (Object) this;
		if (self.getItem() instanceof IHasConditionalAttributes item) {
			item.adjustAttributes(self, mainHandQuery, consumer);
		}
	}
}
