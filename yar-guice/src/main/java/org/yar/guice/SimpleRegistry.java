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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.yar.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.*;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.transform;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.yar.guice.GuiceWatchableRegistrationContainer.newLoadingCacheGuiceWatchableRegistrationContainer;
import static org.yar.guice.GuiceWatchableRegistrationContainer.newMultimapGuiceWatchableRegistrationContainer;

/**
 * TODO comment
 * TODO implement it with watchers !!!
 * TODO have a look to the equinox implementation
 * Date: 2/10/13
 * Time: 10:47 PM
 *
 * @author Romain Gilles
 */
public class SimpleRegistry implements Registry {

    //TODO test synchronized Multimap vs MapMaker/CacheBuilder
//    ConcurrentMap<KEYTYPE, Set<VALUETYPE>> hashMap = new MapMaker()
//            .makeComputingMap(
//                    new Function<KEYTYPE, VALUETYPE>() {
//                        public Graph apply(KEYTYPE key) {
//                            return new HashSet<VALUETYPE>();
//                        }
//                    });

    private final LinkedBlockingQueue<RegistryAction> registryActionQueue;
    private final WatchableRegistrationContainer registrationContainer;
    public SimpleRegistry() {
        this(new GuiceWatchableRegistrationContainer());
    }

    SimpleRegistry(WatchableRegistrationContainer registrationContainer) {
        this.registrationContainer = registrationContainer;
        registryActionQueue = new LinkedBlockingQueue<>();
        Thread registryActionThread = new Thread(new RegistryActionHandler(registryActionQueue, registrationContainer));
        registryActionThread.setDaemon(true);
        registryActionThread.start();
    }

    @Override
    public <T> List<Supplier<T>> getAll(Class<T> type) {
        return copyOfEntries(registrationContainer.getAll(type));


    }
    private <T> List<Supplier<T>> copyOfEntries(List<SupplierRegistration<?>> pairs) {
        return ImmutableList.copyOf(transform(pairs, new Function<SupplierRegistration<?>, Supplier<T>>() {

            @Nullable
            @Override @SuppressWarnings("unchecked")
            public Supplier<T> apply(@Nullable SupplierRegistration<?> registration) {
                return (Supplier<T>)requireNonNull(registration, "registration").rightValue;
            }
        }));
    }

    @Override
    public <T> List<Supplier<T>> getAll(Key<T> key) {
        List<SupplierRegistration<T>> pairs = registrationContainer.getAll(key);

        return ImmutableList.copyOf(transform(pairs, new Function<SupplierRegistration<T>, Supplier<T>>() {
            @Nullable
            @Override @SuppressWarnings("unchecked")
            public Supplier<T> apply(@Nullable SupplierRegistration<T> registration) {
                return (Supplier<T>)requireNonNull(registration, "registration").rightValue;
            }
        }));

    }

    @Nullable
    @Override @SuppressWarnings("unchecked")
    public <T> Supplier<T> get(Class<T> type) {
        SupplierRegistration<?> registration = registrationContainer.getFirst(type);
        if (registration == null) {
            return null;
        }
        return (Supplier<T>) registration.rightValue;
    }

    @Nullable
    @Override @SuppressWarnings("unchecked")
    public <T> Supplier<T> get(Key<T> key) {
        SupplierRegistration<T> registration = registrationContainer.getFirst(key);
        if (registration == null) {
            return null;
        }
        return registration.rightValue;
    }

    @Override
    public <T> SupplierRegistration<T> put(Key<T> key, Supplier<T> supplier) {
        checkKey(key, "key");
        checkSupplier(supplier);
        SupplierRegistration<T> registration = new SupplierRegistration<>(key, supplier);
        Add add = new Add(registration);
        executeActionOnRegistry(add);
        return registration;
    }

    private <T> GuiceKey<T> checkKey(Key<T> watchedKey, String attribute) {
        requireNonNull(watchedKey, attribute);
        if (watchedKey instanceof GuiceKey) {
            return (GuiceKey<T>) watchedKey;
        } else {
            throw new IllegalArgumentException("The " + attribute + " parameter must be a GuiceKey instance");
        }
    }

    private <T> void checkSupplier(Supplier<? extends T> supplier) {
        requireNonNull(supplier, "supplier");
        checkGuiceSupplier(supplier);
    }

    private <T> void checkGuiceSupplier(Supplier<? extends T> supplier) {
        if (!isGuiceSupplier(supplier)) {
            throw new IllegalArgumentException(format("Only guice supplier is supported and not: %s", supplier.getClass()));
        }
    }

    private <T> boolean isGuiceSupplier(Supplier<? extends T> supplier) {
        return supplier instanceof GuiceSupplier;
    }

    @Override
    public void remove(org.yar.Registration<?> registration) {
        RegistryAction action = new Remove(checkSupplierRegistration(registration));
        executeActionOnRegistry(action);
    }


    private void executeActionOnRegistry(RegistryAction action) {
        try {
            registryActionQueue.put(action);
            if (!action.asFuture().get()) {
                throw new RuntimeException(String.format("Cannot execute action [%s] key [%s] on the registry", action.getClass().getSimpleName(), action.key()));
            }
        } catch (InterruptedException | ExecutionException e) {
            //TODO try again??? on interrupted?
            throw propagate(e);
        }
    }

    private SupplierRegistration checkSupplierRegistration(org.yar.Registration<?> registration) {
        return checkRegistration(registration, SupplierRegistration.class);
    }

    private <T extends AbstractRegistration> T checkRegistration(Registration<?> registration, Class<T> registrationClass) {
        requireNonNull(registration, "registration");

        if (!(registrationClass.isInstance(registration))) {
            throw new IllegalArgumentException(String.format("Only %s registration class are supported", registrationClass.getName()));
        }
        return registrationClass.cast(registration);
    }


    @Override
    public <T> Registration<T> addWatcher(Key<T> watchedKey, Watcher<Supplier<T>> watcher) {
        checkKey(watchedKey, "key");
        WatcherRegistration<T> watcherRegistration = new WatcherRegistration<>(watchedKey, watcher);
        executeActionOnRegistry(new AddWatcher<>(watcherRegistration));
        return watcherRegistration;
    }

    @Override
    public void removeWatcher(Registration<?> watcherRegistration) {
        WatcherRegistration<?> registration = checkRegistration(watcherRegistration, WatcherRegistration.class);
        RemoveWatcher<?> action = new RemoveWatcher<>(registration);
        executeActionOnRegistry(action);
    }

    static interface RegistryAction  {
        Key<?> key();
        void execute(WatchableRegistrationContainer registrationContainer);
        Future<Boolean> asFuture();

    }

    static abstract class AbstractAction implements RegistryAction {
        private final Registration<?> registration;
        AbstractAction(Registration<?> registration) {
            this.registration = registration;
        }
        @Override
        public Key<?> key() {
            return registration.key();
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
            for(;;) {
                try {
                    RegistryAction registryAction = registryActionQueue.take();
                    registryAction.execute(registrationContainer);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
