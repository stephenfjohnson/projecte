package moze_intel.projecte.utils;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Catches what a sheared entity drops, so ProjectE's shears can double the wool and gather it at the player.
 * <p>
 * NeoForge's shearing contract handed the drops back for the caller to place; vanilla's {@link Shearable#shear} spawns
 * them itself. {@code EntityShearDropMixin} funnels those spawns in here while a shear is being collected.
 *
 * @implNote Only ever used from the server thread, from within {@link #collect(Entity, Runnable)}, which always clears
 * up after itself.
 */
public final class ShearCollector {

	@Nullable
	private static Entity shearing;
	@Nullable
	private static List<ItemStack> collected;

	private ShearCollector() {
	}

	/**
	 * Shears an entity, returning what it dropped instead of letting the drops land in the world.
	 */
	public static List<ItemStack> collect(Entity entity, Runnable shear) {
		List<ItemStack> drops = new ArrayList<>();
		Entity previousEntity = shearing;
		List<ItemStack> previousDrops = collected;
		shearing = entity;
		collected = drops;
		try {
			shear.run();
		} finally {
			shearing = previousEntity;
			collected = previousDrops;
		}
		return drops;
	}

	/**
	 * @return True if the stack was collected and should not be spawned into the world.
	 */
	public static boolean capture(Entity entity, ItemStack stack) {
		if (shearing != entity || collected == null) {
			return false;
		}
		if (!stack.isEmpty()) {
			collected.add(stack.copy());
		}
		return true;
	}
}
