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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;
import com.google.inject.Key;
import org.javabits.yar.*;

import javax.annotation.Nullable;
import java.lang.InterruptedException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.transform;
import static java.util.Objects.requireNonNull;
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

    private final LinkedBlockingQueue<RegistryAction> registryActionQueue;
    private final WatchableRegistrationContainer registrationContainer;
    private final FinalizableReferenceQueue referenceQueue;

    public SimpleRegistry() {
        this(new GuiceWatchableRegistrationContainer());
    }

    SimpleRegistry(WatchableRegistrationContainer registrationContainer) {
        referenceQueue = new FinalizableReferenceQueue();
        this.registrationContainer = registrationContainer;
        registryActionQueue = new LinkedBlockingQueue<>();
        Thread registryActionThread = new Thread(new RegistryActionHandler(registryActionQueue, registrationContainer), "yar-action-handler");
        registryActionThread.setDaemon(true);
        registryActionThread.start();
    }

    @Override
    public Set<Id<?>> ids() {
        return ImmutableSet.copyOf(Iterables.transform(registrationContainer.types(), new Function<Type, Id<?>>() {
            @Nullable
            @Override
            public Id<?> apply(@Nullable Type input) {
                return GuiceId.of(Key.get(input));
            }
        }));
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
    public <T> Supplier<T> get(Id<T> id) {
        SupplierRegistration<T> registration = registrationContainer.getFirst(id);
        if (registration == null) {
            return null;
        }
        return registration.right();
    }

    @Override
    public <T> SupplierRegistration<T> put(Id<T> id, Supplier<T> supplier) {
        checkKey(id, "id");
        checkSupplier(supplier);
        SupplierRegistration<T> registration = new SupplierRegistration<>(id, supplier);
        Add add = new Add(registration);
        executeActionOnRegistry(add);
        return registration;
    }

    @Override
    public <T> Registration<T> put(Id<T> id, com.google.common.base.Supplier<T> supplier) {
        return put(id, requireNonNull(new GuavaSupplierAdapter<>(supplier), "supplier"));
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
        RegistryAction action = new Remove(checkSupplierRegistration(registration));
        executeActionOnRegistry(action);
    }


    private void executeActionOnRegistry(RegistryAction action) {
        try {
            registryActionQueue.put(action);
            if (!action.asFuture().get()) {
                throw new RuntimeException(String.format("Cannot execute action [%s] id [%s] on the registry", action.getClass().getSimpleName(), action.id()));
            }
        } catch (InterruptedException | ExecutionException e) {
            //TODO try again??? on interrupted?
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
        WatcherRegistration<?> registration = checkRegistration(watcherRegistration, WatcherRegistration.class);
        RemoveWatcher<?> action = new RemoveWatcher<>(registration);
        executeActionOnRegistry(action);
    }

    @Override
    public void removeAll(Type type) {
        RegistryAction removeAllAction = new RemoveAll(type);
        executeActionOnRegistry(removeAllAction);
    }

    static interface RegistryAction {
        Id<?> id();

        void execute(WatchableRegistrationContainer registrationContainer);

        Future<Boolean> asFuture();

    }

    static abstract class AbstractAction implements RegistryAction {
        private final Registration<?> registration;

        AbstractAction(Registration<?> registration) {
            this.registration = registration;
        }

        @Override
        public Id<?> id() {
            return registration.id();
        }
    }

    static class Add extends AbstractAction implements RegistryAction {
        private final FutureTask<Boolean> futureTask;
        private final AddCall addCall;
        private final SupplierRegistration<?> registration;


        Add(SupplierRegistration<?> registration) {
            super(registration);
            this.registration = registration;
            addCall = new AddCall();
            this.futureTask = new FutureTask<>(addCall);

        }

        @Override
        public void execute(WatchableRegistrationContainer registrationContainer) {
            addCall.registration = registration;
            addCall.registrationContainer = registrationContainer;
            futureTask.run();
        }

        @Override
        public Future<Boolean> asFuture() {
            return futureTask;
        }

        private static class AddCall implements Callable<Boolean> {
            private WatchableRegistrationContainer registrationContainer;
            private SupplierRegistration<?> registration;

            @Override
            public Boolean call() throws Exception {
                return registrationContainer.put(registration);
            }
        }

    }

    static class Remove extends AbstractAction implements RegistryAction {
        private final FutureTask<Boolean> futureTask;
        private final RemoveCall removeCall;
        private final SupplierRegistration<?> registration;

        Remove(SupplierRegistration<?> registration) {
            super(registration);
            this.registration = registration;
            removeCall = new RemoveCall();
            this.futureTask = new FutureTask<>(removeCall);
        }

        @Override
        public void execute(WatchableRegistrationContainer registrationContainer) {
            removeCall.registration = registration;
            removeCall.registrationContainer = registrationContainer;
            futureTask.run();
        }

        @Override
        public Future<Boolean> asFuture() {
            return futureTask;
        }

        private static class RemoveCall implements Callable<Boolean> {
            private WatchableRegistrationContainer registrationContainer;
            private SupplierRegistration<?> registration;

            @Override
            public Boolean call() throws Exception {
                return registrationContainer.remove(registration);
            }
        }

    }

    static class RemoveAll implements RegistryAction {
        private final FutureTask<Boolean> futureTask;
        private final RemoveAllCall removeCall;
        private final Type type;

        RemoveAll(Type type) {
            this.type = type;
            removeCall = new RemoveAllCall();
            this.futureTask = new FutureTask<>(removeCall);
        }

        @Override
        public Id<?> id() {
            return GuiceId.of(type, null);
        }

        @Override
        public void execute(WatchableRegistrationContainer registrationContainer) {
            removeCall.registrationContainer = registrationContainer;
            removeCall.type = type;
            futureTask.run();
        }

        @Override
        public Future<Boolean> asFuture() {
            return futureTask;
        }

        private static class RemoveAllCall implements Callable<Boolean> {
            private WatchableRegistrationContainer registrationContainer;
            private Type type;

            @Override
            public Boolean call() throws Exception {
                return registrationContainer.removeAll(type);
            }
        }

    }

    private static class AddWatcher<T> extends AbstractAction implements RegistryAction {

        private final WatcherRegistration<T> watcherRegistration;
        private final AddWatcherCall addWatcherCall;
        private final FutureTask<Boolean> futureTask;

        public AddWatcher(WatcherRegistration<T> watcherRegistration) {
            super(watcherRegistration);
            this.watcherRegistration = watcherRegistration;
            addWatcherCall = new AddWatcherCall();
            this.futureTask = new FutureTask<>(addWatcherCall);
        }

        @Override
        public void execute(WatchableRegistrationContainer registrationContainer) {
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
                return registrationContainer.add(watcherRegistration);
            }
        }
    }

    private static class RemoveWatcher<T> extends AbstractAction implements RegistryAction {

        private final WatcherRegistration<T> watcherRegistration;
        private final RemoveWatcherCall removeWatcherCall;
        private final FutureTask<Boolean> futureTask;

        public RemoveWatcher(WatcherRegistration<T> watcherRegistration) {
            super(watcherRegistration);
            this.watcherRegistration = watcherRegistration;
            removeWatcherCall = new RemoveWatcherCall();
            this.futureTask = new FutureTask<>(removeWatcherCall);
        }

        @Override
        public void execute(WatchableRegistrationContainer registrationContainer) {
            removeWatcherCall.registrationContainer = registrationContainer;
            futureTask.run();
        }

        @Override
        public Future<Boolean> asFuture() {
            return futureTask;
        }

        private class RemoveWatcherCall implements Callable<Boolean> {
            private WatchableRegistrationContainer registrationContainer;

            @Override
            public Boolean call() throws Exception {
                return registrationContainer.remove(watcherRegistration);
            }
        }
    }

    static class RegistryActionHandler implements Runnable {
        private final BlockingQueue<RegistryAction> registryActionQueue;
        private final WatchableRegistrationContainer registrationContainer;

        RegistryActionHandler(BlockingQueue<RegistryAction> registryActionQueue, WatchableRegistrationContainer registrationContainer) {
            this.registryActionQueue = registryActionQueue;
            this.registrationContainer = registrationContainer;
        }

        @Override
        public void run() {
            try {
                for (; !Thread.currentThread().isInterrupted() ; ) {
                    RegistryAction registryAction = registryActionQueue.take();
                    registryAction.execute(registrationContainer);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
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
