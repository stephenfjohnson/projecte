package moze_intel.projecte.api;

import com.google.common.base.Suppliers;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

/**
 * ProjectE's sound events.
 * <p>
 * These are suppliers rather than plain constants because the class may be loaded before the sounds have been
 * registered; each one resolves against the registry the first time it is asked for, and is cached after that.
 */
public final class PESounds {

	public static final Supplier<SoundEvent> WIND = get("windmagic");
	public static final Supplier<SoundEvent> WATER = get("watermagic");
	public static final Supplier<SoundEvent> POWER = get("power");
	public static final Supplier<SoundEvent> HEAL = get("heal");
	public static final Supplier<SoundEvent> DESTRUCT = get("destruct");
	public static final Supplier<SoundEvent> CHARGE = get("charge");
	public static final Supplier<SoundEvent> UNCHARGE = get("uncharge");
	public static final Supplier<SoundEvent> TRANSMUTE = get("transmute");

	private PESounds() {
	}

	private static Supplier<SoundEvent> get(String name) {
		ResourceKey<SoundEvent> key = ResourceKey.create(Registries.SOUND_EVENT,
				ResourceLocation.fromNamespaceAndPath(ProjectEAPI.PROJECTE_MODID, name));
		return Suppliers.memoize(() -> BuiltInRegistries.SOUND_EVENT.getHolderOrThrow(key).value());
	}
}
