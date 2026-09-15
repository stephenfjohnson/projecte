package moze_intel.projecte.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import moze_intel.projecte.gameObjs.blocks.ProjectETNT;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Makes fire spreading into ProjectE's TNT prime its own entity rather than vanilla's.
 * <p>
 * This is the one ignition path {@code TntBlockMixin} cannot cover: vanilla clears the block before it primes the TNT,
 * so the block has to come from the state fire captured on its way in.
 */
@Mixin(FireBlock.class)
public class FireBlockMixin {

	@Redirect(method = "checkBurnOut", at = @At(value = "INVOKE",
											   target = "Lnet/minecraft/world/level/block/TntBlock;explode(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
	private void projecte$primeOwnEntity(Level level, BlockPos pos, @Local BlockState burned) {
		if (burned.getBlock() instanceof ProjectETNT tnt) {
			tnt.prime(level, pos, null);
		} else {
			TntBlock.explode(level, pos);
		}
	}
}
