package moze_intel.projecte.mixin;

import moze_intel.projecte.gameObjs.items.armor.PEArmor.ReductionInfo;
import moze_intel.projecte.gameObjs.items.armor.PEArmor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies the damage reduction of ProjectE's armour.
 * <p>
 * NeoForge let a mod add a reduction to the damage being worked out. Fabric has no such hook, so this extends
 * vanilla's own armour step. Each piece contributes a share of the full set's reduction, capped by how much a
 * single piece can soak up for that kind of damage.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityArmorMixin {

	@Inject(method = "getDamageAfterArmorAbsorb", at = @At("RETURN"), cancellable = true)
	private void projecte$armorReduction(DamageSource source, float damage, CallbackInfoReturnable<Float> cir) {
		float remaining = cir.getReturnValueF();
		if (remaining <= 0) {
			return;
		}
		LivingEntity self = (LivingEntity) (Object) this;
		ReductionInfo reductionInfo = ReductionInfo.ZERO;
		for (ItemStack armorStack : self.getArmorSlots()) {
			if (armorStack.getItem() instanceof PEArmor armorItem) {
				reductionInfo = reductionInfo.add(armorItem.getReductionInfo(source));
			}
		}
		if (reductionInfo.percentReduced() >= 1) {
			//The set covers this damage completely
			cir.setReturnValue(0F);
		} else if (reductionInfo.maxDamagedAbsorbed() > 0 && reductionInfo.percentReduced() > 0) {
			float absorbed = Math.min(remaining * reductionInfo.percentReduced(), reductionInfo.maxDamagedAbsorbed());
			cir.setReturnValue(Math.max(0F, remaining - absorbed));
		}
	}
}
