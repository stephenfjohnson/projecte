package moze_intel.projecte.api;

import com.google.common.base.Suppliers;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * ProjectE's data component types.
 * <p>
 * These are suppliers rather than plain constants because the class may be loaded before the components have
 * been registered; each one resolves against the registry the first time it is asked for, and is cached after
 * that.
 */
public class PEDataComponents {

	public static final Supplier<DataComponentType<Integer>> CHARGE = get("charge");

	private PEDataComponents() {
	}

	@SuppressWarnings("unchecked")
	private static <TYPE> Supplier<DataComponentType<TYPE>> get(String name) {
		ResourceKey<DataComponentType<?>> key = ResourceKey.create(Registries.DATA_COMPONENT_TYPE,
				ResourceLocation.fromNamespaceAndPath(ProjectEAPI.PROJECTE_MODID, name));
		return Suppliers.memoize(() -> (DataComponentType<TYPE>) BuiltInRegistries.DATA_COMPONENT_TYPE.getHolderOrThrow(key).value());
	}
}
