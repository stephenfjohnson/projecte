package moze_intel.projecte.gameObjs.container;

import io.netty.buffer.Unpooled;
import java.util.function.Consumer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * The extra data sent to the client when a ProjectE menu opens.
 * <p>
 * NeoForge let a menu be opened with a writer that filled a buffer, and handed that same buffer to the menu's
 * constructor on the client. Fabric instead sends a typed payload. Rather than invent a payload per menu, this
 * carries the bytes those writers produce, so every menu's existing buffer-reading constructor works unchanged.
 *
 * @param bytes What the opening side wrote.
 */
public record PEMenuData(byte[] bytes) {

	public static final StreamCodec<RegistryFriendlyByteBuf, PEMenuData> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BYTE_ARRAY, PEMenuData::bytes,
			PEMenuData::new
	);

	/**
	 * Captures what {@code writer} writes, for sending to the client.
	 */
	public static PEMenuData of(Consumer<RegistryFriendlyByteBuf> writer, RegistryAccess registryAccess) {
		RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess);
		writer.accept(buffer);
		byte[] bytes = new byte[buffer.readableBytes()];
		buffer.readBytes(bytes);
		return new PEMenuData(bytes);
	}

	/**
	 * Reads the data back out, for handing to a menu's constructor.
	 */
	public RegistryFriendlyByteBuf toBuffer(RegistryAccess registryAccess) {
		return new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(bytes), registryAccess);
	}
}
