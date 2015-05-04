/*
 * Copyright (c) 4/30/15 9:52 PM Romain Gilles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javabits.yar.guice;

import com.google.inject.Inject;
import com.google.inject.Key;
import org.javabits.yar.Id;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Implementation of the noWait provider.
 */
class BlockingSupplierRegistryProvider<T> implements RegistryProvider<T> {

    private final Id<T> id;
    private org.javabits.yar.BlockingSupplierRegistry registry;

    BlockingSupplierRegistryProvider(Id<T> id) {
        this.id = id;
    }

    static <T> BlockingSupplierRegistryProvider<T> newProvider(Key<T> key) {
        return new BlockingSupplierRegistryProvider<>(getSupplierTypeParameter(key));
    }

    static <T> BlockingSupplierRegistryProvider<T> newProviderOf(Key<T> key) {
        return new BlockingSupplierRegistryProvider<>(GuiceId.of(key));
    }


    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        return (T) registry.get(id);
    }

    @SuppressWarnings("unchecked")
    private static <T> Id<T> getSupplierTypeParameter(Key<T> key) {
        Type type = key.getTypeLiteral().getType();
        checkParameterizedType(type);
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments.length != 1) {
            throw new IllegalArgumentException("Supplier type must be mono parameterized: " + type);
        }

        Type actualTypeArgument = actualTypeArguments[0];
        return GuiceId.of(actualTypeArgument, key);
    }

    @Inject
    public void setRegistry(org.javabits.yar.BlockingSupplierRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void noWait() {
        //has no sens here in this context => nothing to do
    }

    private static void checkParameterizedType(Type type) {
        if (!isParameterizedType(type)) {
            throw new IllegalArgumentException("Supplier type must be parameterized: " + type);
        }
    }

    private static boolean isParameterizedType(Type type) {
        return type instanceof ParameterizedType;
    }
}
