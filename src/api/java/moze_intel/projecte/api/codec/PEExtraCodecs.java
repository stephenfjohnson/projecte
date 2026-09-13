package moze_intel.projecte.api.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;

/**
 * Codec helpers that NeoForge used to supply and that have no counterpart in vanilla or Fabric API.
 */
public final class PEExtraCodecs {

	private PEExtraCodecs() {
	}

	/**
	 * Builds a codec that decodes with {@code main}, falling back to {@code alternative} if that fails, and
	 * always encodes with {@code main}.
	 * <p>
	 * This is how ProjectE accepts both a shorthand and a longhand spelling of the same value in its data
	 * files while only ever writing the canonical one back out.
	 */
	public static <T> Codec<T> withAlternative(Codec<T> main, Codec<? extends T> alternative) {
		return Codec.either(main, alternative)
				.xmap(either -> either.map(Function.identity(), Function.identity()), Either::left);
	}

	/**
	 * {@link #withAlternative(Codec, Codec)} for codecs that contribute fields to a surrounding object rather
	 * than standing alone.
	 */
	public static <T> MapCodec<T> withAlternative(MapCodec<T> main, MapCodec<? extends T> alternative) {
		return Codec.mapEither(main, alternative)
				.xmap(either -> either.map(Function.identity(), Function.identity()), Either::left);
	}

	/**
	 * Wraps a codec so that the object it reads and writes may also carry Fabric load conditions, under the
	 * usual {@code fabric:load_conditions} key.
	 * <p>
	 * The conditions are carried rather than acted on here: whoever loads the file evaluates them, since
	 * deciding whether a condition holds needs access to the registries and a codec has none.
	 */
	public static <T> Codec<WithConditions<T>> withConditions(MapCodec<T> valueCodec) {
		return RecordCodecBuilder.<WithConditions<T>>mapCodec(instance -> instance.group(
				ResourceCondition.LIST_CODEC.optionalFieldOf(ResourceConditions.CONDITIONS_KEY, List.of())
						.forGetter((WithConditions<T> withConditions) -> withConditions.conditions()),
				valueCodec.forGetter((WithConditions<T> withConditions) -> withConditions.carrier())
		).apply(instance, (conditions, carrier) -> new WithConditions<>(conditions, carrier))).codec();
	}
}
