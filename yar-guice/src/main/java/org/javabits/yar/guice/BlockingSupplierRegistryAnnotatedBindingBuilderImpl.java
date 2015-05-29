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
import com.google.inject.util.Types;
import org.javabits.yar.BlockingSupplier;
import org.javabits.yar.Supplier;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;

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
    Iterable<RegistryProvider<?>> doToRegistry() {
        RegistryProvider<T> registryProvider = newRegistryProvider();
        linkedBindingBuilder().toProvider(registryProvider);
        bindOtherSuppliers(registryProvider);
        return Collections.<RegistryProvider<?>>singleton(registryProvider);
    }

    void bindOtherSuppliers(RegistryProvider registryProvider) {
        bind(BlockingSupplier.class, registryProvider);
        bind(Supplier.class, registryProvider);
        bind(java.util.function.Supplier.class, registryProvider);
        bind(com.google.common.base.Supplier.class, registryProvider);
    }

    @SuppressWarnings("unchecked")
    private void bind(Class<?> rawType, RegistryProvider registryProvider) {
        if(!rawType.isAssignableFrom(key().getTypeLiteral().getRawType())) {
            binder().bind(getKey(rawType)).toProvider(registryProvider);
        }
    }

    private Key getKey(Type rawType) {
        ParameterizedType parameterizedType = Types.newParameterizedType(rawType, getParametrizedType(key().getTypeLiteral().getType()));
        return Keys.of(parameterizedType, key());
    }


    @SuppressWarnings("unchecked")
    private RegistryProvider<T> newRegistryProvider() {
        return BlockingSupplierRegistryProvider.newProvider((Key)key());
    }

    private static Type getParametrizedType(Type type) {
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments.length != 1) {
            throw new IllegalArgumentException("Supplier type must be mono parameterized: " + type);
        }

        return actualTypeArguments[0];
    }

}
