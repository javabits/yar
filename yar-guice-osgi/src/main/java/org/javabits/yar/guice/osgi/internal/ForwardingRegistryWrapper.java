package org.javabits.yar.guice.osgi.internal;

import com.google.common.base.*;
import com.google.common.reflect.TypeToken;
import org.javabits.yar.*;
import org.javabits.yar.Supplier;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * TODO comment
 * Date: 5/29/13
 * Time: 9:42 AM
 *
 * @author Romain Gilles
 */
class ForwardingRegistryWrapper implements BlockingSupplierRegistry {
    private final BlockingSupplierRegistry delegate;

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
    public <T> Registration<T> put(Id<T> id, Supplier<T> supplier) {
        return delegate.put(id, supplier);
    }

    @Override
    public <T> Registration<T> put(Id<T> id, com.google.common.base.Supplier<T> supplier) {
        return delegate.put(id, supplier);
    }

    @Override
    public void remove(Registration<?> registration) {
        delegate.remove(registration);
    }

    @Override
    public <T> Registration<T> addWatcher(IdMatcher<T> watchedKey, Watcher<T> watcher) {
        return delegate.addWatcher(watchedKey, watcher);
    }

    @Override
    public void removeWatcher(Registration<?> watcherRegistration) {
        delegate.removeWatcher(watcherRegistration);
    }
}
