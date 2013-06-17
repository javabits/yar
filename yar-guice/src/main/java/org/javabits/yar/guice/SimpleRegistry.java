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

import com.google.common.base.FinalizableReferenceQueue;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.google.common.reflect.TypeToken;
import org.javabits.yar.*;

import javax.annotation.Nullable;
import java.lang.InterruptedException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.transform;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static org.javabits.yar.InterruptedException.newInterruptedException;
import static org.javabits.yar.guice.GuiceWatchableRegistrationContainer.newLoadingCacheGuiceWatchableRegistrationContainer;
import static org.javabits.yar.guice.GuiceWatchableRegistrationContainer.newMultimapGuiceWatchableRegistrationContainer;
import static org.javabits.yar.guice.WatcherRegistration.newWatcherRegistration;

/**
 * TODO comment
 * TODO implement it with watchers !!!
 * TODO have a look to the equinox implementation
 * Date: 2/10/13
 * Time: 10:47 PM
 *
 * @author Romain Gilles
 */
public class SimpleRegistry implements Registry, RegistryHook {
    private static final Logger LOG = Logger.getLogger(SimpleRegistry.class.getName());
    private final LinkedBlockingQueue<RegistryAction> registryActionQueue;
    private final WatchableRegistrationContainer registrationContainer;
    private final FinalizableReferenceQueue referenceQueue;
    private final long defaultTimeOut;
    private final TimeUnit defaultTimeoutUnit;

    public SimpleRegistry() {
        this(new GuiceWatchableRegistrationContainer());
    }

    SimpleRegistry(WatchableRegistrationContainer registrationContainer) {
        this(registrationContainer, Registry.DEFAULT_TIMEOUT, Registry.DEFAULT_TIME_UNIT);
    }

    SimpleRegistry(WatchableRegistrationContainer registrationContainer, long timeout, TimeUnit unit) {
        referenceQueue = new FinalizableReferenceQueue();
        this.registrationContainer = registrationContainer;
        registryActionQueue = new LinkedBlockingQueue<>();
        Thread registryActionThread = new Thread(new RegistryActionHandler(registryActionQueue), "yar-action-handler");
        registryActionThread.setDaemon(true);
        registryActionThread.start();
        this.defaultTimeOut = timeout;
        this.defaultTimeoutUnit = unit;
    }


    @Override
    public Set<Type> types() {
        return unmodifiableSet(registrationContainer.types());
    }

    @Override
    public Set<Id<?>> ids() {
        ImmutableSet.Builder<Id<?>> builder = ImmutableSet.builder();
        for (Type type : registrationContainer.types()) {
            builder.addAll(Lists.transform(getAll(TypeToken.of(type)), new Function<Supplier<?>, Id<?>>() {
                @Nullable
                @Override
                public Id<?> apply(@Nullable Supplier<?> supplier) {
                    requireNonNull(supplier, "supplier");
                    return supplier.id();
                }
            }));
        }
        return builder.build();
    }

    @Override
    public <T> List<Supplier<T>> getAll(Class<T> type) {
        return viewOfEntries(registrationContainer.getAll(type));
    }

    private static <T> List<Supplier<T>> viewOfEntries(List<SupplierRegistration<?>> pairs) {
        return transform(pairs, new Function<SupplierRegistration<?>, Supplier<T>>() {

            @Nullable
            @Override
            @SuppressWarnings("unchecked")
            public Supplier<T> apply(@Nullable SupplierRegistration<?> registration) {
                return (Supplier<T>) requireNonNull(registration, "registration").right();
            }
        });
    }

    @Override
    public <T> List<Supplier<T>> getAll(Id<T> id) {
        List<SupplierRegistration<T>> pairs = registrationContainer.getAll(id);
        return transformToSuppliers(pairs);

    }

    @Override
    public <T> List<Supplier<T>> getAll(TypeToken<T> typeToken) {
        return viewOfEntries(registrationContainer.getAll(typeToken.getType()));
    }


