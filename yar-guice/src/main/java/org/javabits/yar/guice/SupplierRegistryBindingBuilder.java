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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * TODO comment
 * Date: 3/4/13
 * Time: 10:11 PM
 *
 * @author Romain Gilles
 */
public class SupplierRegistryBindingBuilder<T> extends RegistryBindingBuilder<T> {

    public SupplierRegistryBindingBuilder(Binder binder, Key<T> key) {
        super(binder, key);
    }

    public SupplierRegistryBindingBuilder(Binder binder, TypeLiteral<T> typeLiteral) {
        super(binder, typeLiteral);
    }

    @Override
    RegistryBindingBuilder.RegistryProvider<T> newRegistryProvider() {
        return new SupplierRegistryProvider<>(key());
    }

    private static class SupplierRegistryProvider<T> extends RegistryBindingBuilder.RegistryProvider<T> {

        private SupplierRegistryProvider(Key<T> key) {
            super(key);
        }

        @Override
        @SuppressWarnings("unchecked")
        public T get() {
            return (T) registry().get(getSupplierTypeParameter());
        }

        @SuppressWarnings("unchecked")
        private GuiceId<T> getSupplierTypeParameter() {
            Type type = key().getTypeLiteral().getType();
            checkParameterizedType(type);
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length != 1) {
                throw new IllegalArgumentException("Supplier type must be mono parameterized: " + type);
            }

            return (GuiceId<T>) GuiceId.of(Key.get(actualTypeArguments[0]));
        }

    }

    static void checkParameterizedType(Type type) {
        if (!isParameterizedType(type)) {
            throw new IllegalArgumentException("Supplier type must be parameterized: " + type);
        }
    }

    static boolean isParameterizedType(Type type) {
        return type instanceof ParameterizedType;
    }
}
