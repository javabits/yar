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

import org.javabits.yar.BlockingSupplier;
import org.javabits.yar.Id;
import org.javabits.yar.Registration;
import org.javabits.yar.TypeListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.javabits.yar.IdMatchers.newKeyMatcher;
import static org.javabits.yar.guice.ExecutionStrategy.SERIALIZED;
import static org.javabits.yar.guice.GuiceWatchableRegistrationContainer.newLoadingCacheGuiceWatchableRegistrationContainer;
import static org.javabits.yar.guice.GuiceWatchableRegistrationContainer.newMultimapGuiceWatchableRegistrationContainer;

/**
 * TODO comment
 * Date: 2/28/13
 * Time: 10:57 AM
 *
 * @author Romain Gilles
 */
class BlockingSupplierRegistryImpl extends SimpleRegistry implements org.javabits.yar.BlockingSupplierRegistry {

    private final BlockingSupplierFactory blockingSupplierFactory;

    private BlockingSupplierRegistryImpl(WatchableRegistrationContainer registrationContainer) {
        super(registrationContainer);
        this.blockingSupplierFactory = new DefaultBlockingSupplierFactory();
    }

    private BlockingSupplierRegistryImpl(WatchableRegistrationContainer registrationContainer, long timeout, TimeUnit unit) {
        this(registrationContainer, timeout, unit, new DefaultBlockingSupplierFactory());
    }

    private BlockingSupplierRegistryImpl(WatchableRegistrationContainer registrationContainer, long timeout, TimeUnit unit, BlockingSupplierFactory blockingSupplierFactory) {
        super(registrationContainer, timeout, unit);
        this.blockingSupplierFactory = blockingSupplierFactory;
    }

    @Override
    public <T> BlockingSupplier<T> get(Class<T> type) {
        return get(GuiceId.of(type));
    }

    @Override
    public <T> BlockingSupplier<T> get(Id<T> id) {
        return blockingSupplierFactory.create(this, id);
    }


    static BlockingSupplierRegistryImpl newMultimapBlockingSupplierRegistry() {
        return new BlockingSupplierRegistryImpl(newMultimapGuiceWatchableRegistrationContainer());
    }

    static BlockingSupplierRegistryImpl newLoadingCacheBlockingSupplierRegistry() {
        return newLoadingCacheBlockingSupplierRegistry(SERIALIZED);
    }

    static BlockingSupplierRegistryImpl newLoadingCacheBlockingSupplierRegistry(long timeout, TimeUnit unit) {
        return newLoadingCacheBlockingSupplierRegistry(SERIALIZED, timeout, unit);
    }

    static BlockingSupplierRegistryImpl newLoadingCacheBlockingSupplierRegistry(ExecutionStrategy executionStrategy, long timeout, TimeUnit unit) {
        return new BlockingSupplierRegistryImpl(newLoadingCacheGuiceWatchableRegistrationContainer(executionStrategy), timeout, unit, new DefaultBlockingSupplierFactory());
    }

    static BlockingSupplierRegistryImpl newLoadingCacheBlockingSupplierRegistry(ExecutionStrategy executionStrategy, long timeout, TimeUnit unit, BlockingSupplierFactory blockingSupplierFactory) {
        return new BlockingSupplierRegistryImpl(newLoadingCacheGuiceWatchableRegistrationContainer(executionStrategy), timeout, unit, blockingSupplierFactory);
    }

    static BlockingSupplierRegistryImpl newLoadingCacheBlockingSupplierRegistry(ExecutionStrategy executionStrategy) {
        return new BlockingSupplierRegistryImpl(newLoadingCacheGuiceWatchableRegistrationContainer(executionStrategy));
    }

    public static BlockingSupplierRegistryImpl newBlockingSupplierRegistry() {
        return newLoadingCacheBlockingSupplierRegistry();
    }

    public static BlockingSupplierRegistryImpl newBlockingSupplierRegistry(long timeout, TimeUnit unit) {
        return newLoadingCacheBlockingSupplierRegistry(timeout, unit);
    }
}
