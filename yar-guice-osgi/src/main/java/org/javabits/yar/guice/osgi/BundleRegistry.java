package org.javabits.yar.guice.osgi;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.javabits.yar.*;
import org.javabits.yar.Supplier;
import org.javabits.yar.guice.SupplierWrapper;
import org.osgi.framework.Bundle;

import javax.annotation.Nullable;
import java.lang.InterruptedException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.util.concurrent.Futures.addCallback;

/**
 * This class is responsible to handle the cleanup of the registry when a bundle is shutdown.
 * It must remove all the remaining suppliers and watchers/listeners. And finally invalidate
 * the entries associated a type whose the stopping bundle is the owner.
 * <p/>
 * Date: 5/29/13
 * Time: 9:42 AM
 *
 * @author Romain Gilles
 */
class BundleRegistry implements BlockingSupplierRegistry, RegistryHook, OSGiRegistry {
    private static final Logger LOG = Logger.getLogger(BundleRegistry.class.getName());
    /**
     * The default initial capacity for concurrent maps
     */
    static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * The default load factor for concurrent maps
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * The default concurrency level for concurrent maps
     */
    static final int DEFAULT_CONCURRENCY_LEVEL = 16;

    /**
     * Flag use to make the registry read only when it is cleared.
     */
    private final AtomicBoolean mutable = new AtomicBoolean(true);

