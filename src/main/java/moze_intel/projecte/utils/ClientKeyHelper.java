package moze_intel.projecte.utils;

import com.google.common.collect.ImmutableBiMap;
import com.mojang.blaze3d.platform.InputConstants;
import moze_intel.projecte.network.PEPackets;
import moze_intel.projecte.network.packets.to_server.KeyPressPKT;
import moze_intel.projecte.utils.text.PELang;
import moze_intel.projecte.utils.text.TextComponentUtil;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ClientKeyHelper {

	private static ImmutableBiMap<PEKeybind, KeyMapping> peToMc = ImmutableBiMap.of();

	/**
	 * Registers ProjectE's keybinds.
	 * <p>
	 * The helmet and boots toggles both sit on X, told apart by whether shift is held, which is how NeoForge's key
	 * modifiers expressed it. Fabric and vanilla have no notion of a modifier on a binding, so the shift state is
	 * checked when the key is pressed instead. The controls screen will show the two as conflicting, which is
	 * cosmetic - they still do the right thing.
	 */
	public static void registerKeyBindings() {
		ImmutableBiMap.Builder<PEKeybind, KeyMapping> builder = ImmutableBiMap.builder();
		addKeyBinding(builder, PEKeybind.HELMET_TOGGLE, true, GLFW.GLFW_KEY_X);
		addKeyBinding(builder, PEKeybind.BOOTS_TOGGLE, false, GLFW.GLFW_KEY_X);
		addKeyBinding(builder, PEKeybind.CHARGE, false, GLFW.GLFW_KEY_V);
		addKeyBinding(builder, PEKeybind.EXTRA_FUNCTION, false, GLFW.GLFW_KEY_C);
		addKeyBinding(builder, PEKeybind.FIRE_PROJECTILE, false, GLFW.GLFW_KEY_R);
		addKeyBinding(builder, PEKeybind.MODE, false, GLFW.GLFW_KEY_G);
		addKeyBinding(builder, PEKeybind.TRANSMUTATION_TABLET, false, GLFW.GLFW_KEY_K);
		peToMc = builder.build();
	}

	private static void addKeyBinding(ImmutableBiMap.Builder<PEKeybind, KeyMapping> builder, PEKeybind keyBind, boolean requiresShift, int keyCode) {
		KeyMapping keyMapping = new PEKeyMapping(keyBind, requiresShift, keyCode);
		builder.put(keyBind, keyMapping);
		KeyBindingHelper.registerKeyBinding(keyMapping);
	}

	public static Component getKeyName(PEKeybind k) {
		KeyMapping keyMapping = peToMc.get(k);
		if (keyMapping == null) {
			//Fallback to the translation key of the key's function
			return TextComponentUtil.build(k);
		}
		return keyMapping.getTranslatedKeyMessage();
	}

	private static class PEKeyMapping extends KeyMapping {

		private final PEKeybind keybind;
		private final boolean requiresShift;
		private boolean lastState;

		PEKeyMapping(PEKeybind keybind, boolean requiresShift, int keyCode) {
			super(keybind.getTranslationKey(), InputConstants.Type.KEYSYM, keyCode, PELang.PROJECTE.getTranslationKey());
			this.keybind = keybind;
			this.requiresShift = requiresShift;
		}

		@Override
		public void setDown(boolean value) {
			super.setDown(value);
			//Note: We check the state based on isDown instead of value, as the value may be wrong depending on the conflict context
			boolean state = isDown() && Screen.hasShiftDown() == requiresShift;
			if (state != lastState) {
				if (state) {
					PEPackets.sendToServer(new KeyPressPKT(keybind));
				}
				lastState = state;
			}
		}
	}
}