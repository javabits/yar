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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.javabits.yar.Id;
import org.javabits.yar.Registration;
import org.javabits.yar.RegistryHook;
import org.javabits.yar.TypeListener;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.javabits.yar.TypeEvent.newAddTypeEvent;
import static org.javabits.yar.TypeEvent.newRemoveTypeEvent;
import static org.javabits.yar.guice.AbstractExecutionStrategy.newExecutionStrategy;
import static org.javabits.yar.guice.CacheContainer.KeyConversionStrategies.TYPE_ERASURE;
import static org.javabits.yar.guice.ExecutionStrategy.Type.*;
import static org.javabits.yar.guice.Reflections.getRawType;

/**
 * TODO comment
 * Date: 2/20/13
 * Time: 9:47 AM
 *
 * @author Romain Gilles
 */
public class GuiceWatchableRegistrationContainer implements WatchableRegistrationContainer {
    private static final Logger LOG = Logger.getLogger(GuiceWatchableRegistrationContainer.class.getName());
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


    GuiceWatchableRegistrationContainer() {
        this(CacheContainer.newConcurrentContainer(), CacheContainer.newNonConcurrentContainer(TYPE_ERASURE), newExecutionStrategy(SERIALIZED));
    }

    private GuiceWatchableRegistrationContainer(Container<Type, SupplierRegistration<?>> supplierRegistry
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
        LOG.log(Level.FINE, ()-> "Execute " + action + " on watchers: " + watcherRegistrations + ", for given supplier " + supplierRegistration );
        executor.execute(getUpdateActionsToExistingWatcherOnSupplierEvent(supplierRegistration, action, watcherRegistrations),timeout, unit);
    }

    private <T> List<Callable<Void>> getUpdateActionsToExistingWatcherOnSupplierEvent(final SupplierRegistration<T> supplierRegistration, final Action action, List<WatcherRegistration<T>> watcherRegistrations) {
        return Lists.transform(watcherRegistrations, watcherRegistration -> new UpdateWatcherOnSupplierEvent<>(watcherRegistration, supplierRegistration, action));
    }

    //returns all the watchers associated to the type of the given id.
    @SuppressWarnings("unchecked")
    private <T> List<WatcherRegistration<T>> getWatcherRegistrations(Id<T> id) {
        return (List<WatcherRegistration<T>>)((Container)watcherRegistry).getAll(id.type());
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

    @SuppressWarnings("unchecked")
    private <T> List<Callable<Void>> getAddSupplierActionsToNewWatcher(final WatcherRegistration<T> watcherRegistration) {
        final Class<?> watcherRawType = getRawType(watcherRegistration.id().type());
        Set<Type> watcherCompatiblesTypes = Sets.filter(types(), type -> watcherRawType.equals(getRawType(type)));
        ImmutableList.Builder<SupplierRegistration<T>> resultBuilder = ImmutableList.builder();
        for (Type watcherCompatiblesType : watcherCompatiblesTypes) {
            resultBuilder.addAll((List)getAll(watcherCompatiblesType));
        }
        List < SupplierRegistration < T >> supplierRegistrations = resultBuilder.build();
        return Lists.transform(supplierRegistrations, supplierRegistration -> new AddToNewWatcher<>(watcherRegistration, supplierRegistration));
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

    @Override
    public boolean hasPendingListenerUpdateTasks() {
        return executor.hasPendingTasks();
    }

    @Override
    public void addEndOfListenerUpdateTasksListener(RegistryHook.EndOfListenerUpdateTasksListener listener) {
        executor.addEndOfListenerUpdateTasksListener(listener);
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
            return this.getClass().getSimpleName() + "{" +
                    "action=" + action +
                    ", watcherRegistration=" + watcherRegistration +
                    ", supplierRegistration=" + supplierRegistration +
                    '}';
        }

        private static <T> void fireAddToWatcherIfMatches(WatcherRegistration<T> watcherRegistration, SupplierRegistration<T> supplierRegistration, Action action) {
            if (watcherRegistration.left().matches(supplierRegistration.id())) {
                action.execute(watcherRegistration, supplierRegistration);
            }
        }
    }

    static class AddToNewWatcher<T> extends ActionAdapter<T> {
        AddToNewWatcher(WatcherRegistration<T> watcherRegistration, SupplierRegistration<T> supplierRegistration) {
            super(watcherRegistration, supplierRegistration, Action.ADD);
        }
    }

    static class UpdateWatcherOnSupplierEvent<T> extends ActionAdapter<T> {
        UpdateWatcherOnSupplierEvent(WatcherRegistration<T> watcherRegistration, SupplierRegistration<T> supplierRegistration, Action action) {
            super(watcherRegistration, supplierRegistration, action);
        }
    }


    @Override
    public boolean remove(WatcherRegistration<?> watcherRegistration) {
        return watcherRegistry.remove(getRegistryKey(watcherRegistration), watcherRegistration);
    }

    @Override
    public void removeAll(Type type, long timeout, TimeUnit unit) throws InterruptedException {
        List<SupplierRegistration<?>> all = getAll(type);
        for (SupplierRegistration<?> supplierRegistration: all) {
            remove(supplierRegistration,timeout, unit);
        }
        watcherRegistry.invalidate(type);
        supplierRegistry.invalidate(type);
    }

    private Type getRegistryKey(Registration<?> watcherRegistration) {
        return watcherRegistration.id().type();
    }

    static GuiceWatchableRegistrationContainer newMultimapGuiceWatchableRegistrationContainer() {
        return new GuiceWatchableRegistrationContainer(ListMultimapContainer.newSynchronizedContainer(), ListMultimapContainer.newLockFreeContainer(), newExecutionStrategy(SERIALIZED));
    }

    static GuiceWatchableRegistrationContainer newLoadingCacheGuiceWatchableRegistrationContainer() {
        return newLoadingCacheGuiceWatchableRegistrationContainer(newExecutionStrategy(SERIALIZED));
    }

    static GuiceWatchableRegistrationContainer newLoadingCacheGuiceWatchableRegistrationContainer(ExecutionStrategy executionStrategy) {
        return new GuiceWatchableRegistrationContainer(CacheContainer.newConcurrentContainer(), CacheContainer.newNonConcurrentContainer(TYPE_ERASURE), executionStrategy);
    }
}
