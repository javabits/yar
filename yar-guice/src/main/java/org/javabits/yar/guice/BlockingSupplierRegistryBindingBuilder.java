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

import com.google.inject.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.javabits.yar.guice.SupplierRegistryBindingBuilder.checkParameterizedType;

/**
 * TODO comment
 * Date: 3/4/13
 * Time: 10:11 PM
 *
 * @author Romain Gilles
 */
public class BlockingSupplierRegistryBindingBuilder<T> extends RegistryBindingBuilder<T> {

    public BlockingSupplierRegistryBindingBuilder(Binder binder, Key<T> key) {
        super(binder, key);
    }

    public BlockingSupplierRegistryBindingBuilder(Binder binder, TypeLiteral<T> typeLiteral) {
        super(binder, typeLiteral);
    }

    @Override
    Provider<T> newRegistryProvider() {
        return new BlockingSupplierRegistryProvider<>(key());
    }
    //TODO refactor it to make merge with RegistryProvider class hierarchy!
    static class BlockingSupplierRegistryProvider<T> implements Provider<T> {

        private final Key<T> key;
        private org.javabits.yar.BlockingSupplierRegistry registry;

        private BlockingSupplierRegistryProvider(Key<T> key) {
            this.key = key;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T get() {
            return (T) registry.get(getSupplierTypeParameter());
        }

        @SuppressWarnings("unchecked")
        private GuiceId<T> getSupplierTypeParameter() {
            Type type = key.getTypeLiteral().getType();
            checkParameterizedType(type);
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length != 1) {
                throw new IllegalArgumentException("Supplier type must be mono parameterized: " + type);
            }

            Type actualTypeArgument = actualTypeArguments[0];
            return (GuiceId<T>) GuiceId.of(actualTypeArgument, key);
        }

        @Inject
        public void setRegistry(org.javabits.yar.BlockingSupplierRegistry registry) {
            this.registry = registry;
        }
    }
}
