package moze_intel.projecte.gameObjs.registration.impl;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import moze_intel.projecte.gameObjs.registration.PEDeferredHolder;
import moze_intel.projecte.gameObjs.registration.PEDeferredRegister;
import moze_intel.projecte.utils.text.ILangEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

public class CreativeTabDeferredRegister extends PEDeferredRegister<CreativeModeTab> {

	private final Runnable addToExistingTabs;

	/**
	 * @param addToExistingTabs Hooks ProjectE's entries into vanilla's tabs. Fabric registers that as its own
	 *                          event rather than passing it a tab-building event, so it is run once here.
	 */
	public CreativeTabDeferredRegister(String modid, Runnable addToExistingTabs) {
		super(Registries.CREATIVE_MODE_TAB, modid);
		this.addToExistingTabs = addToExistingTabs;
	}

	@Override
	public void register() {
		super.register();
		addToExistingTabs.run();
	}

	/**
	 * @apiNote We manually require the title and icon to be passed so that we ensure all tabs have one.
	 */
	public PEDeferredHolder<CreativeModeTab, CreativeModeTab> registerMain(ILangEntry title, ItemLike icon, UnaryOperator<CreativeModeTab.Builder> operator) {
		return register(getNamespace(), title, icon, operator);
	}

	/**
	 * @apiNote We manually require the title and icon to be passed so that we ensure all tabs have one.
	 */
	public PEDeferredHolder<CreativeModeTab, CreativeModeTab> register(String name, ILangEntry title, ItemLike icon, UnaryOperator<CreativeModeTab.Builder> operator) {
		return register(name, () -> {
			CreativeModeTab.Builder builder = CreativeModeTab.builder()
					.title(title.translate())
					.icon(() -> icon.asItem().getDefaultInstance());
			return operator.apply(builder).build();
		});
	}
}