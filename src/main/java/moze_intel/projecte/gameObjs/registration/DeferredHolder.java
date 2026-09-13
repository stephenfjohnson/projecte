package moze_intel.projecte.gameObjs.registration;

import com.mojang.datafixers.util.Either;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link Holder} that is created before the thing it points at exists, and binds once its
 * {@link DeferredRegister} has run.
 * <p>
 * The mod's object definitions are written as static fields that name what they will be, which needs a handle
 * to an entry that is not registered yet. Fabric registers immediately and hands back a bound holder, so this
 * exists to give those static fields something stable to hold onto in the meantime; every call delegates to the
 * real holder once bound.
 *
 * @param <R> The registry's type.
 * @param <T> This entry's own type.
 */
public class DeferredHolder<R, T extends R> implements Holder<R>, Supplier<T> {

	private final ResourceKey<R> key;
	private Holder.Reference<R> delegate;

	public DeferredHolder(ResourceKey<? extends Registry<R>> registryKey, ResourceLocation valueName) {
		this(ResourceKey.create(registryKey, valueName));
	}

	public DeferredHolder(ResourceKey<R> key) {
		this.key = key;
	}

	/**
	 * Points this holder at the now-registered entry. Called by {@link DeferredRegister} as it registers.
	 */
	void bind(Holder.Reference<R> delegate) {
		this.delegate = delegate;
	}

	private Holder.Reference<R> delegate() {
		if (delegate == null) {
			throw new IllegalStateException("Tried to use " + key.location() + " from registry " + key.registry()
										   + " before it was registered.");
		}
		return delegate;
	}

	public ResourceLocation getId() {
		return key.location();
	}

	public ResourceKey<R> getKey() {
		return key;
	}

	/**
	 * @return The registered entry, narrowed to the type this holder was declared with.
	 */
	@NotNull
	@Override
	@SuppressWarnings("unchecked")
	public T get() {
		return (T) delegate().value();
	}

	@Override
	public R value() {
		return delegate().value();
	}

	@Override
	public boolean isBound() {
		return delegate != null && delegate.isBound();
	}

	@Override
	public boolean is(ResourceLocation id) {
		return key.location().equals(id);
	}

	@Override
	public boolean is(ResourceKey<R> other) {
		return key.equals(other);
	}

	@Override
	public boolean is(Predicate<ResourceKey<R>> filter) {
		return filter.test(key);
	}

	@Override
	public boolean is(TagKey<R> tag) {
		return delegate().is(tag);
	}

	@Override
	public boolean is(Holder<R> holder) {
		return holder.is(key);
	}

	@Override
	public Stream<TagKey<R>> tags() {
		return delegate().tags();
	}

	@Override
	public Either<ResourceKey<R>, R> unwrap() {
		return Either.left(key);
	}

	@Override
	public Optional<ResourceKey<R>> unwrapKey() {
		return Optional.of(key);
	}

	@Override
	public Kind kind() {
		return Kind.REFERENCE;
	}

	@Override
	public boolean canSerializeIn(HolderOwner<R> owner) {
		return delegate().canSerializeIn(owner);
	}

	@Override
	public String getRegisteredName() {
		return key.location().toString();
	}

	@Override
	public boolean equals(Object o) {
		return this == o || o instanceof DeferredHolder<?, ?> other && key.equals(other.key);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(key);
	}

	@Override
	public String toString() {
		return "DeferredHolder{" + key.registry() + "/" + key.location() + "}";
	}
}
