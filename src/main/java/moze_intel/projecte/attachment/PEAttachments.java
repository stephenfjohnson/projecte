package moze_intel.projecte.attachment;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Declares ProjectE's player attachments and provides the access that NeoForge put directly on the entity.
 */
public final class PEAttachments {

	/**
	 * The compound inside a player's saved data that holds all of ProjectE's attachments, keyed by attachment
	 * id. The offline readers in {@code ShowBagCMD} and {@code TransmutationOffline} read the same layout.
	 */
	public static final String ATTACHMENTS_NBT_KEY = "projecte:attachments";

	private static final List<PEAttachmentType<?>> TYPES = new ArrayList<>();

	private PEAttachments() {
	}

	/**
	 * Declares an attachment.
	 *
	 * @param name        Path of this attachment's id, under ProjectE's namespace.
	 * @param factory     Builds the starting value for a player who has none saved.
	 * @param codec       How the value is saved and loaded.
	 * @param copyHandler What carries over when the player respawns; returning {@code null} starts fresh.
	 */
	public static <T> PEAttachmentType<T> register(String name, Function<Player, T> factory, Codec<T> codec,
			PEAttachmentType.CopyHandler<T> copyHandler) {
		PEAttachmentType<T> type = new PEAttachmentType<>(ResourceLocation.fromNamespaceAndPath("projecte", name),
				factory, codec, copyHandler);
		TYPES.add(type);
		return type;
	}

	/**
	 * @return Every declared attachment, in declaration order.
	 */
	public static List<PEAttachmentType<?>> types() {
		return List.copyOf(TYPES);
	}

	/**
	 * @return The player's value for this attachment, creating the starting value if they have none.
	 */
	public static <T> T get(Player player, PEAttachmentType<T> type) {
		return ((PEAttachmentHolder) player).projecte$getAttachment(type);
	}

	public static <T> void set(Player player, PEAttachmentType<T> type, T value) {
		((PEAttachmentHolder) player).projecte$setAttachment(type, value);
	}

	/**
	 * Starts carrying attachments across respawns. Called once during mod initialisation.
	 */
	public static void init() {
		//Every ProjectE attachment survives death, so copy on both kinds of respawn and let each
		// attachment's own copy handler decide what carries over
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> copyOnRespawn(oldPlayer, newPlayer));
	}

	/**
	 * Carries attachments from a player's old body to their new one after a respawn.
	 */
	public static void copyOnRespawn(Player from, Player to) {
		HolderLookup.Provider registries = to.level().registryAccess();
		for (PEAttachmentType<?> type : TYPES) {
			copyOne(type, from, to, registries);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> void copyOne(PEAttachmentType<T> type, Player from, Player to, HolderLookup.Provider registries) {
		Object existing = ((PEAttachmentHolder) from).projecte$attachments().get(type);
		if (existing == null) {
			//Nothing was ever set on the old body, so there is nothing to carry over
			return;
		}
		@Nullable T copied = type.copy((T) existing, to, registries);
		if (copied != null) {
			set(to, type, copied);
		}
	}
}
