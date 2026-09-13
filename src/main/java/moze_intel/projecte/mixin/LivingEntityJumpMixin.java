package moze_intel.projecte.mixin;

import moze_intel.projecte.gameObjs.items.armor.GemLegs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Notes when a player jumps, so the gem leggings can tell a jump from a fall.
 * <p>
 * NeoForge had a living jump event; Fabric has none, so the vanilla jump is hooked directly.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityJumpMixin {

	@Inject(method = "jumpFromGround", at = @At("TAIL"))
	private void projecte$onJump(CallbackInfo ci) {
		if ((Object) this instanceof Player player) {
			GemLegs.onJump(player);
		}
	}
}
