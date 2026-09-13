package moze_intel.projecte.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * This event is fired on the server after all EMC values are recalculated.
 * <p>
 * Listen for it by registering with {@link #EVENT}.
 */
public class EMCRemapEvent {

	public static final Event<Callback> EVENT = EventFactory.createArrayBacked(Callback.class, callbacks -> event -> {
		for (Callback callback : callbacks) {
			callback.onEmcRemap(event);
		}
	});

	/**
	 * Fires this event to every registered listener.
	 *
	 * @return This event.
	 */
	public EMCRemapEvent fire() {
		EVENT.invoker().onEmcRemap(this);
		return this;
	}

	@FunctionalInterface
	public interface Callback {

		void onEmcRemap(EMCRemapEvent event);
	}
}
