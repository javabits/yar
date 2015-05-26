/*
 * Copyright (c) 4/30/15 8:53 PM Romain Gilles
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

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import org.javabits.yar.BlockingSupplier;
import org.javabits.yar.Supplier;

import java.lang.reflect.ParameterizedType;
import java.util.concurrent.TimeUnit;

/**
 * This binding builder will handle the classic binding to and from the registry.
 * This binding builder will produce 3 types of binding when {@link #toRegistry()} is executed.
 * <p>
 * Let say we want to get an implementation of the {@code Hello} interface from the registry.
 * Therefore you will add the following binding to your module:
 * <pre>
 *     bind(Hello.class).toRegistry();
 * </pre>
 * This binding will automatically amply the following available injections:
 * <ul>
 *     <li>@Inject Hello hello;</li>
 *     <li>@Inject com.google.common.base.Supplier&gt;Hello&lt; hello;</li>
 *     <li>@Inject org.javabits.yar.Supplier&gt;Hello&lt; hello;</li>
 *     <li>@Inject org.javabits.yar.BlockingSupplier&gt;Hello&lt; hello;</li>
 * </ul>
 * </p>
 * @author Romain Gilles
 */
class DefaultRegistryAnnotatedBindingBuilderImpl<T> extends RegistryAnnotatedBindingBuilderImpl<T> {

    DefaultRegistryAnnotatedBindingBuilderImpl(Binder binder, Key<T> key) {
        super(binder, key);
    }

    DefaultRegistryAnnotatedBindingBuilderImpl(Binder binder, TypeLiteral<T> typeLiteral) {
        super(binder, typeLiteral);
    }

    DefaultRegistryAnnotatedBindingBuilderImpl(Binder binder, Class<T> clazz) {
        super(binder, clazz);
    }

    @Override
    public void toRegistry(long timeout, TimeUnit unit) {
        linkedBindingBuilder().toProvider(newRegistryProvider(timeout, unit));
        bindSuppliers();
    }

    @Override
    Iterable<RegistryProvider<?>> doToRegistry() {
        RegistryProvider<T> registryProvider = newRegistryProvider();
        linkedBindingBuilder().toProvider(registryProvider);
        RegistryProvider<BlockingSupplier<T>> blockingSupplierRegistryProvider = bindSuppliers();
        return ImmutableList.of(registryProvider, blockingSupplierRegistryProvider);
    }

    private RegistryProvider<BlockingSupplier<T>> bindSuppliers() {
        Key<BlockingSupplier<T>> blockingSupplierKey = newBlockingSupplierKey();
        BlockingSupplierRegistryProvider<T> blockingSupplierRegistryProvider = BlockingSupplierRegistryProvider.newProviderOf(key());
        binder().bind(blockingSupplierKey).toProvider(blockingSupplierRegistryProvider);
        Key<Supplier<T>> yarSupplierKey = newYarSupplierKey();
        binder().bind(yarSupplierKey).to(blockingSupplierKey);
        binder().bind(newGuavaSupplierKey()).to(yarSupplierKey);
        binder().bind(newJavaSupplierKey()).to(yarSupplierKey);
        return blockingSupplierRegistryProvider;
    }

    private Key<com.google.common.base.Supplier<T>> newGuavaSupplierKey() {
        ParameterizedType guavaSupplierType = Types.newParameterizedType(com.google.common.base.Supplier.class, key().getTypeLiteral().getType());
        return Keys.of(guavaSupplierType, key());
    }

    private Key<java.util.function.Supplier<T>> newJavaSupplierKey() {
        ParameterizedType guavaSupplierType = Types.newParameterizedType(java.util.function.Supplier.class, key().getTypeLiteral().getType());
        return Keys.of(guavaSupplierType, key());
    }

    private Key<Supplier<T>> newYarSupplierKey() {
        ParameterizedType yarSupplierType = Types.newParameterizedType(Supplier.class, key().getTypeLiteral().getType());
        return Keys.of(yarSupplierType, key());
    }

    private Key<BlockingSupplier<T>> newBlockingSupplierKey() {
        ParameterizedType blockingSupplierType = Types.newParameterizedType(BlockingSupplier.class, key().getTypeLiteral().getType());
        return Keys.of(blockingSupplierType, key());
    }

    private RegistryProvider<? extends T> newRegistryProvider(long timeout, TimeUnit unit) {
        return new RegistryProviderImpl<>(key(), timeout, unit);
    }

    private RegistryProvider<T> newRegistryProvider() {
        return new RegistryProviderImpl<>(key());
    }
}