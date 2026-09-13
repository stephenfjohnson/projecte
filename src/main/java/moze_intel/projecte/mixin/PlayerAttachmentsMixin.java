package moze_intel.projecte.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.IdentityHashMap;
import java.util.Map;
import moze_intel.projecte.PECore;
import moze_intel.projecte.attachment.PEAttachmentHolder;
import moze_intel.projecte.attachment.PEAttachmentType;
import moze_intel.projecte.attachment.PEAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Gives players somewhere to carry ProjectE's attachments, and saves them with the player.
 * <p>
 * NeoForge had a data attachment system for exactly this. Fabric has none, so the storage is added here and
 * hooked into the player's own save and load.
 */
@Mixin(Player.class)
public abstract class PlayerAttachmentsMixin implements PEAttachmentHolder {

	@Unique
	private final Map<PEAttachmentType<?>, Object> projecte$attachments = new IdentityHashMap<>();

	@SuppressWarnings("unchecked")
	@Override
	public <T> T projecte$getAttachment(PEAttachmentType<T> type) {
		Player self = (Player) (Object) this;
		return (T) projecte$attachments.computeIfAbsent(type, key -> type.create(self));
	}

	@Override
	public <T> void projecte$setAttachment(PEAttachmentType<T> type, T value) {
		projecte$attachments.put(type, value);
	}

	@Override
	public Map<PEAttachmentType<?>, Object> projecte$attachments() {
		return projecte$attachments;
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void projecte$saveAttachments(CompoundTag tag, CallbackInfo ci) {
		if (projecte$attachments.isEmpty()) {
			return;
		}
		RegistryOps<Tag> ops = projecte$ops();
		CompoundTag stored = new CompoundTag();
		for (Map.Entry<PEAttachmentType<?>, Object> entry : projecte$attachments.entrySet()) {
			projecte$saveOne(ops, stored, entry.getKey(), entry.getValue());
		}
		if (!stored.isEmpty()) {
			tag.put(PEAttachments.ATTACHMENTS_NBT_KEY, stored);
		}
	}

	@SuppressWarnings("unchecked")
	@Unique
	private <T> void projecte$saveOne(RegistryOps<Tag> ops, CompoundTag stored, PEAttachmentType<T> type, Object value) {
		DataResult<Tag> result = type.codec().encodeStart(ops, (T) value);
		result.resultOrPartial(error -> PECore.LOGGER.error("Failed to save attachment {}: {}", type.getId(), error))
				.ifPresent(encoded -> stored.put(type.getId().toString(), encoded));
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void projecte$loadAttachments(CompoundTag tag, CallbackInfo ci) {
		if (!tag.contains(PEAttachments.ATTACHMENTS_NBT_KEY, Tag.TAG_COMPOUND)) {
			return;
		}
		CompoundTag stored = tag.getCompound(PEAttachments.ATTACHMENTS_NBT_KEY);
		RegistryOps<Tag> ops = projecte$ops();
		for (PEAttachmentType<?> type : PEAttachments.types()) {
			String key = type.getId().toString();
			if (stored.contains(key)) {
				projecte$loadOne(ops, stored.get(key), type);
			}
		}
	}

	@Unique
	private <T> void projecte$loadOne(RegistryOps<Tag> ops, Tag encoded, PEAttachmentType<T> type) {
		type.codec().parse(new Dynamic<>(ops, encoded))
				.resultOrPartial(error -> PECore.LOGGER.error("Failed to load attachment {}: {}", type.getId(), error))
				.ifPresent(value -> projecte$attachments.put(type, value));
	}

	/**
	 * Attachment codecs reference registry entries, so encoding and decoding needs registry access.
	 */
	@Unique
	private RegistryOps<Tag> projecte$ops() {
		Player self = (Player) (Object) this;
		return self.level().registryAccess().createSerializationContext(NbtOps.INSTANCE);
	}
}
