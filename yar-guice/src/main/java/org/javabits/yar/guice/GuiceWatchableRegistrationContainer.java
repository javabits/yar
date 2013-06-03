/*
 * Copyright 2013 Romain Gilles
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.javabits.yar.guice;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.javabits.yar.Id;
import org.javabits.yar.Registration;
import org.javabits.yar.TypeEvent;
import org.javabits.yar.TypeListener;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.javabits.yar.TypeEvent.newAddTypeEvent;
import static org.javabits.yar.TypeEvent.newRemoveTypeEvent;
import static org.javabits.yar.guice.ExecutionStrategy.SERIALIZED;

/**
 * TODO comment
 * Date: 2/20/13
 * Time: 9:47 AM
 *
 * @author Romain Gilles
 */
public class GuiceWatchableRegistrationContainer implements WatchableRegistrationContainer {

    private enum Action {
        ADD() {
            @Override
            <T> void execute(WatcherRegistration<T> watcherRegistration, SupplierRegistration<T> supplierRegistration) {
                watcherRegistration.right().add(supplierRegistration.right());
            }
        }, REMOVE() {
            @Override
            <T> void execute(WatcherRegistration<T> watcherRegistration, SupplierRegistration<T> supplierRegistration) {
                watcherRegistration.right().remove(supplierRegistration.right());
            }
        };

        abstract <T> void execute(WatcherRegistration<T> watcherRegistration, SupplierRegistration<T> supplierRegistration);
    }

    private final Container<Type, SupplierRegistration<?>> supplierRegistry;
    private final Container<Type, WatcherRegistration<?>> watcherRegistry;
    private final ExecutionStrategy executor;


    public GuiceWatchableRegistrationContainer() {
        this(CacheContainer.<SupplierRegistration<?>>newConcurrentContainer(), CacheContainer.<WatcherRegistration<?>>newNonConcurrentContainer(), SERIALIZED);
    }

    public GuiceWatchableRegistrationContainer(Container<Type, SupplierRegistration<?>> supplierRegistry
            , Container<Type, WatcherRegistration<?>> watcherRegistry, ExecutionStrategy executionStrategy) {
        this.supplierRegistry = supplierRegistry;
        this.watcherRegistry = watcherRegistry;
        this.executor = executionStrategy;
    }

    @Override
    public Set<Type> types() {
        return supplierRegistry.asMap().keySet();
    }

    @Override
    public List<SupplierRegistration<?>> getAll(Type type) {
        return supplierRegistry.getAll(type);
    }

    @Nullable
    @Override
    public SupplierRegistration<?> getFirst(Type type) {
        return supplierRegistry.getFirst(type);
    }

    @Override
    public <T> List<SupplierRegistration<T>> getAll(Id<T> id) {
        return getSupplierRegistrationsFor(id, supplierRegistry);
    }

    private <T> List<SupplierRegistration<T>> getSupplierRegistrationsFor(Id<T> id, Container<Type, SupplierRegistration<?>> registry) {
        List<SupplierRegistration<?>> pairs = registry.getAll(id.type());
        ImmutableList.Builder<SupplierRegistration<T>> suppliersByKey = ImmutableList.builder();
        for (SupplierRegistration<?> registryEntry : pairs) {
            addSupplierIfKeyEquals(id, suppliersByKey, registryEntry);
        }
        return suppliersByKey.build();
    }

    @SuppressWarnings("unchecked")
    private <T> void addSupplierIfKeyEquals(Id<T> id, ImmutableList.Builder<SupplierRegistration<T>> suppliersByKey, SupplierRegistration<?> registryEntry) {
        if (isKeyCompatibleToThisRegistration(id, registryEntry))
            suppliersByKey.add((SupplierRegistration<T>) registryEntry);
    }

