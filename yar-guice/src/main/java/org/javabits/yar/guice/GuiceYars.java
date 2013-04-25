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
import com.google.inject.*;
import org.javabits.yar.Id;
import org.javabits.yar.Registry;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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


    public static org.javabits.yar.BlockingRegistry newBlockingRegistry(long defaultTimeoutInMillis) {
        return newLoadingCacheBasedBlockingRegistry(defaultTimeoutInMillis);
    }

    public static org.javabits.yar.BlockingRegistry newLoadingCacheBasedBlockingRegistry(long defaultTimeoutInMillis) {
        return BlockingRegistry.newLoadingCacheBlockingRegistry(defaultTimeoutInMillis);
    }

    public static org.javabits.yar.BlockingRegistry newMultimapBasedBlockingRegistry(long defaultTimeoutInMillis) {
        return BlockingRegistry.newMultimapBlockingRegistry(defaultTimeoutInMillis);
    }


    public static org.javabits.yar.BlockingSupplierRegistry newBlockingSupplierRegistry(long defaultTimeoutInMillis) {
        return newLoadingCacheBasedBlockingSupplierRegistry(defaultTimeoutInMillis);
    }

    public static org.javabits.yar.BlockingSupplierRegistry newLoadingCacheBasedBlockingSupplierRegistry(long defaultTimeoutInMillis) {
        return BlockingSupplierRegistry.newLoadingCacheBlockingSupplierRegistry(defaultTimeoutInMillis);
    }

    public static org.javabits.yar.BlockingSupplierRegistry newLoadingCacheBasedBlockingSupplierRegistry(long defaultTimeoutInMillis, ExecutionStrategy executionStrategy) {
        return BlockingSupplierRegistry.newLoadingCacheBlockingSupplierRegistry(defaultTimeoutInMillis, executionStrategy);
    }

    public static org.javabits.yar.BlockingSupplierRegistry newLoadingCacheBlockingSupplierRegistry() {
        return BlockingSupplierRegistry.newLoadingCacheBlockingSupplierRegistry();
    }

    public static org.javabits.yar.BlockingSupplierRegistry newMultimapBasedBlockingSupplierRegistry(long defaultTimeoutInMillis) {
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

    public static Module newRegistryDeclarationModule(final org.javabits.yar.BlockingSupplierRegistry registry) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                Key<org.javabits.yar.BlockingSupplierRegistry> blockingSupplierRegistryKey = Key.get(org.javabits.yar.BlockingSupplierRegistry.class);
                bind(Registry.class).to(blockingSupplierRegistryKey);
                bind(blockingSupplierRegistryKey).toInstance(registry);
            }
        };
    }


    public static List<Id<?>> requiredSuppliers(Injector injector) {
        //RegistryProvider.class
        ImmutableList.Builder<Id<?>> requiredSuppliers = ImmutableList.builder();
        Map<Key<?>, Binding<?>> allBindings = injector.getAllBindings();
        for (Map.Entry<Key<?>, Binding<?>> bindingEntry : allBindings.entrySet()) {
            Provider<?> provider = bindingEntry.getValue().getProvider();
            if (provider instanceof RegistryBindingBuilder.RegistryProvider
                    || provider instanceof BlockingSupplierRegistryBindingBuilder.BlockingSupplierRegistryProvider) {
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
        Binding<? extends Handler> registryListenerHandlerBinding = injector.getExistingBinding(Key.get(type));
        if (registryListenerHandlerBinding.getProvider() == null) {
            return Collections.emptyList();
        } else {
            return registryListenerHandlerBinding.getProvider().get().ids();
        }
    }

}