    private final BlockingSupplierRegistry delegate;
    private final Bundle bundle;
    private final RegistryHook registryHook;
    private final ConcurrentMap<Registration<?>, Id<?>> supplierRegistrations = new ConcurrentHashMap<>(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
    private final ConcurrentMap<Registration<?>, Id<?>> watcherRegistrations = new MapMaker().weakKeys().initialCapacity(DEFAULT_INITIAL_CAPACITY).concurrencyLevel(DEFAULT_CONCURRENCY_LEVEL).makeMap();

    BundleRegistry(BlockingSupplierRegistry delegate, Bundle bundle) {
        checkArgument(delegate instanceof RegistryHook, "Wrapped registry must implement RegistryHook interface");
        this.delegate = delegate;
        this.bundle = checkNotNull(bundle, "bundle");
        //noinspection ConstantConditions
        registryHook = (RegistryHook) delegate;
    }

    @Override
    public long defaultTimeout() {
        return delegate.defaultTimeout();
    }

    @Override
    public TimeUnit defaultTimeUnit() {
        return delegate.defaultTimeUnit();
    }

    @Nullable
    @Override
    public <T> OSGiSupplier<T> get(Class<T> type) {
        return newDecorator(delegate.get(type));
    }

    @Nullable
    @Override
    public <T> OSGiSupplier<T> get(Id<T> id) {
        return newDecorator(delegate.get(id));
    }

    @Nullable
    @Override
    public <T> OSGiSupplier<T> get(TypeToken<T> type) {
        return newDecorator(delegate.get(type));
    }

    @Override
    public Set<Type> types() {
        return delegate.types();
    }

    @Override
    public Set<Id<?>> ids() {
        return delegate.ids();
    }

    @Override
    public <T> List<Supplier<T>> getAll(Class<T> type) {
        return transformToBundleSuppliers(delegate.getAll(type));
    }

    @Override
    public <T> List<Supplier<T>> getAll(Id<T> id) {
        return transformToBundleSuppliers(delegate.getAll(id));
    }

    @Override
    public <T> List<Supplier<T>> getAll(TypeToken<T> type) {
        return transformToBundleSuppliers(delegate.getAll(type));
    }

    @Override
    public <T> Registration<T> put(Id<T> id, com.google.common.base.Supplier<? extends T> supplier) {
        if (!mutable.get()) {
            return newNullRegistration(id);
        }
        Registration<T> registration = delegate.put(id, newGuavaWrapper(supplier));
        trackRegistration(id, "Supplier", registration, supplierRegistrations);
        return registration;
    }

    @Override
    public void remove(Registration<?> registration) {
        if (!mutable.get()) {
            return;
        }
        supplierRegistrations.remove(registration);
        delegate.remove(registration);
    }

    @Override
    public void removeAll(Collection<? extends Registration<?>> registrations) {
        if (!mutable.get()) {
            return;
        }
        for (Registration<?> registration : registrations) {
            supplierRegistrations.remove(registration);
        }
        delegate.removeAll(registrations);
    }

    @Override
    public <T> Registration<T> addWatcher(IdMatcher<T> watchedKey, Watcher<T> watcher) {
        if (!mutable.get()) {
            return newNullRegistration(watchedKey.id());
        }
        Registration<T> registration = delegate.addWatcher(watchedKey, watcher);
        trackRegistration(watchedKey.id(), "Watcher", registration, watcherRegistrations);
        return registration;
    }

    private <T> void trackRegistration(final Id<T> id, final String registrationType, Registration<T> registration, final ConcurrentMap<Registration<?>, Id<?>> registrations) {
        LOG.log(Level.FINER, registrationType + " Registration: " + id);
        registrations.put(registration, id);
    }

    @Override
    public void removeWatcher(Registration<?> watcherRegistration) {
        if (!mutable.get()) {
            return;
        }
        watcherRegistrations.remove(watcherRegistration);
        delegate.removeWatcher(watcherRegistration);
    }

    @Override
    public void removeAllWatchers(Collection<? extends Registration<?>> watcherRegistrations) {
        if (!mutable.get()) {
            return;
        }
        for (Registration<?> watcherRegistration : watcherRegistrations) {
            this.watcherRegistrations.remove(watcherRegistration);
        }
        delegate.removeAllWatchers(watcherRegistrations);
    }

    @Override
    public void clear() {
        mutable.set(false);
        //remove first the supplier to let to the watcher a chance handle it
        delegate.removeAll(supplierRegistrations.keySet());
        //then remove the watcher
        delegate.removeAllWatchers(watcherRegistrations.keySet());
    }

    @Override
    public Collection<Id<?>> getBundleWatchers() {
        return watcherRegistrations.values();
    }

    @Override
    public Collection<Id<?>> getBundleSuppliers() {
        return supplierRegistrations.values();
    }

    private static <T> Registration<T> newNullRegistration(final Id<T> id) {
        return new Registration<T>() {
            @Override
            public Id<T> id() {
                return id;
            }
        };
    }

    @Override
    public void invalidate(Type type) {
        if (!mutable.get()) {
            return;
        }
        registryHook.invalidate(type);
    }

    @Override
    public void invalidateAll(Collection<Type> types) {
        if (!mutable.get()) {
            return;
        }
        registryHook.invalidateAll(types);
    }

    @Override
    public void addTypeListener(TypeListener typeListener) {
        if (!mutable.get()) {
            return;
        }
        registryHook.addTypeListener(typeListener);
    }

    @Override
    public void removeTypeListener(TypeListener typeListener) {
        if (!mutable.get()) {
            return;
        }
        registryHook.removeTypeListener(typeListener);
    }

    @Override
    public boolean hasPendingListenerUpdateTasks() {
        return registryHook.hasPendingListenerUpdateTasks();
    }

    @Override
    public void addEndOfListenerUpdateTasksListener(EndOfListenerUpdateTasksListener listener) {
        registryHook.addEndOfListenerUpdateTasksListener(listener);
    }

    private <T> OSGiSupplier<T> newDecorator(BlockingSupplier<T> blockingSupplier) {
        return blockingSupplier == null ? null : new BlockingSupplierDecorator<>(blockingSupplier);
    }

    private <T> OSGiGuavaWrapper<T> newGuavaWrapper(com.google.common.base.Supplier<T> nativeSupplier) {
        return new OSGiGuavaWrapper<>(nativeSupplier, bundle);
    }

    private <T> List<Supplier<T>> transformToBundleSuppliers(List<Supplier<T>> suppliers) {
        return Lists.transform(suppliers, new Function<Supplier<T>, Supplier<T>>() {
            @Nullable
            @Override
            public Supplier<T> apply(@Nullable Supplier<T> supplier) {
                return new BundleSupplierWrapper<>(supplier);
            }
        });
    }

    private static <T> com.google.common.base.Supplier<? extends T> nativeSupplier(Supplier<T> delegate) {
        com.google.common.base.Supplier<? extends T> nativeSupplier = delegate.getNativeSupplier();
        while (nativeSupplier instanceof SupplierWrapper) {
            nativeSupplier = getWrapped(nativeSupplier);
        }
        return nativeSupplier;
    }

    private static <T> Bundle bundle(Supplier<T> delegate) {
        com.google.common.base.Supplier<? extends T> nativeSupplier = delegate.getNativeSupplier();
        while (nativeSupplier instanceof SupplierWrapper && !(nativeSupplier instanceof OSGiGuavaWrapper)) {
            nativeSupplier = getWrapped(nativeSupplier);
        }

        if (nativeSupplier instanceof OSGiGuavaWrapper) {
            return ((OSGiGuavaWrapper) nativeSupplier).getBundle();
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> com.google.common.base.Supplier<T> getWrapped(com.google.common.base.Supplier<? extends T> supplier) {
        return ((SupplierWrapper<T>) supplier).getWrapped();
    }

    <T> T injectAware(@Nullable T instance) {
        if (instance instanceof Aware) {
            if (instance instanceof BundleAware) {
                ((BundleAware) instance).setBundle(bundle);
            }
            if (instance instanceof OSGiRegistryAware) {
                ((OSGiRegistryAware) instance).setOSGiRegistry(this);
            }
            if (instance instanceof BlockingSupplierRegistryAware) {
                ((BlockingSupplierRegistryAware) instance).setBlockingSupplierRegistry(this);
            }
            if (instance instanceof RegistryAware) {
                ((RegistryAware) instance).setRegistry(this);
            }
        }
        return instance;
    }

    /**
     * This class is used to wrap the suppliers returned by the getAll(...) methods of the registry
     * in order to provide access to the underlying bundle for where the supplier as been registered.
     *
     * @param <T>
     */
    private final class BundleSupplierWrapper<T> implements BundleSupplier<T> {
        private final Supplier<T> delegate;

        private BundleSupplierWrapper(Supplier<T> delegate) {
            this.delegate = delegate;
        }

        @Nullable
        @Override
        public Bundle getBundle() {
            return bundle(delegate);
        }

        @Override
        public Id<T> id() {
            return delegate.id();
        }

        @Nullable
        @Override
        public T get() {
            return injectAware(delegate.get());
        }

        @Nullable
        @Override
        public com.google.common.base.Supplier<? extends T> getNativeSupplier() {
            return nativeSupplier(delegate);
        }
    }

    private static final class OSGiGuavaWrapper<T> implements com.google.common.base.Supplier<T>, org.javabits.yar.guice.SupplierWrapper<T> {
        private final com.google.common.base.Supplier<T> delegate;
        private final Bundle bundle;

        private OSGiGuavaWrapper(com.google.common.base.Supplier<T> delegate, Bundle bundle) {
            this.delegate = delegate;
            this.bundle = bundle;
        }

        @Override
        public T get() {
            return delegate.get();
        }

        Bundle getBundle() {
            return bundle;
        }

        @Override
        public com.google.common.base.Supplier<T> getWrapped() {
            return delegate;
        }

        @Override
        public String toString() {
            return "OSGiGuavaWrapper{" +
                    "delegate=" + delegate +
                    ", bundle=" + bundle +
                    '}';
        }
    }

    private final class BlockingSupplierDecorator<T> implements OSGiSupplier<T> {
        private final BlockingSupplier<T> delegate;

        private BlockingSupplierDecorator(BlockingSupplier<T> delegate) {
            this.delegate = delegate;
        }

        @Nullable
        @Override
        public T get() {
            return injectAware(delegate.get());
        }

        @Override
        public T getSync() throws InterruptedException {
            return injectAware(delegate.getSync());
        }

        @Override
        public T getSync(long timeout, TimeUnit unit) throws InterruptedException, java.util.concurrent.TimeoutException {
            return injectAware(delegate.getSync(timeout, unit));
        }

        @Override
        public ListenableFuture<T> getAsync() {
            final SettableFuture<T> future = SettableFuture.create();

            // The future callback will be executed either on the current thread (if the future is
            // already completed) or on the registry's action handler thread.
            addCallback(delegate.getAsync(), new FutureCallback<T>() {
                @Override
                public void onSuccess(T instance) {
                    future.set(injectAware(instance));
                }

                @Override
                public void onFailure(Throwable t) {
                }
            });

            return future;
        }

        @Override
        public long defaultTimeout() {
            return delegate.defaultTimeout();
        }

        @Override
        public TimeUnit defaultTimeUnit() {
            return delegate.defaultTimeUnit();
        }

        @Override
        public Id<T> id() {
            return delegate.id();
        }

        @Override
        @Nullable
        public com.google.common.base.Supplier<? extends T> getNativeSupplier() {
            return nativeSupplier(delegate);
        }

        @Override
        public Bundle getBundle() {
            return bundle(delegate);
        }
    }
}
