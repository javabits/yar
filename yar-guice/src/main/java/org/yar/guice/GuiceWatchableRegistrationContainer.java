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
import org.yar.Key;
import org.yar.Registration;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;

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
                watcherRegistration.right.add(supplierRegistration.right);
            }
        }, REMOVE() {
            @Override
            <T> void execute(WatcherRegistration<T> watcherRegistration, SupplierRegistration<T> supplierRegistration) {
                watcherRegistration.right.remove(supplierRegistration.right);
            }
        };
        abstract <T> void execute(WatcherRegistration<T> watcherRegistration, SupplierRegistration<T> supplierRegistration);
    }

    private final Container<Type, SupplierRegistration<?>> supplierRegistry;
    private final Container<Type, WatcherRegistration<?>> watcherRegistry;

//    public GuiceWatchableRegistrationContainer() {
//        this(ListMultimapContainer.<Type, SupplierRegistration<?>>newSynchronizedContainer(), ListMultimapContainer.<Type, WatcherRegistration<?>>newLockFreeContainer());
//    }

    public GuiceWatchableRegistrationContainer() {
        this(CacheContainer.<Type, SupplierRegistration<?>>newConcurrentContainer(), CacheContainer.<Type, WatcherRegistration<?>>newNonConcurrentContainer());
    }

    public GuiceWatchableRegistrationContainer(Container<Type, SupplierRegistration<?>> supplierRegistry
            , Container<Type, WatcherRegistration<?>> watcherRegistry) {
        this.supplierRegistry = supplierRegistry;
        this.watcherRegistry = watcherRegistry;
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
    public <T> List<SupplierRegistration<T>> getAll(Key<T> key) {
        return getSupplierRegistrationsFor(key, supplierRegistry);
    }

    private <T> List<SupplierRegistration<T>> getSupplierRegistrationsFor(Key<T> key, Container<Type, SupplierRegistration<?>> registry) {
        List<SupplierRegistration<?>> pairs = registry.getAll(key.type());
        ImmutableList.Builder<SupplierRegistration<T>> suppliersByKey = ImmutableList.builder();
        for (SupplierRegistration<?> registryEntry : pairs) {
            addSupplierIfKeyEquals(key, suppliersByKey, registryEntry);
        }
        return suppliersByKey.build();
    }

    @SuppressWarnings("unchecked")
    private <T> void addSupplierIfKeyEquals(Key<T> key, ImmutableList.Builder<SupplierRegistration<T>> suppliersByKey, SupplierRegistration<?> registryEntry) {
        if (isKeyCompatibleToThisRegistration(key, registryEntry))
            suppliersByKey.add((SupplierRegistration<T>) registryEntry);
    }

    private <T> boolean isKeyCompatibleToThisRegistration(Key<T> key, Registration<?> registryEntry) {
        return key.annotationType() == null && key.type().equals(registryEntry.key().type())
                || key.equals(registryEntry.key());
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> SupplierRegistration<T> getFirst(Key<T> key) {
        List<SupplierRegistration<?>> all = supplierRegistry.getAll(key.type());
        for (SupplierRegistration<?> pair : all) {
            if (key.equals(pair.left)) {
                return (SupplierRegistration<T>) pair;
            }
        }
        return null;
    }

    @Override
    public boolean put(SupplierRegistration<?> registration) {
        updateWatcher(registration, Action.ADD);
        return putToRegistry(supplierRegistry, registration);
    }

    private <T> void updateWatcher(SupplierRegistration<T> registration, Action action) {
        Key<T> key = registration.key();
        List<WatcherRegistration<T>> watchers = getWatchers(key);
        for (WatcherRegistration<T> registryEntry : watchers) {
            fireAddToWatcherIfMatches(registryEntry, registration, action);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<WatcherRegistration<T>> getWatchers(Key<T> key) {
        ImmutableList.Builder<WatcherRegistration<T>> resultBuilder = ImmutableList.builder();
        List<WatcherRegistration<?>> watchers = watcherRegistry.getAll(key.type());
        for (WatcherRegistration<?> watcher : watchers) {
            if (isKeyCompatibleToThisRegistration(key, watcher)) {
                resultBuilder.add((WatcherRegistration<T>)watcher);
            }
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
    public <T> boolean add(WatcherRegistration<T> watcherRegistration) {
        for (SupplierRegistration<T> supplierRegistration : getAll(watcherRegistration.key())) {
            fireAddToWatcherIfMatches(watcherRegistration, supplierRegistration, Action.ADD);
        }
        return putToRegistry(watcherRegistry, watcherRegistration);
    }

    private <T> void fireAddToWatcherIfMatches(WatcherRegistration<T> watcherRegistration, SupplierRegistration<T> supplierRegistration, Action action) {
        if (watcherRegistration.left.matches(supplierRegistration.key())) {
            action.execute(watcherRegistration, supplierRegistration);
        }
    }

    @Override
    public boolean remove(WatcherRegistration<?> watcherRegistration) {
        return watcherRegistry.remove(getRegistryKey(watcherRegistration), watcherRegistration);
    }

    private Type getRegistryKey(Registration<?> watcherRegistration) {
        return watcherRegistration.key().type();
    }

    static GuiceWatchableRegistrationContainer newMultimapGuiceWatchableRegistrationContainer() {
        return new GuiceWatchableRegistrationContainer(ListMultimapContainer.<Type, SupplierRegistration<?>>newSynchronizedContainer(), ListMultimapContainer.<Type, WatcherRegistration<?>>newLockFreeContainer());
    }

    static GuiceWatchableRegistrationContainer newLoadingCacheGuiceWatchableRegistrationContainer() {
        return new GuiceWatchableRegistrationContainer(CacheContainer.<Type, SupplierRegistration<?>>newConcurrentContainer(), CacheContainer.<Type, WatcherRegistration<?>>newNonConcurrentContainer());
    }
}
