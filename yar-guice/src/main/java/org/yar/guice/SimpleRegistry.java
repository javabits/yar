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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import org.yar.*;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.*;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Multimaps.synchronizedListMultimap;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

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
        registryActionQueue = new LinkedBlockingQueue<>();
        registrationContainer = new MultimapListWatchableRegistrationContainer();
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

    @Override @SuppressWarnings("unchecked")
    public <T> Supplier<T> get(Class<T> type) {
        SupplierRegistration<?> registration = registrationContainer.getFirst(type);
        return (Supplier<T>) registration.rightValue;
    }

    @Override @SuppressWarnings("unchecked")
    public <T> Supplier<T> get(Key<T> key) {
        SupplierRegistration<T> registration = registrationContainer.getFirst(key);
        return (Supplier<T>) registration.rightValue;
    }

    @Override
    public <T> SupplierRegistration<T> put(Key<T> key, Supplier<? extends T> supplier) {
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
        RegistryAction action = new Remove(checkRegistration(registration));
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

    private SupplierRegistration checkRegistration(org.yar.Registration<?> registration) {
        requireNonNull(registration, "registration");
        if (!(registration instanceof SupplierRegistration)) {
            throw new IllegalArgumentException(String.format("Only %s registration class are supported", SupplierRegistration.class.getName()));
        }
        return SupplierRegistration.class.cast(registration);
    }


    @Override
    public <T> void addWatcher(Key<T> watchedKey, Watcher<? extends T> watcher) {
        checkKey(watchedKey, "key");
        WatcherRegistration<T> watcherRegistration = new WatcherRegistration<>(watchedKey, watcher);
        executeActionOnRegistry(new AddWatcher<>(watcherRegistration));
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void removeWatcher(Registration<?> watcherRegistration) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    static class WatcherRegistration<T> extends Pair<Key<T>, Watcher<? extends T>> implements org.yar.Registration<T> {
        private WatcherRegistration(Key<T> leftValue, Watcher<? extends T> rightValue) {
            super(leftValue, rightValue);
        }

        @Override
        public Key<T> key() {
            return leftValue;
        }

        @Override
        public String toString() {
            return toStringHelper(SupplierRegistration.class)
                    .add("key", key())
                    .toString();
        }
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

    private class AddWatcher<T> extends AbstractAction implements RegistryAction {

        private final WatcherRegistration<T> watcherRegistration;

        public AddWatcher(WatcherRegistration<T> watcherRegistration) {
            super(watcherRegistration);
            this.watcherRegistration = watcherRegistration;
        }

        @Override
        public void execute(WatchableRegistrationContainer registrationContainer) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Future<Boolean> asFuture() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
