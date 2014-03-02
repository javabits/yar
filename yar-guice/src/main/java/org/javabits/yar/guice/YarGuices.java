/*
 * Copyright 2013 Romain Gilles
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javabits.yar.guice;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.javabits.yar.Registry.DEFAULT_TIMEOUT;
import static org.javabits.yar.Registry.DEFAULT_TIME_UNIT;
import static org.javabits.yar.guice.AbstractExecutionStrategy.newExecutionStrategy;
import static org.javabits.yar.guice.ExecutionStrategy.Type;
import static org.javabits.yar.guice.ExecutionStrategy.Type.PARALLEL;
import static org.javabits.yar.guice.ExecutionStrategy.Type.SERIALIZED;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.javabits.yar.BlockingSupplierRegistry;
import org.javabits.yar.Id;
import org.javabits.yar.Registry;

import com.google.common.collect.ImmutableList;
import com.google.inject.*;

/**
 * Utility class to construct registries.
 * 
 * @author Romain Gilles
 */
public final class YarGuices {
    private YarGuices() {
        throw new AssertionError("not for you");
    }

    public static Registry newSimpleRegistry() {
        return newLoadingCacheBasedRegistry();
    }

    public static Registry newLoadingCacheBasedRegistry() {
        return SimpleRegistry.newLoadingCacheRegistry();
    }

    public static Registry newMultimapBasedRegistry() {
        return SimpleRegistry.newMultimapRegistry();
    }

    public static BlockingSupplierRegistry newMultimapBlockingSupplierRegistry() {
        return BlockingSupplierRegistryImpl.newMultimapBlockingSupplierRegistry();
    }

    public static BlockingSupplierRegistry newLoadingCacheBlockingSupplierRegistry() {
        return BlockingSupplierRegistryImpl.newLoadingCacheBlockingSupplierRegistry();
    }

    public static BlockingSupplierRegistry newLoadingCacheBlockingSupplierRegistry(
            ExecutionStrategy executionStrategy) {
        return BlockingSupplierRegistryImpl
                .newLoadingCacheBlockingSupplierRegistry(executionStrategy);
    }

