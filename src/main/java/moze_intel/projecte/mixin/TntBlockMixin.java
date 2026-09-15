package moze_intel.projecte.mixin;

import moze_intel.projecte.gameObjs.blocks.ProjectETNT;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TntBlock;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes ProjectE's TNT blocks prime their own entity rather than vanilla's.
 * <p>
 * Redstone, flint and steel, a flaming arrow and breaking unstable TNT all reach this one method, and each of them
 * still has the block in the world when it does, so the block itself says what to spawn. NeoForge let the block
 * override the ignition hook directly; vanilla hard-codes its own primed TNT.
 */
@Mixin(TntBlock.class)
public class TntBlockMixin {

	@Inject(method = "explode(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/LivingEntity;)V",
			at = @At("HEAD"), cancellable = true)
	private static void projecte$primeOwnEntity(Level level, BlockPos pos, @Nullable LivingEntity igniter, CallbackInfo ci) {
		if (level.getBlockState(pos).getBlock() instanceof ProjectETNT tnt) {
			tnt.prime(level, pos, igniter);
			ci.cancel();
		}
	}
}
