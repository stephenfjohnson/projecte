package moze_intel.projecte.mixin.client;

import moze_intel.projecte.gameObjs.items.IReequipAnimation;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Stops ProjectE's items from replaying the held-item animation every time their stored EMC changes.
 * <p>
 * This comparison is what vanilla uses to notice the held item changed, and it counts any difference in the stack's
 * data, which for these items happens constantly. NeoForge asked the item; this puts that question back.
 */
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

	@Redirect(method = "tick", at = @At(value = "INVOKE",
									   target = "Lnet/minecraft/world/item/ItemStack;matches(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
	private boolean projecte$unchangedForAnimation(ItemStack oldStack, ItemStack newStack) {
		if (oldStack.getItem() instanceof IReequipAnimation item) {
			return !item.shouldCauseReequipAnimation(oldStack, newStack, false);
		}
		return ItemStack.matches(oldStack, newStack);
	}
}