    public static Module newRegistryDeclarationModule(final Registry registry) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(Registry.class).toInstance(registry);
            }
        };
    }

    public static Module newRegistryDeclarationModule(final BlockingSupplierRegistry registry) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                Key<BlockingSupplierRegistry> blockingSupplierRegistryKey = Key
                        .get(BlockingSupplierRegistry.class);
                bind(Registry.class).to(blockingSupplierRegistryKey);
                bind(blockingSupplierRegistryKey).toInstance(registry);
            }
        };
    }

    public static Module newRegistryDeclarationModule(
            final Key<? extends BlockingSupplierRegistry> key) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                Key<BlockingSupplierRegistry> blockingSupplierRegistryKey = Key
                        .get(BlockingSupplierRegistry.class);
                bind(Registry.class).to(blockingSupplierRegistryKey);
                bind(blockingSupplierRegistryKey).to(key);
            }
        };
    }

    public static List<Id<?>> requiredSuppliers(Injector injector) {
        // RegistryProvider.class
        ImmutableList.Builder<Id<?>> requiredSuppliers = ImmutableList.builder();
        Map<Key<?>, Binding<?>> allBindings = injector.getAllBindings();
        for (Map.Entry<Key<?>, Binding<?>> bindingEntry : allBindings.entrySet()) {
            Provider<?> provider = bindingEntry.getValue().getProvider();
            if (provider instanceof RegistryProvider) {
                requiredSuppliers.add(GuiceId.of(bindingEntry.getKey()));
            }
        }
        return requiredSuppliers.build();
    }

    public static List<Id<?>> providedSuppliers(Injector injector) {
        return getIds(injector, RegistrationHandler.class);
    }

    public static List<Id<?>> registeredListener(Injector injector) {
        return getIds(injector, RegistryListenerHandler.class);
    }

    private static List<Id<?>> getIds(Injector injector, Class<? extends Handler> type) {
        Binding<? extends Handler> registryListenerHandlerBinding = injector.getExistingBinding(Key
                .get(type));
        if (registryListenerHandlerBinding.getProvider() == null) {
            return Collections.emptyList();
        } else {
            return registryListenerHandlerBinding.getProvider().get().ids();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ExecutionStrategy executionStrategy;
        private long timeout = DEFAULT_TIMEOUT;
        private TimeUnit unit = DEFAULT_TIME_UNIT;
        private BlockingSupplierFactory blockingSupplierFactory = new DefaultBlockingSupplierFactory();

        /**
         * Set the timeout value to use when executing concurrent methods (e.g. {@code Future},
         * {@code BlockingQueue} {@code ExecutorService} ...
         * 
         * @param timeout
         *            set the timeout value t
         * @return this {@code Builder}
         * @see #timeUnit(java.util.concurrent.TimeUnit)
         */
        public Builder timeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Set the unit to associate to the timeout. Default value
         * {@link Registry#DEFAULT_TIME_UNIT}
         * 
         * @param unit
         *            of the timeout
         * @return this {@code Builder}
         * @see #timeout(long)
         */
        public Builder timeUnit(TimeUnit unit) {
            this.unit = unit;
            return this;
        }

        /**
         * Enable the parallel update of the {@link org.javabits.yar.Watcher} and
         * {@link org.javabits.yar.SupplierListener} on {@link Registry} state change. Therefore
         * updates are executed within the same thread than the reactor thread in serial mode the
         * next listener is called after the completion of the previous one is reached or
         * cancellation occured.
         * 
         * @return this {@code Builder}
         */
        public Builder serializedListenerUpdate() {
            return listenerUpdateExecutionStrategy(SERIALIZED);
        }

        /**
         * Enable the parallel update of the {@link org.javabits.yar.Watcher} and
         * {@link org.javabits.yar.SupplierListener} on {@link Registry} state change. Therefore
         * this execution is executed within the same thread than the reactor thread in parallel
         * mode where all listener are updated in parallel but the next registry state is execute
         * only when all the previous listener updates are completed or cancelled by
         * 
         * @return this {@code Builder}
         */
        public Builder parallelListenerUpdate() {
            return listenerUpdateExecutionStrategy(PARALLEL);
        }

        /**
         * Set the execution strategy to apply on registry watcher / listener when un mutation
         * action is executed on the registry. The default strategy is a parallel execution.
         * 
         * @param executionStrategy
         *            to apply on {@link Registry} state change.
         * @return this {@code Builder}
         * @see #parallelListenerUpdate()
         * @see #serializedListenerUpdate()
         */
        public Builder listenerUpdateExecutionStrategy(Type executionStrategy) {
            checkExecutionStrategy();
            checkNotNull(executionStrategy, "executionStrategy");
            this.executionStrategy = newExecutionStrategy(executionStrategy);
            return this;
        }

        private void checkExecutionStrategy() {
            checkState(this.executionStrategy == null, "Execution strategy already define");
        }

        /**
         * Make the returned supplier no wait on any methods. and there for return directly a
         * {@code null} value \ if no suppliers are available.
         * 
         * @return this {@code Builder}
         * @see #blockingSupplierStrategy(BlockingSupplierFactory)
         */
        public Builder noWaitSupplier() {
            blockingSupplierFactory = new NoWaitBlockingSupplierFactory();
            return this;
        }

        /**
         * Define the blocking supplier factory to use (SPI). By default the blocking strategy will
         * block factory will create blocking supplier based on future.
         * <p>An alternative implementation can be the no wait strategy that will directly return
         * the underlying supplier.</p>.
         *
         * @param blockingSupplierFactory
         *            the blocking supplier strategy factory to use when lookup for supplier.
         *            The default strategy is the blocking one.
         * @return this {@code Builder}
         * @see #noWaitSupplier()
         */
        public Builder blockingSupplierStrategy(BlockingSupplierFactory blockingSupplierFactory) {
            this.blockingSupplierFactory = blockingSupplierFactory;
            return this;
        }

        public BlockingSupplierRegistry build() {
            return BlockingSupplierRegistryImpl.newLoadingCacheBlockingSupplierRegistry(
                    executionStrategy, timeout, unit, blockingSupplierFactory);
        }

        @Override
        public String toString() {
            return "Builder{" + "executionStrategy=" + executionStrategy + ", timeout=" + timeout
                    + ", unit=" + unit + ", blockingSupplierFactory=" + blockingSupplierFactory
                    + '}';
        }
    }
}