    private static <T> ImmutableList<Supplier<T>> transformToSuppliers(List<SupplierRegistration<T>> pairs) {
        return ImmutableList.copyOf(transform(pairs, new Function<SupplierRegistration<T>, Supplier<T>>() {
            @Nullable
            @Override
            @SuppressWarnings("unchecked")
            public Supplier<T> apply(@Nullable SupplierRegistration<T> registration) {
                return requireNonNull(registration, "registration").right();
            }
        }));
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> Supplier<T> get(Class<T> type) {
        SupplierRegistration<?> registration = registrationContainer.getFirst(type);
        if (registration == null) {
            return null;
        }
        return (Supplier<T>) registration.right();
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> Supplier<T> get(TypeToken<T> type) {
        SupplierRegistration<?> registration = registrationContainer.getFirst(type.getType());
        if (registration == null) {
            return null;
        }
        return (Supplier<T>) registration.right();
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> Supplier<T> get(Id<T> id) {
        return getDirectly(id);
    }

    final <T> Supplier<T> getDirectly(Id<T> id) {
        SupplierRegistration<T> registration = registrationContainer.getFirst(id);
        if (registration == null) {
            return null;
        }
        return registration.right();
    }

    private <T> SupplierRegistration<T> put(Id<T> id, Supplier<T> supplier) {
        checkKey(id, "id");
        checkSupplier(supplier);
        SupplierRegistration<T> registration = new SupplierRegistration<>(id, supplier);
        Add add = new Add(registration);
        executeActionOnRegistry(add);
        return registration;
    }

    @Override
    public <T> Registration<T> put(Id<T> id, com.google.common.base.Supplier<T> supplier) {
        return put(id, requireNonNull(new GuavaSupplierAdapter<>(id, supplier), "supplier"));
    }

    private <T> GuiceId<T> checkKey(Id<T> watchedId, String attribute) {
        requireNonNull(watchedId, attribute);
        if (watchedId instanceof GuiceId) {
            return (GuiceId<T>) watchedId;
        } else {
            throw new IllegalArgumentException("The " + attribute + " parameter must be a GuiceId instance");
        }
    }

    private <T> void checkSupplier(Supplier<? extends T> supplier) {
        requireNonNull(supplier, "supplier");
    }

    @Override
    public void remove(org.javabits.yar.Registration<?> registration) {
        removeAll(singletonList(registration));
    }

    @Override
    public void removeAll(Collection<? extends Registration<?>> registrations) {
        Collection<SupplierRegistration<?>> supplierRegistrations = Collections2.transform(registrations, new Function<Registration<?>, SupplierRegistration<?>>() {
            @Nullable
            @Override
            public SupplierRegistration<?> apply(@Nullable Registration<?> registration) {
                return checkSupplierRegistration(registration);
            }
        });
        RegistryAction action = new Remove(supplierRegistrations);
        executeActionOnRegistry(action);
    }

    private void executeActionOnRegistry(RegistryAction action) {
        try {
            registryActionQueue.put(action);
            if (!action.asFuture().get()) {
                LOG.warning(String.format("Cannot execute action %s on the registry", action));
            }
        } catch (InterruptedException e) {
            throw newInterruptedException(String.format("Cannot execute action [%s] on the registry", action), e);
        } catch (ExecutionException e) {
            throw propagate(e);
        }
    }

    private SupplierRegistration<?> checkSupplierRegistration(org.javabits.yar.Registration<?> registration) {
        return checkRegistration(registration, SupplierRegistration.class);
    }

    private <T extends Registration> T checkRegistration(Registration<?> registration, Class<T> registrationClass) {
        requireNonNull(registration, "registration");

        if (!(registrationClass.isInstance(registration))) {
            throw new IllegalArgumentException(String.format("Only %s registration class are supported", registrationClass.getName()));
        }
        return registrationClass.cast(registration);
    }

    @Override
    public <T> Registration<T> addWatcher(IdMatcher<T> idMatcher, Watcher<T> watcher) {
        checkKeyMatcher(idMatcher, "idMatcher");
        WatcherRegistration<T> watcherRegistration = newWatcherRegistration(idMatcher, watcher, referenceQueue, this);
        return addWatcherRegistration(watcherRegistration);
    }

    <T> Registration<T> addWatcherRegistration(WatcherRegistration<T> watcherRegistration) {
        executeActionOnRegistry(new AddWatcher<>(watcherRegistration));
        return watcherRegistration;
    }

    <T> Registration<T> addSupplierListener(IdMatcher<T> idMatcher, SupplierListener supplierListener) {
        checkKeyMatcher(idMatcher, "idMatcher");
        requireNonNull(supplierListener, "supplierListener");
        WatcherRegistration<T> watcherRegistration = newWatcherRegistration(idMatcher, supplierListener, referenceQueue, this);
        return addWatcherRegistration(watcherRegistration);
    }

    private <T> IdMatcher<T> checkKeyMatcher(IdMatcher<T> matcher, String attribute) {
        return requireNonNull(matcher, attribute);
    }

    @Override
    public void removeWatcher(Registration<?> watcherRegistration) {
        removeAllWatchers(Collections.singletonList(watcherRegistration));
    }

    @Override
    public void removeAllWatchers(Collection<? extends Registration<?>> watcherRegistrations) {
        Collection<WatcherRegistration<?>> registrations = Collections2.transform(watcherRegistrations, new Function<Registration<?>, WatcherRegistration<?>>() {
            @Nullable
            @Override
            public WatcherRegistration<?> apply(@Nullable Registration<?> watcherRegistration) {
                return checkRegistration(watcherRegistration, WatcherRegistration.class);
            }
        });
        RemoveWatcher action = new RemoveWatcher(registrations);
        executeActionOnRegistry(action);
    }

    @Override
    public void invalidate(Type type) {
        invalidateAll(singletonList(type));
    }

    @Override
    public void invalidateAll(Collection<Type> types) {
        RegistryAction removeAllAction = new InvalidateType(types);
        executeActionOnRegistry(removeAllAction);
    }

    @Override
    public void addTypeListener(TypeListener typeListener) {
        registrationContainer.addTypeListener(typeListener);
    }

    @Override
    public void removeTypeListener(TypeListener typeListener) {
        registrationContainer.removeTypeListener(typeListener);
    }

    static interface RegistryAction {

        void execute();

        Future<Boolean> asFuture();

    }

    private class Add implements RegistryAction {
        private final FutureTask<Boolean> futureTask;
        private final AddCall addCall;
        private final SupplierRegistration<?> registration;


        Add(SupplierRegistration<?> registration) {
            this.registration = registration;
            addCall = new AddCall();
            this.futureTask = new FutureTask<>(addCall);

        }

        @Override
        public void execute() {
            addCall.registration = registration;
            futureTask.run();
        }

        @Override
        public Future<Boolean> asFuture() {
            return futureTask;
        }

        private class AddCall implements Callable<Boolean> {
            private SupplierRegistration<?> registration;

            @Override
            public Boolean call() throws Exception {
                return registrationContainer.put(registration, defaultTimeOut, defaultTimeoutUnit);
            }
        }

    }

    private class Remove implements RegistryAction {
        private final FutureTask<Boolean> futureTask;
        private final RemoveCall removeCall;
        private final Iterable<SupplierRegistration<?>> registrations;

        Remove(Iterable<SupplierRegistration<?>> registrations) {
            this.registrations = registrations;
            removeCall = new RemoveCall();
            this.futureTask = new FutureTask<>(removeCall);
        }

        @Override
        public void execute() {
            futureTask.run();
        }

        @Override
        public Future<Boolean> asFuture() {
            return futureTask;
        }

        private class RemoveCall implements Callable<Boolean> {
            @Override
            public Boolean call() throws Exception {
                for (SupplierRegistration<?> registration : registrations) {
                    registrationContainer.remove(registration, defaultTimeOut, defaultTimeoutUnit);
                }
                return true;
            }
        }

    }

    class InvalidateType implements RegistryAction {
        private final FutureTask<Boolean> futureTask;
        private final RemoveAllCall removeCall;
        private final Iterable<Type> types;

        InvalidateType(Iterable<Type> types) {
            Preconditions.checkArgument(!Iterables.isEmpty(types), "No null or empty types list");
            this.types = types;
            this.removeCall = new RemoveAllCall();
            this.futureTask = new FutureTask<>(removeCall);
        }

        @Override
        public void execute() {
            futureTask.run();
        }

        @Override
        public Future<Boolean> asFuture() {
            return futureTask;
        }

        private class RemoveAllCall implements Callable<Boolean> {

            @Override
            public Boolean call() throws Exception {
                for (Type type : types) {
                    registrationContainer.removeAll(type, defaultTimeOut, defaultTimeoutUnit);
                }
                return TRUE;
            }
        }

    }


    private class AddWatcher<T> implements RegistryAction {

        private final WatcherRegistration<T> watcherRegistration;
        private final AddWatcherCall addWatcherCall;
        private final FutureTask<Boolean> futureTask;

        public AddWatcher(WatcherRegistration<T> watcherRegistration) {
            this.watcherRegistration = watcherRegistration;
            addWatcherCall = new AddWatcherCall();
            this.futureTask = new FutureTask<>(addWatcherCall);
        }

        @Override
        public void execute() {
            addWatcherCall.registrationContainer = registrationContainer;
            futureTask.run();
        }

        @Override
        public Future<Boolean> asFuture() {
            return futureTask;
        }

        private class AddWatcherCall implements Callable<Boolean> {
            private WatchableRegistrationContainer registrationContainer;

            @Override
            public Boolean call() throws Exception {
                return registrationContainer.add(watcherRegistration, defaultTimeOut, defaultTimeoutUnit);
            }
        }
    }

    private class RemoveWatcher implements RegistryAction {
        private final Collection<WatcherRegistration<?>> watcherRegistrations;
        private final RemoveWatcherCall removeWatcherCall;
        private final FutureTask<Boolean> futureTask;

        public RemoveWatcher(Collection<WatcherRegistration<?>> watcherRegistrations) {
            this.watcherRegistrations = watcherRegistrations;
            removeWatcherCall = new RemoveWatcherCall();
            this.futureTask = new FutureTask<>(removeWatcherCall);
        }

        @Override
        public void execute() {
            futureTask.run();
        }

        @Override
        public Future<Boolean> asFuture() {
            return futureTask;
        }

        private class RemoveWatcherCall implements Callable<Boolean> {
            @Override
            public Boolean call() throws Exception {
                for (WatcherRegistration<?> watcherRegistration : watcherRegistrations) {
                    registrationContainer.remove(watcherRegistration);
                }
                return TRUE;
            }
        }
    }

    private static class RegistryActionHandler implements Runnable {
        private static final Logger LOG = Logger.getLogger(RegistryActionHandler.class.getName());
        private final BlockingQueue<RegistryAction> registryActionQueue;

        RegistryActionHandler(BlockingQueue<RegistryAction> registryActionQueue) {
            this.registryActionQueue = registryActionQueue;
        }

        @Override
        public void run() {
            try {
                try {
                    for (; !Thread.currentThread().isInterrupted(); ) {
                        RegistryAction registryAction = registryActionQueue.take();
                        registryAction.execute();
                    }
                } catch (InterruptedException e) {
                    LOG.fine("Exit on interruption");
                }
                for (RegistryAction registryAction = registryActionQueue.poll(); registryAction != null; ) {
                    try {
                        registryAction.asFuture().cancel(true);
                    } catch (Exception ex) {
                        LOG.log(Level.SEVERE, "Error on cancel to exit on interruption", ex);
                    }
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Exit on exception", e);
            }
        }
    }

    static SimpleRegistry newMultimapRegistry() {
        return new SimpleRegistry(newMultimapGuiceWatchableRegistrationContainer());
    }

    static SimpleRegistry newLoadingCacheRegistry() {
        return new SimpleRegistry(newLoadingCacheGuiceWatchableRegistrationContainer());
    }
}
