package org.javabits.yar.guice.osgi;

import com.google.common.collect.MapMaker;
import com.google.common.reflect.TypeToken;
import org.javabits.yar.*;
import org.javabits.yar.Supplier;
import org.javabits.yar.guice.Reflections;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.javabits.yar.guice.Reflections.getRawType;

/**
 * This class is responsible to handle the cleanup of the registry when a bundle is shutdown.
 * It must remove all the remaning suppliers and watchers/listeners. And finally invalidate
 * the entries associated a type whose the stopping bundle is the owner.
 *
 * Date: 5/29/13
 * Time: 9:42 AM
 *
 * @author Romain Gilles
 */
class ForwardingRegistryWrapper implements BlockingSupplierRegistry {
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

    private final BlockingSupplierRegistry delegate;
    private static final Object NULL_VALUE = Boolean.TRUE;
    private ConcurrentMap<Registration<?>, Object> supplierRegistrations = new ConcurrentHashMap<>(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
    private ConcurrentMap<Registration<?>, Object> watcherRegistrations = new MapMaker().weakKeys().initialCapacity(DEFAULT_INITIAL_CAPACITY).concurrencyLevel(DEFAULT_CONCURRENCY_LEVEL).makeMap();

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

    @Override
    public <T> List<Supplier<T>> getAll(Class<T> type) {
        return delegate.getAll(type);
    }

    @Override
    public Set<Id<?>> ids() {
        return delegate.ids();
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
    public <T> Registration<T> put(Id<T> id, com.google.common.base.Supplier<T> supplier) {
        Registration<T> registration = delegate.put(id, supplier);
        supplierRegistrations.put(registration, NULL_VALUE);
        return registration;
    }

    @Override
    public void remove(Registration<?> registration) {
        supplierRegistrations.remove(registration);
        delegate.remove(registration);
    }

    @Override
    public void removeAll(Collection<? extends Registration<?>> registrations) {
        for (Registration<?> registration : registrations) {
            supplierRegistrations.remove(registration);
        }
        delegate.removeAll(registrations);
    }

    @Override
    public <T> Registration<T> addWatcher(IdMatcher<T> watchedKey, Watcher<T> watcher) {
        Registration<T> registration = delegate.addWatcher(watchedKey, watcher);
        watcherRegistrations.put(registration, NULL_VALUE);
        return registration;
    }

    @Override
    public void removeWatcher(Registration<?> watcherRegistration) {
        watcherRegistrations.remove(watcherRegistration);
        delegate.removeWatcher(watcherRegistration);
    }

    @Override
    public void removeAllWatchers(Collection<? extends Registration<?>> watcherRegistrations) {
        watcherRegistrations.removeAll(watcherRegistrations);
        delegate.removeAllWatchers(watcherRegistrations);
    }

    public void clear() {
        //remove first the supplier to let to the watcher a chance handle it
        delegate.removeAll(supplierRegistrations.keySet());
        //then remove the watcher
        delegate.removeAllWatchers(watcherRegistrations.keySet());
    }
}
