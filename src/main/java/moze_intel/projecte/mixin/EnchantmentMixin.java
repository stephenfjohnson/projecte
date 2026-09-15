package moze_intel.projecte.mixin;

import moze_intel.projecte.gameObjs.items.IUnenchantableItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps enchantments off ProjectE's gear everywhere vanilla asks whether an enchantment may apply.
 * <p>
 * Vanilla's {@code Item#isEnchantable} only guards the enchanting table, so anvils, enchanted books, loot
 * tables and villager trades would all still land enchantments on gear that has no durability to spend and
 * no room for them. NeoForge exposed per-item hooks for those paths; on Fabric they are all reachable
 * through these three, which every applicability check in the game funnels into.
 */
@Mixin(Enchantment.class)
public class EnchantmentMixin {

	@Inject(method = "isPrimaryItem", at = @At("HEAD"), cancellable = true)
	private void projecte$noPrimaryItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (stack.getItem() instanceof IUnenchantableItem) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "isSupportedItem", at = @At("HEAD"), cancellable = true)
	private void projecte$noSupportedItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (stack.getItem() instanceof IUnenchantableItem) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "canEnchant", at = @At("HEAD"), cancellable = true)
	private void projecte$cannotEnchant(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (stack.getItem() instanceof IUnenchantableItem) {
			cir.setReturnValue(false);
		}
	}
}
