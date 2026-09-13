package moze_intel.projecte.events;

import moze_intel.projecte.gameObjs.registries.PEItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

/**
 * Adjusts what the player sees based on the ProjectE gear they are wearing.
 */
public class PlayerRender {

	private PlayerRender() {
	}

	/**
	 * Narrows the field of view while the gem boots are worn, matching how sprinting does.
	 * <p>
	 * Called from {@code AbstractClientPlayerFovMixin}, in place of NeoForge's field of view event.
	 *
	 * @param modifier What vanilla worked out.
	 *
	 * @return The modifier to actually use.
	 */
	public static float adjustFovModifier(Player player, float modifier) {
		if (player.getItemBySlot(EquipmentSlot.FEET).is(PEItems.GEM_BOOTS)) {
			return modifier - 0.5F * Minecraft.getInstance().options.fovEffectScale().get().floatValue();
		}
		return modifier;
	}
}
