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

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * TODO comment
 * Date: 3/4/13
 * Time: 10:11 PM
 *
 * @author Romain Gilles
 */
public class BlockingSupplierRegistryAnnotatedBindingBuilderImpl<T> extends RegistryAnnotatedBindingBuilderImpl<T> {

    public BlockingSupplierRegistryAnnotatedBindingBuilderImpl(Binder binder, Key<T> key) {
        super(binder, key);
    }

    public BlockingSupplierRegistryAnnotatedBindingBuilderImpl(Binder binder, TypeLiteral<T> typeLiteral) {
        super(binder, typeLiteral);
    }

    @Override
    public void toRegistry(long timeout, TimeUnit unit) {
        linkedBindingBuilder().toProvider(newRegistryProvider(timeout, unit));
    }

    @Override
    Iterable<RegistryProvider<?>> doToRegistry() {
        RegistryProvider<T> registryProvider = newRegistryProvider();
        linkedBindingBuilder().toProvider(registryProvider);
        return Collections.<RegistryProvider<?>>singleton(registryProvider);
    }

    private RegistryProvider<T> newRegistryProvider() {
        return BlockingSupplierRegistryProvider.newProvider(key());
    }

    private RegistryProvider<? extends T> newRegistryProvider(long timeout, TimeUnit unit) {
        return newRegistryProvider();
    }

}
