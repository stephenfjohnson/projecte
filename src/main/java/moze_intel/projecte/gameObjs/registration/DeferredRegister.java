package moze_intel.projecte.gameObjs.registration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Collects the entries a mod wants in one registry and registers them in a single pass.
 * <p>
 * Fabric registers entries as soon as you ask it to, which does not fit definitions written as static fields
 * that reference one another. Entries are therefore gathered here as suppliers and only created and registered
 * when {@link #register()} runs during mod initialisation, at which point every handed-out
 * {@link DeferredHolder} is bound.
 *
 * @param <T> The registry's type.
 */
public class DeferredRegister<T> {

	@NotNull
	private final ResourceKey<? extends Registry<T>> registryKey;
	@NotNull
	private final String namespace;
	private final List<Entry<T, ? extends T>> entries = new ArrayList<>();
	private final List<DeferredHolder<T, ? extends T>> holders = new ArrayList<>();
	private boolean registered;

	public DeferredRegister(@NotNull ResourceKey<? extends Registry<T>> registryKey, @NotNull String namespace) {
		this.registryKey = registryKey;
		this.namespace = namespace;
	}

	@NotNull
	public ResourceKey<? extends Registry<T>> getRegistryKey() {
		return registryKey;
	}

	@NotNull
	public String getNamespace() {
		return namespace;
	}

	public ResourceLocation id(String name) {
		return ResourceLocation.fromNamespaceAndPath(namespace, name);
	}

	@NotNull
	public <I extends T> DeferredHolder<T, I> register(@NotNull String name, @NotNull Supplier<? extends I> sup) {
		return register(name, id -> sup.get());
	}

	@NotNull
	public <I extends T> DeferredHolder<T, I> register(@NotNull String name, @NotNull Function<ResourceLocation, ? extends I> func) {
		if (registered) {
			throw new IllegalStateException("Cannot add " + name + " to " + registryKey.location() + " after it has been registered.");
		}
		ResourceLocation id = id(name);
		DeferredHolder<T, I> holder = createHolder(registryKey, id);
		entries.add(new Entry<>(id, func, holder));
		holders.add(holder);
		return holder;
	}

	/**
	 * Creates the handle handed back for an entry. Subclasses override this to hand back a richer holder.
	 */
	@NotNull
	@SuppressWarnings("unchecked")
	protected <I extends T> DeferredHolder<T, I> createHolder(@NotNull ResourceKey<? extends Registry<T>> registryKey, @NotNull ResourceLocation key) {
		return (DeferredHolder<T, I>) new DeferredHolder<T, T>(registryKey, key);
	}

	/**
	 * Creates every collected entry and puts it in the registry, binding the holders handed out earlier.
	 * <p>
	 * Call this once, from mod initialisation.
	 */
	@SuppressWarnings("unchecked")
	public void register() {
		if (registered) {
			throw new IllegalStateException("Registry " + registryKey.location() + " has already been registered for " + namespace + ".");
		}
		registered = true;
		Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(registryKey.location());
		if (registry == null) {
			throw new IllegalStateException("No registry found for " + registryKey.location() + ".");
		}
		for (Entry<T, ? extends T> entry : entries) {
			entry.registerInto(registry);
		}
	}

	public boolean isRegistered() {
		return registered;
	}

	public Collection<DeferredHolder<T, ? extends T>> getEntries() {
		return Collections.unmodifiableCollection(holders);
	}

	private record Entry<T, I extends T>(ResourceLocation id, Function<ResourceLocation, ? extends I> factory, DeferredHolder<T, I> holder) {

		private void registerInto(Registry<T> registry) {
			Holder.Reference<T> reference = Registry.registerForHolder(registry, id, factory.apply(id));
			holder.bind(reference);
		}
	}
}
