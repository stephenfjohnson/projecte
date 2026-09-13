package moze_intel.projecte.api.event;

import moze_intel.projecte.api.ItemInfo;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired on the server when a player is attempting to place an item in the condenser.
 * <p>
 * Listen for it by registering with {@link #EVENT}. Cancelling it stops the action.
 */
public class PlayerAttemptCondenserSetEvent extends CancellableEvent {

	public static final Event<Callback> EVENT = EventFactory.createArrayBacked(Callback.class, callbacks -> event -> {
		for (Callback callback : callbacks) {
			callback.onAttemptCondenserSet(event);
			if (event.isCanceled()) {
				//A listener vetoed it, so do not give the rest a say
				return;
			}
		}
	});

	private final Player player;
	private final ItemInfo sourceInfo;
	private final ItemInfo reducedInfo;

	public PlayerAttemptCondenserSetEvent(@NotNull Player entityPlayer, @NotNull ItemInfo sourceInfo, @NotNull ItemInfo reducedInfo) {
		player = entityPlayer;
		this.sourceInfo = sourceInfo;
		this.reducedInfo = reducedInfo;
	}

	/**
	 * @return The player who is attempting to put in the condenser slot.
	 */
	@NotNull
	public Player getPlayer() {
		return player;
	}

	/**
	 * @return The {@link ItemInfo} that the player is trying to condense.
	 */
	@NotNull
	public ItemInfo getSourceInfo() {
		return sourceInfo;
	}

	/**
	 * Gets the "cleaned" {@link ItemInfo} that the player is trying to condense. This {@link ItemInfo} may have reduced data component information.
	 *
	 * @return The "cleaned" {@link ItemInfo} that the player is trying to learn.
	 */
	@NotNull
	public ItemInfo getReducedInfo() {
		return reducedInfo;
	}

	/**
	 * Fires this event to every registered listener, stopping early if one cancels it.
	 *
	 * @return This event, so that the caller can check {@link #isCanceled()}.
	 */
	public PlayerAttemptCondenserSetEvent fire() {
		EVENT.invoker().onAttemptCondenserSet(this);
		return this;
	}

	@FunctionalInterface
	public interface Callback {

		void onAttemptCondenserSet(PlayerAttemptCondenserSetEvent event);
	}
}
