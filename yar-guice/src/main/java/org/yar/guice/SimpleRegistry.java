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
import org.yar.Key;
import org.yar.Registry;
import org.yar.Supplier;

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

    private final ListMultimap<Type, Registration<?>> registry;
    private final LinkedBlockingQueue<RegistryAction> registryActionQueue;

    public SimpleRegistry() {
        registry = synchronizedListMultimap(ArrayListMultimap.<Type, Registration<?>>create());
        registryActionQueue = new LinkedBlockingQueue<>();
        Thread registryActionThread = new Thread(new RegistryActionHandler(registryActionQueue, registry));
        registryActionThread.setDaemon(true);
        registryActionThread.start();
    }

    @Override
    public <T> List<Supplier<T>> getAll(Class<T> type) {
        List<Registration<?>> pairs = registry.get(type);
        synchronized (registry) {
            return copyOfEntries(pairs);
        }

    }

    private <T> List<Supplier<T>> copyOfEntries(List<Registration<?>> pairs) {
        return ImmutableList.copyOf(transform(pairs, new Function<SimpleRegistry.Registration<?>, Supplier<T>>() {

            @Nullable
            @Override @SuppressWarnings("unchecked")
            public Supplier<T> apply(@Nullable SimpleRegistry.Registration<?> registration) {
                return (Supplier<T>)requireNonNull(registration, "registration").rightValue;
            }
        }));
    }

    @Override
    public <T> List<Supplier<T>> getAll(Key<T> key) {
        List<Registration<?>> pairs = registry.get(key.type());
        ImmutableList.Builder<Supplier<T>> suppliersByKey = ImmutableList.builder();
        synchronized (registry) {
            for (Registration<?> registryEntry : pairs) {
                addSupplierIfKeyEquals(key, suppliersByKey, registryEntry);
            }
        }
        return suppliersByKey.build();
    }

    @SuppressWarnings("unchecked")
    private <T> void addSupplierIfKeyEquals(Key<T> key, ImmutableList.Builder<Supplier<T>> suppliersByKey, Registration<?> registryEntry) {
        if (key.equals(registryEntry.leftValue))
            suppliersByKey.add((Supplier<T>) registryEntry.rightValue);
    }

    @Override @SuppressWarnings("unchecked")
    public <T> Supplier<T> get(Class<T> type) {
        List<Registration<?>> pairs = registry.get(type);
        synchronized (registry) {
            if (!pairs.isEmpty()) {
                return (Supplier<T>) pairs.get(0).rightValue;
            }
        }
        return null;
    }

    @Override @SuppressWarnings("unchecked")
    public <T> Supplier<T> get(Key<T> key) {
        List<Registration<?>> pairs = registry.get(key.type());
        synchronized (registry) {
            for (Registration<?> pair : pairs) {
                if (key.equals(pair.leftValue)) {
                    return (Supplier<T>) pair.rightValue;
                }
            }
        }
        return null;
    }

    @Override
    public <T> Registration<T> put(Key<T> key, Supplier<? extends T> supplier) {
        requireNonNull(key, "key");
        checkSupplier(supplier);
        Registration<T> registration = new Registration<>(key, supplier);
        Add add = new Add(registration);
        executeActionOnRegistry(add);
        return registration;
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

    private Registration checkRegistration(org.yar.Registration<?> registration) {
        requireNonNull(registration, "registration");
        if (!(registration instanceof Registration)) {
            throw new IllegalArgumentException(String.format("Only %s registration class are supported", Registration.class.getName()));
        }
        return Registration.class.cast(registration);
    }

    private static class Pair<L, R> {
        final L leftValue;
        final R rightValue;

        private Pair(L leftValue, R rightValue) {
            this.leftValue = leftValue;
            this.rightValue = rightValue;
        }
    }

    static class Registration<T> extends Pair<Key<T>, Supplier<? extends T>> implements org.yar.Registration<T> {
        private Registration(Key<T> leftValue, Supplier<? extends T> rightValue) {
            super(leftValue, rightValue);
        }

        @Override
        public Key<T> key() {
            return leftValue;
        }

        @Override
        public String toString() {
            return toStringHelper(SimpleRegistry.Registration.class)
                    .add("key", key())
                    .toString();
        }
    }

    static interface RegistryAction  {
        Key<?> key();
        void execute(ListMultimap<Type, Registration<?>> registry);
        Future<Boolean> asFuture();

    }

    static abstract class AbstractAction implements RegistryAction {
        final Registration<?> registration;
        public AbstractAction(Registration<?> registration) {
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


        public Add(Registration<?> registration) {
            super(registration);
            addCall = new AddCall();
            this.futureTask = new FutureTask<>(addCall);

        }

        @Override
        public void execute(ListMultimap<Type, Registration<?>> registry) {
            addCall.registration = registration;
            addCall.registry = registry;
            futureTask.run();
        }

        @Override
        public Future<Boolean> asFuture() {
            return futureTask;
        }

        private static class AddCall implements Callable<Boolean> {
            private ListMultimap<Type, Registration<?>> registry;
            private Registration<?> registration;
            @Override
            public Boolean call() throws Exception {
                return registry.put(registration.key().type(), registration);
            }
        }

    }

    static class Remove extends AbstractAction implements RegistryAction {
        private final FutureTask<Boolean> futureTask;
        private final RemoveCall removeCall;

        public Remove(Registration<?> registration) {
            super(registration);
            removeCall = new RemoveCall();
            this.futureTask = new FutureTask<>(removeCall);
        }

        @Override
        public void execute(ListMultimap<Type, Registration<?>> registry) {
            removeCall.registration = registration;
            removeCall.registry = registry;
            futureTask.run();
        }

        @Override
        public Future<Boolean> asFuture() {
            return futureTask;
        }

        private static class RemoveCall implements Callable<Boolean> {
            private ListMultimap<Type, Registration<?>> registry;
            private Registration<?> registration;
            @Override
            public Boolean call() throws Exception {
                return registry.remove(registration.key(), registration);
            }

        }

    }

    static class RegistryActionHandler implements Runnable {
        private final BlockingQueue<RegistryAction> registryActionQueue;
        private final ListMultimap<Type, Registration<?>> registry;

        public RegistryActionHandler(BlockingQueue<RegistryAction> registryActionQueue, ListMultimap<Type, Registration<?>> registry) {
            this.registryActionQueue = registryActionQueue;
            this.registry = registry;
        }

        @Override
        public void run() {
            for(;;) {
                try {
                    RegistryAction registryAction = registryActionQueue.take();
                    registryAction.execute(registry);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