    private <T> boolean isKeyCompatibleToThisRegistration(Id<T> id, Registration<?> registryEntry) {
        return id.annotationType() == null && id.type().equals(registryEntry.id().type())
                || id.equals(registryEntry.id());
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> SupplierRegistration<T> getFirst(Id<T> id) {
        List<SupplierRegistration<?>> all = supplierRegistry.getAll(id.type());
        for (SupplierRegistration<?> pair : all) {
            if (id.equals(pair.left())) {
                return (SupplierRegistration<T>) pair;
            }
        }
        return null;
    }

    @Override
    public boolean put(SupplierRegistration<?> registration, long timeout, TimeUnit unit) throws InterruptedException {
        boolean added = putToRegistry(supplierRegistry, registration);
        updateWatcher(registration, Action.ADD, timeout, unit);
        return added;
    }

    private <T> void updateWatcher(final SupplierRegistration<T> supplierRegistration, final Action action, long timeout, TimeUnit unit) throws InterruptedException {
        Id<T> id = supplierRegistration.id();
        final List<WatcherRegistration<T>> watcherRegistrations = getWatcherRegistrations(id);
        executor.execute(getUpdateActionsToExistingWatcherOnSupplierEvent(supplierRegistration, action, watcherRegistrations),timeout, unit);
    }

    private <T> List<Callable<Void>> getUpdateActionsToExistingWatcherOnSupplierEvent(final SupplierRegistration<T> supplierRegistration, final Action action, List<WatcherRegistration<T>> watcherRegistrations) {
        return Lists.transform(watcherRegistrations, new Function<WatcherRegistration<T>, Callable<Void>>() {
            @Nullable
            @Override
            public Callable<Void> apply(@Nullable WatcherRegistration<T> watcherRegistration) {
                return new ActionAdapter<>(watcherRegistration, supplierRegistration, action);
            }
        });
    }

    //returns all the watchers associated to the type of the given id.
    @SuppressWarnings("unchecked")
    private <T> List<WatcherRegistration<T>> getWatcherRegistrations(Id<T> id) {
        ImmutableList.Builder<WatcherRegistration<T>> resultBuilder = ImmutableList.builder();
        List<WatcherRegistration<?>> watchers = watcherRegistry.getAll(id.type());
        for (WatcherRegistration<?> watcher : watchers) {
            resultBuilder.add((WatcherRegistration<T>) watcher);
        }

        return resultBuilder.build();
    }

    private <T extends Registration<?>> boolean putToRegistry(Container<Type, T> container, T registration) {
        return container.put(getRegistryKey(registration), registration);
    }

    @Override
    public boolean remove(SupplierRegistration<?> registration, long timeout, TimeUnit unit) throws InterruptedException {
        boolean removed = removeFromRegistry(supplierRegistry, registration);
        updateWatcher(registration, Action.REMOVE, timeout, unit);
        return removed;
    }

    private <T extends Registration<?>> boolean removeFromRegistry(Container<Type, T> container, T registration) {
        return container.remove(getRegistryKey(registration), registration);
    }

    @Override
    public <T> boolean add(final WatcherRegistration<T> watcherRegistration, long timeout, TimeUnit unit) throws InterruptedException {
        executor.execute(getAddSupplierActionsToNewWatcher(watcherRegistration), timeout, unit);
        return putToRegistry(watcherRegistry, watcherRegistration);
    }

    private <T> List<Callable<Void>> getAddSupplierActionsToNewWatcher(final WatcherRegistration<T> watcherRegistration) {
        List<SupplierRegistration<T>> supplierRegistrations = getAll(watcherRegistration.id());
        return Lists.transform(supplierRegistrations, new Function<SupplierRegistration<T>, Callable<Void>>() {
            @Nullable
            @Override
            public Callable<Void> apply(@Nullable SupplierRegistration<T> supplierRegistration) {
                return new ActionAdapter<>(watcherRegistration, supplierRegistration, Action.ADD);
            }
        });
    }

    @Override
    public void addTypeListener(TypeListener typeListener) {
        supplierRegistry.addKeyListener(adapt(typeListener));
    }

    private static KeyListener<Type> adapt(final TypeListener typeListener) {
        return new KeyListener<Type>() {
            @Override
            public void keyAdded(KeyEvent<Type> event) {
                typeListener.typeChanged(newAddTypeEvent(event.key()));
            }

            @Override
            public void keyRemoved(KeyEvent<Type> event) {
                typeListener.typeChanged(newRemoveTypeEvent(event.key()));
            }
        };
    }

    @Override
    public void removeTypeListener(TypeListener typeListener) {
        supplierRegistry.removeKeyListener(adapt(typeListener));
    }

    static class ActionAdapter<T> implements Callable<Void> {
        private final WatcherRegistration<T> watcherRegistration;
        private final SupplierRegistration<T> supplierRegistration;
        private final Action action;

        ActionAdapter(WatcherRegistration<T> watcherRegistration, SupplierRegistration<T> supplierRegistration, Action action) {
            this.watcherRegistration = watcherRegistration;
            this.supplierRegistration = supplierRegistration;
            this.action = action;
        }

        @Override
        public Void call() throws Exception {
            fireAddToWatcherIfMatches(watcherRegistration, supplierRegistration, action);
            return null;
        }

        @Override
        public String toString() {
            return "ActionAdapter{" +
                    "watcherRegistration=" + watcherRegistration +
                    ", supplierRegistration=" + supplierRegistration +
                    ", action=" + action +
                    '}';
        }
    }
    static private <T> void fireAddToWatcherIfMatches(WatcherRegistration<T> watcherRegistration, SupplierRegistration<T> supplierRegistration, Action action) {
        if (watcherRegistration.left().matches(supplierRegistration.id())) {
            action.execute(watcherRegistration, supplierRegistration);
        }
    }

    @Override
    public boolean remove(WatcherRegistration<?> watcherRegistration) {
        return watcherRegistry.remove(getRegistryKey(watcherRegistration), watcherRegistration);
    }

    @Override
    public boolean removeAll(Type type, long timeout, TimeUnit unit) throws InterruptedException {
        List<SupplierRegistration<?>> all = getAll(type);
        for (SupplierRegistration<?> supplierRegistration: all) {
            remove(supplierRegistration,timeout, unit);
        }
        watcherRegistry.invalidate(type);
        supplierRegistry.invalidate(type);
        return true;
    }

    private Type getRegistryKey(Registration<?> watcherRegistration) {
        return watcherRegistration.id().type();
    }

    static GuiceWatchableRegistrationContainer newMultimapGuiceWatchableRegistrationContainer() {
        return new GuiceWatchableRegistrationContainer(ListMultimapContainer.<Type, SupplierRegistration<?>>newSynchronizedContainer(), ListMultimapContainer.<Type, WatcherRegistration<?>>newLockFreeContainer(), SERIALIZED);
    }

    static GuiceWatchableRegistrationContainer newLoadingCacheGuiceWatchableRegistrationContainer() {
        return newLoadingCacheGuiceWatchableRegistrationContainer(SERIALIZED);
    }

    static GuiceWatchableRegistrationContainer newLoadingCacheGuiceWatchableRegistrationContainer(ExecutionStrategy executionStrategy) {
        return new GuiceWatchableRegistrationContainer(CacheContainer.<SupplierRegistration<?>>newConcurrentContainer(), CacheContainer.<WatcherRegistration<?>>newNonConcurrentContainer(), executionStrategy);
    }
}
