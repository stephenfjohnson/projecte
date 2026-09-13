package moze_intel.projecte.mixin.client;

import moze_intel.projecte.events.PlayerRender;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Narrows the field of view while the gem boots are worn.
 * <p>
 * NeoForge had an event for adjusting the field of view; Fabric has none, so vanilla's own calculation is
 * extended.
 */
@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerFovMixin {

	@Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
	private void projecte$adjustFov(CallbackInfoReturnable<Float> cir) {
		cir.setReturnValue(PlayerRender.adjustFovModifier((AbstractClientPlayer) (Object) this, cir.getReturnValueF()));
	}
}
