package moze_intel.projecte.mixin;

import moze_intel.projecte.gameObjs.items.ISweepHitBoxProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Widens the sweep of ProjectE's charged weapons.
 * <p>
 * Vanilla hard-codes how far a sweep reaches around what was hit; NeoForge let the weapon say. This is the one place
 * vanilla works that area out.
 */
@Mixin(Player.class)
public class PlayerSweepMixin {

	@Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"))
	private AABB projecte$sweepHitBox(AABB targetBounds, double x, double y, double z) {
		Player self = (Player) (Object) this;
		ItemStack weapon = self.getMainHandItem();
		if (weapon.getItem() instanceof ISweepHitBoxProvider provider) {
			return provider.getSweepHitBox(weapon, self, targetBounds);
		}
		return targetBounds.inflate(x, y, z);
	}
}
