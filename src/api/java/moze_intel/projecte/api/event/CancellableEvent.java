package moze_intel.projecte.api.event;

/**
 * Base class for ProjectE events that a listener may veto.
 * <p>
 * Fabric's event system has no shared event object or built-in cancellation, so the flag lives here and the
 * dispatcher of each event stops calling further listeners once one has cancelled it.
 */
public abstract class CancellableEvent {

	private boolean canceled;

	/**
	 * @return {@code true} if a listener has vetoed whatever this event describes, in which case it must not
	 * be carried out.
	 */
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * Vetoes the action this event describes. Later listeners are not called once this is set.
	 */
	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}
}
