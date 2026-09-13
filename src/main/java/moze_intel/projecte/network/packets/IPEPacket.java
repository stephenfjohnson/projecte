package moze_intel.projecte.network.packets;

import moze_intel.projecte.network.PEPacketContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface IPEPacket extends CustomPacketPayload {

	void handle(PEPacketContext context);
}
