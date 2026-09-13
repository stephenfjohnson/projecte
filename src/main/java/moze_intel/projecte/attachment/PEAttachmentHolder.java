package moze_intel.projecte.attachment;

/**
 * Implemented on {@link net.minecraft.world.entity.player.Player} by mixin, so that a player can carry
 * ProjectE's attachments.
 * <p>
 * Reach for {@link PEAttachments} rather than calling this directly; it does the cast for you.
 */
public interface PEAttachmentHolder {

	<T> T projecte$getAttachment(PEAttachmentType<T> type);

	<T> void projecte$setAttachment(PEAttachmentType<T> type, T value);

	/**
	 * @return Every attachment this player currently has a value for, for copying on respawn.
	 */
	java.util.Map<PEAttachmentType<?>, Object> projecte$attachments();
}
