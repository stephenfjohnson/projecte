package moze_intel.projecte.attachment;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * A piece of ProjectE's own data that rides along on a player and is saved with them.
 * <p>
 * This stands in for NeoForge's data attachments, which Fabric has no counterpart for. Only players carry these,
 * so the type is narrowed to them rather than being general.
 *
 * @param <T> The data being attached.
 */
public final class PEAttachmentType<T> {

	private final ResourceLocation id;
	private final Function<Player, T> factory;
	private final Codec<T> codec;
	private final CopyHandler<T> copyHandler;

	PEAttachmentType(ResourceLocation id, Function<Player, T> factory, Codec<T> codec, CopyHandler<T> copyHandler) {
		this.id = id;
		this.factory = factory;
		this.codec = codec;
		this.copyHandler = copyHandler;
	}

	public ResourceLocation getId() {
		return id;
	}

	public Codec<T> codec() {
		return codec;
	}

	/**
	 * Builds the value a player starts with when they have none saved.
	 */
	public T create(Player player) {
		return factory.apply(player);
	}

	/**
	 * Works out what carries over to the player's new body when they respawn.
	 *
	 * @return The value to carry over, or {@code null} to start fresh.
	 */
	@Nullable
	public T copy(T existing, Player player, HolderLookup.Provider registries) {
		return copyHandler.copy(existing, player, registries);
	}

	@FunctionalInterface
	public interface CopyHandler<T> {

		@Nullable
		T copy(T existing, Player player, HolderLookup.Provider registries);
	}
}
