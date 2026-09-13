package moze_intel.projecte.mixin;

import moze_intel.projecte.events.TickEvents;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Runs ProjectE's per-player tick.
 * <p>
 * NeoForge had a player tick event that fired on both sides; the player's own tick is where that came from, so
 * this hooks it directly and keeps the same two-sided behaviour.
 */
@Mixin(Player.class)
public abstract class PlayerTickMixin {

	@Inject(method = "tick", at = @At("TAIL"))
	private void projecte$tick(CallbackInfo ci) {
		TickEvents.playerTick((Player) (Object) this);
	}
}
