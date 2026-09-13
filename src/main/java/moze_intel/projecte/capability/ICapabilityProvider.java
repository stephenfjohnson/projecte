package moze_intel.projecte.capability;

import org.jetbrains.annotations.Nullable;

/**
 * Supplies a capability for some object, given the context the lookup was performed with.
 * <p>
 * Fabric's lookups take a plain {@code BiFunction} for this; naming the shape keeps the nullability contract
 * explicit and the builder signatures readable.
 *
 * @param <O> The object the capability is being looked up on, such as a block entity or an item stack.
 * @param <C> The lookup context, for instance the side a block was approached from.
 * @param <A> The capability type.
 */
@FunctionalInterface
public interface ICapabilityProvider<O, C, A> {

	/**
	 * @return The capability, or {@code null} if this object does not provide it in this context.
	 */
	@Nullable
	A getCapability(O object, C context);
}
