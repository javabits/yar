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

package org.yar.guice;

import com.google.common.collect.ImmutableList;
import org.yar.Id;
import org.yar.Registration;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import static org.yar.guice.ExecutionStrategy.SYNCHRONOUS;

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

//    public GuiceWatchableRegistrationContainer() {
//        this(ListMultimapContainer.<Type, SupplierRegistration<?>>newSynchronizedContainer(), ListMultimapContainer.<Type, WatcherRegistration<?>>newLockFreeContainer());
//    }

    public GuiceWatchableRegistrationContainer() {
        this(CacheContainer.<Type, SupplierRegistration<?>>newConcurrentContainer(), CacheContainer.<Type, WatcherRegistration<?>>newNonConcurrentContainer(), SYNCHRONOUS);
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
    public boolean put(SupplierRegistration<?> registration) {
        boolean added = putToRegistry(supplierRegistry, registration);
        updateWatcher(registration, Action.ADD);
        return added;
    }

    private <T> void updateWatcher(final SupplierRegistration<T> supplierRegistration, final Action action) {
        Id<T> id = supplierRegistration.id();
        final List<WatcherRegistration<T>> watcherRegistrations = getWatcherRegistrations(id);

        for (final WatcherRegistration<T> watcherRegistration : watcherRegistrations) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    fireAddToWatcherIfMatches(watcherRegistration, supplierRegistration, action);
                }
            });
        }
    }
    //returns all the watchers associated to the type of the given id.
    @SuppressWarnings("unchecked")
    private <T> List<WatcherRegistration<T>> getWatcherRegistrations(Id<T> id) {
        ImmutableList.Builder<WatcherRegistration<T>> resultBuilder = ImmutableList.builder();
        List<WatcherRegistration<?>> watchers = watcherRegistry.getAll(id.type());
        for (WatcherRegistration<?> watcher : watchers) {
            resultBuilder.add((WatcherRegistration<T>)watcher);
        }

        return resultBuilder.build();
    }

    private <T extends Registration<?>> boolean putToRegistry(Container<Type, T> container, T registration) {
        return container.put(getRegistryKey(registration), registration);
    }

    @Override
    public boolean remove(SupplierRegistration<?> registration) {
        boolean removed = removeFromRegistry(supplierRegistry, registration);
        updateWatcher(registration, Action.REMOVE);
        return removed;
    }

    private <T extends Registration<?>> boolean removeFromRegistry(Container<Type, T> container, T registration) {
        return container.remove(getRegistryKey(registration), registration);
    }

    @Override
    public <T> boolean add(final WatcherRegistration<T> watcherRegistration) {
        for (final SupplierRegistration<T> supplierRegistration : getAll(watcherRegistration.id())) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    fireAddToWatcherIfMatches(watcherRegistration, supplierRegistration, Action.ADD);
                }
            });
        }
        return putToRegistry(watcherRegistry, watcherRegistration);
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

    private Type getRegistryKey(Registration<?> watcherRegistration) {
        return watcherRegistration.id().type();
    }

    static GuiceWatchableRegistrationContainer newMultimapGuiceWatchableRegistrationContainer() {
        return new GuiceWatchableRegistrationContainer(ListMultimapContainer.<Type, SupplierRegistration<?>>newSynchronizedContainer(), ListMultimapContainer.<Type, WatcherRegistration<?>>newLockFreeContainer(), SYNCHRONOUS);
    }

    static GuiceWatchableRegistrationContainer newLoadingCacheGuiceWatchableRegistrationContainer() {
        return newLoadingCacheGuiceWatchableRegistrationContainer(SYNCHRONOUS);
    }

    static GuiceWatchableRegistrationContainer newLoadingCacheGuiceWatchableRegistrationContainer(ExecutionStrategy executionStrategy) {
        return new GuiceWatchableRegistrationContainer(CacheContainer.<Type, SupplierRegistration<?>>newConcurrentContainer(), CacheContainer.<Type, WatcherRegistration<?>>newNonConcurrentContainer(), executionStrategy);
    }
}
