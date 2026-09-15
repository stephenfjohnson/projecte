package moze_intel.projecte.mixin;

import moze_intel.projecte.utils.ShearCollector;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Diverts an entity's drops into {@link ShearCollector} while ProjectE is shearing it.
 * <p>
 * Every {@code Shearable} in the game drops its wool, mushrooms or pumpkin through this one method, which is the only
 * place left to catch them: NeoForge asked the entity for its drops and let the caller place them, and Fabric has no
 * equivalent hook. Vanilla's shearing code already copes with this returning null, which is what it does on the client.
 */
@Mixin(Entity.class)
public class EntityShearDropMixin {

	@Inject(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
			at = @At("HEAD"), cancellable = true)
	private void projecte$collectShearDrops(ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir) {
		if (ShearCollector.capture((Entity) (Object) this, stack)) {
			cir.setReturnValue(null);
		}
	}
}
