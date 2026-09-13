package moze_intel.projecte.gameObjs.container;

import java.util.function.Consumer;
import moze_intel.projecte.gameObjs.container.PEMenus;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

/**
 * Opens ProjectE's menus, which all need a little extra data on the client to set themselves up.
 * <p>
 * NeoForge added overloads of {@code Player#openMenu} taking a buffer writer. Vanilla has no such thing, so the
 * provider is wrapped in Fabric's {@link ExtendedScreenHandlerFactory} instead.
 */
public final class PEMenus {

	private PEMenus() {
	}

	/**
	 * Opens a menu, sending whatever {@code writer} puts in the buffer along with it.
	 */
	public static void open(Player player, MenuProvider provider, Consumer<RegistryFriendlyByteBuf> writer) {
		if (player instanceof ServerPlayer serverPlayer) {
			PEMenus.open(serverPlayer, new DataProvider(provider, writer));
		}
	}

	/**
	 * Opens a menu that needs no extra data of its own.
	 */
	public static void open(Player player, MenuProvider provider) {
		open(player, provider, buffer -> {
		});
	}

	/**
	 * Opens a menu for a block, sending its position so the client can find the block entity.
	 */
	public static void open(Player player, MenuProvider provider, BlockPos pos) {
		open(player, provider, buffer -> buffer.writeBlockPos(pos));
	}

	private record DataProvider(MenuProvider delegate, Consumer<RegistryFriendlyByteBuf> writer)
			implements ExtendedScreenHandlerFactory<PEMenuData> {

		@Override
		public PEMenuData getScreenOpeningData(ServerPlayer player) {
			return PEMenuData.of(writer, player.registryAccess());
		}

		@Override
		public Component getDisplayName() {
			return delegate.getDisplayName();
		}

		@Nullable
		@Override
		public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
			return delegate.createMenu(containerId, inventory, player);
		}
	}
}
