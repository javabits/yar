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

import java.util.concurrent.TimeUnit;

import static org.javabits.yar.IdMatchers.newKeyMatcher;
import static org.javabits.yar.guice.ExecutionStrategy.SYNCHRONOUS;
import static org.javabits.yar.guice.GuiceWatchableRegistrationContainer.newLoadingCacheGuiceWatchableRegistrationContainer;
import static org.javabits.yar.guice.GuiceWatchableRegistrationContainer.newMultimapGuiceWatchableRegistrationContainer;

/**
 * TODO comment
 * Date: 2/28/13
 * Time: 10:57 AM
 *
 * @author Romain Gilles
 */
public class BlockingSupplierRegistry extends SimpleRegistry implements org.javabits.yar.BlockingSupplierRegistry {
    public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;
    public static final long DEFAULT_TIMEOUT = 0L;

    private final long defaultTimeout;
    private final TimeUnit defaultTimeUnit;


    private BlockingSupplierRegistry(long defaultTimeout, TimeUnit defaultTimeUnit) {
        this.defaultTimeout = defaultTimeout;
        this.defaultTimeUnit = defaultTimeUnit;
    }

    private BlockingSupplierRegistry(WatchableRegistrationContainer registrationContainer, long defaultTimeout) {
        super(registrationContainer);
        this.defaultTimeout = defaultTimeout;
        defaultTimeUnit = DEFAULT_TIME_UNIT;
    }

    private BlockingSupplierRegistry(WatchableRegistrationContainer registrationContainer, long defaultTimeout, TimeUnit defaultTimeUnit) {
        super(registrationContainer);
        this.defaultTimeout = defaultTimeout;
        this.defaultTimeUnit = defaultTimeUnit;
    }

    @Override
    public <T> BlockingSupplier<T> get(Class<T> type) {
        return get(GuiceId.of(type));
    }

    @Override
    public <T> BlockingSupplier<T> get(Id<T> id) {
        BlockingSupplierImpl<T> supplier = new BlockingSupplierImpl<>();
        // If an instance of the requested service has been registered, this call will trigger the
        // listener's supplierChanged event with the current value of the service.
        // This is how the supplier instance obtains the initial value of the service.
        Registration<T> registration = addSupplierListener(newKeyMatcher(id), supplier);
        // preserve a reference to the registration to avoid gc and let the caller decides when listener can be gc.
        supplier.setSelfRegistration(registration);
        return supplier;
    }

    static BlockingSupplierRegistry newMultimapBlockingSupplierRegistry() {
        return newMultimapBlockingSupplierRegistry(DEFAULT_TIMEOUT);
    }

    static BlockingSupplierRegistry newMultimapBlockingSupplierRegistry(long defaultTimeout) {
        return new BlockingSupplierRegistry(newMultimapGuiceWatchableRegistrationContainer(), defaultTimeout);
    }

    static BlockingSupplierRegistry newLoadingCacheBlockingSupplierRegistry() {
        return newLoadingCacheBlockingSupplierRegistry(SYNCHRONOUS);
    }

    static BlockingSupplierRegistry newLoadingCacheBlockingSupplierRegistry(ExecutionStrategy executionStrategy) {
        return newLoadingCacheBlockingSupplierRegistry(DEFAULT_TIMEOUT, executionStrategy);
    }

    static BlockingSupplierRegistry newLoadingCacheBlockingSupplierRegistry(long defaultTimeout) {
        return newLoadingCacheBlockingSupplierRegistry(defaultTimeout, SYNCHRONOUS);
    }

    static BlockingSupplierRegistry newLoadingCacheBlockingSupplierRegistry(long defaultTimeout, ExecutionStrategy executionStrategy) {
        return new BlockingSupplierRegistry(newLoadingCacheGuiceWatchableRegistrationContainer(executionStrategy), defaultTimeout);
    }

    public static BlockingSupplierRegistry newBlockingSupplierRegistry() {
        return newBlockingSupplierRegistry(DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT);
    }

    public static BlockingSupplierRegistry newBlockingSupplierRegistry(long defaultTimeout, TimeUnit defaultTimeUnit) {
        return new BlockingSupplierRegistry(defaultTimeout, defaultTimeUnit);
    }
}
