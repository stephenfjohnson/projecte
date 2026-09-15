package moze_intel.projecte.mixin;

import moze_intel.projecte.gameObjs.items.IStackCraftingRemainder;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Leaves ProjectE's reusable ingredients - the philosopher's stone, the amulets, the rings - in the crafting grid
 * with their data intact.
 * <p>
 * Vanilla builds the remainder list from {@code Item#getCraftingRemainingItem}, which is a single item fixed at
 * construction: it cannot name the item itself, and a fresh stack of it would drop the mode, charge and stored EMC
 * the original was carrying. NeoForge asked the item about the stack instead; this restores that by fixing up the
 * list vanilla just built, which covers every recipe type since none of them override the default.
 */
@Mixin(Recipe.class)
public interface RecipeMixin<T extends RecipeInput> {

	@Inject(method = "getRemainingItems", at = @At("RETURN"))
	private void projecte$keepStackData(T input, CallbackInfoReturnable<NonNullList<ItemStack>> cir) {
		NonNullList<ItemStack> remaining = cir.getReturnValue();
		for (int i = 0; i < remaining.size() && i < input.size(); i++) {
			ItemStack ingredient = input.getItem(i);
			if (ingredient.getItem() instanceof IStackCraftingRemainder remainder) {
				remaining.set(i, remainder.getCraftingRemainder(ingredient));
			}
		}
	}
}
