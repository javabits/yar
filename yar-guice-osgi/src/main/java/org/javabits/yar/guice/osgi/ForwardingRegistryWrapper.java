package org.javabits.yar.guice.osgi;

import com.google.common.collect.MapMaker;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.javabits.yar.*;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.util.concurrent.Futures.addCallback;

/**
 * This class is responsible to handle the cleanup of the registry when a bundle is shutdown.
 * It must remove all the remaning suppliers and watchers/listeners. And finally invalidate
 * the entries associated a type whose the stopping bundle is the owner.
 * <p/>
 * Date: 5/29/13
 * Time: 9:42 AM
 *
 * @author Romain Gilles
 */
class ForwardingRegistryWrapper implements BlockingSupplierRegistry, BundleRegistryWrapper {
    private static final Logger LOG = Logger.getLogger(ForwardingRegistryWrapper.class.getName());
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
    private final ConcurrentMap<Registration<?>, Id<?>> supplierRegistrations = new ConcurrentHashMap<>(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
    private final ConcurrentMap<Registration<?>, Id<?>> watcherRegistrations = new MapMaker().weakKeys().initialCapacity(DEFAULT_INITIAL_CAPACITY).concurrencyLevel(DEFAULT_CONCURRENCY_LEVEL).makeMap();

    ForwardingRegistryWrapper(BlockingSupplierRegistry delegate) {
        this.delegate = delegate;
    }

    @Nullable
    @Override
    public <T> BlockingSupplier<T> get(Class<T> type) {
        return delegate.get(type);
    }

    @Nullable
    @Override
    public <T> BlockingSupplier<T> get(Id<T> id) {
        return delegate.get(id);
    }

    @Nullable
    @Override
    public <T> Supplier<T> get(TypeToken<T> type) {
        return delegate.get(type);
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
        return delegate.getAll(type);
    }

    @Override
    public <T> List<Supplier<T>> getAll(Id<T> id) {
        return delegate.getAll(id);
    }

    @Override
    public <T> List<Supplier<T>> getAll(TypeToken<T> type) {
        return delegate.getAll(type);
    }

    @Override
    public <T> ListenableFuture<Registration<T>> put(Id<T> id, com.google.common.base.Supplier<? extends T> supplier) {
        if (!mutable.get()) {
            return newNullRegistration(id);
        }
        ListenableFuture<Registration<T>> registration = delegate.put(id, supplier);
        trackRegistration(id, "Supplier", registration, supplierRegistrations);
        return registration;
    }

    @Override
    public ListenableFuture<Void> remove(Registration<?> registration) {
        if (!mutable.get()) {
            return newNullFuture();
        }
        supplierRegistrations.remove(registration);
        return delegate.remove(registration);
    }

    @Override
    public ListenableFuture<Void> removeAll(Collection<? extends Registration<?>> registrations) {
        if (!mutable.get()) {
            return newNullFuture();
        }
        for (Registration<?> registration : registrations) {
            supplierRegistrations.remove(registration);
        }
        return delegate.removeAll(registrations);
    }

    @Override
    public <T> ListenableFuture<Registration<T>> addWatcher(final IdMatcher<T> watchedKey, Watcher<T> watcher) {
        if (!mutable.get()) {
            return newNullRegistration(watchedKey.id());
        }
        final Id<T> id = watchedKey.id();
        final String registrationType = "Watcher";
        final ListenableFuture<Registration<T>> registration = delegate.addWatcher(watchedKey, watcher);
        final ConcurrentMap<Registration<?>, Id<?>> registrations = watcherRegistrations;

        trackRegistration(id, registrationType, registration, registrations);

        return registration;
    }

    private <T> void trackRegistration(final Id<T> id, final String registrationType, ListenableFuture<Registration<T>> registration, final ConcurrentMap<Registration<?>, Id<?>> registrations) {
        addCallback(registration, new FutureCallback<Registration<T>>() {
            @Override
            public void onSuccess(Registration<T> result) {

                registrations.put(result, id);
            }

            @Override
            public void onFailure(Throwable t) {
                 LOG.log(Level.FINE, registrationType + "Registration failed: " + id, t);
                //do nothing
            }
        });
    }

    @Override
    public ListenableFuture<Void> removeWatcher(Registration<?> watcherRegistration) {
        if (!mutable.get()) {
            return newNullFuture();
        }
        watcherRegistrations.remove(watcherRegistration);
        return delegate.removeWatcher(watcherRegistration);
    }

    private ListenableFuture<Void> newNullFuture() {
        SettableFuture<Void> settableFuture = SettableFuture.create();
        settableFuture.set(null);
        return settableFuture;
    }

    @Override
    public ListenableFuture<Void> removeAllWatchers(Collection<? extends Registration<?>> watcherRegistrations) {
        if (!mutable.get()) {
            return newNullFuture();
        }
        watcherRegistrations.removeAll(watcherRegistrations);
        return delegate.removeAllWatchers(watcherRegistrations);
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

    private static <T> ListenableFuture<Registration<T>> newNullRegistration(final Id<T> id) {
        SettableFuture<Registration<T>> futureRegistration = SettableFuture.create();
        futureRegistration.set(new Registration<T>() {
            @Override
            public Id<T> id() {
                return id;
            }
        });
        return futureRegistration;
    }
}
