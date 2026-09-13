package moze_intel.projecte.mixin;

import moze_intel.projecte.PECore;
import moze_intel.projecte.PEPlatform;
import moze_intel.projecte.impl.TransmutationOffline;
import moze_intel.projecte.utils.PEFakePlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drops a player's cached offline transmutation data as soon as a real player object exists for them, so the
 * cached copy cannot be used in place of the data about to be loaded.
 * <p>
 * NeoForge had an entity construction event for this; the constructor is where that fired, so it is hooked here.
 */
@Mixin(Player.class)
public abstract class PlayerOfflineCacheMixin {

	@Inject(method = "<init>", at = @At("TAIL"))
	private void projecte$clearOfflineCache(CallbackInfo ci) {
		Player self = (Player) (Object) this;
		//There is no level to check yet, so go by the thread; fake players have no saved data to speak of
		if (PEPlatform.isServerThread() && !(self instanceof PEFakePlayer)) {
			TransmutationOffline.clear(self.getUUID());
			PECore.debugLog("Clearing offline data cache in preparation to load online data");
		}
	}
}
