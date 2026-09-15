package moze_intel.projecte.utils.text;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * @apiNote From Mekanism
 */
public interface IHasTranslationKey {

	String getTranslationKey();

	/**
	 * @implNote NeoForge had a {@code TranslatableEnum} interface that declared {@code getTranslatedName} for its config
	 * screens to pick up. Fabric has no equivalent, so this just provides the method for ProjectE's own use.
	 */
	interface IHasEnumNameTranslationKey extends IHasTranslationKey {

		@NotNull
		default Component getTranslatedName() {
			return TextComponentUtil.translate(getTranslationKey());
		}
	}
}