package moze_intel.projecte.api.world_transmutation;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import moze_intel.projecte.api.codec.IPECodecHelper;
import moze_intel.projecte.api.codec.PEExtraCodecs;
import moze_intel.projecte.api.codec.WithConditions;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.Nullable;

/**
 * @param comment        Optional comment describing the file.
 * @param transmutations List of world transmutations.
 */
public record WorldTransmutationFile(@Nullable String comment, List<IWorldTransmutation> transmutations) {

	private static final Codec<IWorldTransmutation> TRANSMUTATION_CODEC = Codec.either(
			SimpleWorldTransmutation.CODEC,
			WorldTransmutation.CODEC
	).flatComapMap(Either::unwrap, transmutation -> DataResult.success(switch (transmutation) {
		case SimpleWorldTransmutation simple -> Either.left(simple);
		case WorldTransmutation worldTransmutation -> Either.right(worldTransmutation);
	}));

	private static final Codec<List<IWorldTransmutation>> LIST_CODEC = TRANSMUTATION_CODEC.listOf();
	/**
	 * Codec for serializing and deserializing World Transmutation Files.
	 */
	public static final MapCodec<WorldTransmutationFile> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ExtraCodecs.NON_EMPTY_STRING.optionalFieldOf("comment").forGetter(file -> Optional.ofNullable(file.comment())),
			LIST_CODEC.optionalFieldOf("transmutations").forGetter(file -> IPECodecHelper.INSTANCE.ifNotEmpty(file.transmutations()))
	).apply(instance, (comment, transmutations) ->
			new WorldTransmutationFile(comment.orElse(null), transmutations.orElseGet(Collections::emptyList))));
	public static final Codec<WorldTransmutationFile> CODEC = MAP_CODEC.codec();
	/**
	 * Codec for World Transmutation Files that may carry load conditions. The conditions come back attached to
	 * the decoded file; whoever loads it is responsible for checking them with
	 * {@link WithConditions#conditionsMet(net.minecraft.core.HolderLookup.Provider)}.
	 */
	public static final Codec<WithConditions<WorldTransmutationFile>> CONDITIONAL_CODEC = PEExtraCodecs.withConditions(MAP_CODEC);
}