package moze_intel.projecte.mixin;

import moze_intel.projecte.events.PlayerEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Gives an alchemical bag with a black hole band in it first refusal on items the player walks over.
 * <p>
 * NeoForge had an item pickup event that could say the normal pickup should not happen. Fabric has none, so this
 * hooks the pickup itself and stops it when the bag swallowed the stack.
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityPickupMixin {

	@Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
	private void projecte$suctionPickup(Player player, CallbackInfo ci) {
		if (PlayerEvents.pickupItem((ItemEntity) (Object) this, player)) {
			//The bag took it, so vanilla should not also pick it up
			ci.cancel();
		}
	}
}
