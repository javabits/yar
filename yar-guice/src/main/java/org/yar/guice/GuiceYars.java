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

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import org.yar.Registry;

/**
 * TODO comment
 * Date: 3/12/13
 * Time: 10:09 AM
 *
 * @author Romain Gilles
 */
public final class GuiceYars {
    private GuiceYars() {
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


    public static org.yar.BlockingRegistry newBlockingRegistry(long defaultTimeoutInMillis) {
        return newLoadingCacheBasedBlockingRegistry(defaultTimeoutInMillis);
    }

    public static org.yar.BlockingRegistry newLoadingCacheBasedBlockingRegistry(long defaultTimeoutInMillis) {
        return BlockingRegistry.newLoadingCacheBlockingRegistry(defaultTimeoutInMillis);
    }

    public static org.yar.BlockingRegistry newMultimapBasedBlockingRegistry(long defaultTimeoutInMillis) {
        return BlockingRegistry.newMultimapBlockingRegistry(defaultTimeoutInMillis);
    }


    public static org.yar.BlockingSupplierRegistry newBlockingSupplierRegistry(long defaultTimeoutInMillis) {
        return newLoadingCacheBasedBlockingSupplierRegistry(defaultTimeoutInMillis);
    }

    public static org.yar.BlockingSupplierRegistry newLoadingCacheBasedBlockingSupplierRegistry(long defaultTimeoutInMillis) {
        return BlockingSupplierRegistry.newLoadingCacheBlockingSupplierRegistry(defaultTimeoutInMillis);
    }

    public static org.yar.BlockingSupplierRegistry newLoadingCacheBasedBlockingSupplierRegistry(long defaultTimeoutInMillis, ExecutionStrategy executionStrategy) {
        return BlockingSupplierRegistry.newLoadingCacheBlockingSupplierRegistry(defaultTimeoutInMillis, executionStrategy);
    }

    public static org.yar.BlockingSupplierRegistry newLoadingCacheBlockingSupplierRegistry() {
        return BlockingSupplierRegistry.newLoadingCacheBlockingSupplierRegistry();
    }

    public static org.yar.BlockingSupplierRegistry newMultimapBasedBlockingSupplierRegistry(long defaultTimeoutInMillis) {
        return BlockingSupplierRegistry.newMultimapBlockingSupplierRegistry(defaultTimeoutInMillis);
    }

    public static Module newRegistryDeclarationModule(final Registry registry) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(Registry.class).toInstance(registry);
            }
        };
    }

    public static Module newRegistryDeclarationModule(final org.yar.BlockingSupplierRegistry registry) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                Key<org.yar.BlockingSupplierRegistry> blockingSupplierRegistryKey = Key.get(org.yar.BlockingSupplierRegistry.class);
                bind(Registry.class).to(blockingSupplierRegistryKey);
                bind(blockingSupplierRegistryKey).toInstance(registry);
            }
        };
    }

}
