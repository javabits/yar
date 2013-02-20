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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import org.yar.Key;

import java.lang.reflect.Type;
import java.util.List;

import static com.google.common.collect.Multimaps.synchronizedListMultimap;

/**
 * TODO comment
 * Date: 2/20/13
 * Time: 9:47 AM
 *
 * @author Romain Gilles
 */
public class MultimapListWatchableRegistrationContainer implements WatchableRegistrationContainer {
    private final ListMultimap<Type, SupplierRegistration<?>> registry;

    public MultimapListWatchableRegistrationContainer() {
        this(synchronizedListMultimap(ArrayListMultimap.<Type, SupplierRegistration<?>>create()));
    }

    public MultimapListWatchableRegistrationContainer(ListMultimap<Type, SupplierRegistration<?>> registry) {
        this.registry = registry;
    }

    @Override
    public List<SupplierRegistration<?>> getAll(Type type) {
        List<SupplierRegistration<?>> pairs = registry.get(type);
        synchronized (registry) {
            return ImmutableList.copyOf(pairs);
        }
    }

    @Override
    public SupplierRegistration<?> getFirst(Type type) {
        List<SupplierRegistration<?>> pairs = registry.get(type);
        synchronized (registry) {
            if (!pairs.isEmpty()) {
                return pairs.get(0);
            }
        }
        return null;
    }

    @Override
    public <T> List<SupplierRegistration<T>> getAll(Key<T> key) {
        List<SupplierRegistration<?>> pairs = registry.get(key.type());
        ImmutableList.Builder<SupplierRegistration<T>> suppliersByKey = ImmutableList.builder();
        synchronized (registry) {
            for (SupplierRegistration<?> registryEntry : pairs) {
                addSupplierIfKeyEquals(key, suppliersByKey, registryEntry);
            }
        }
        return suppliersByKey.build();
    }

    private <T> void addSupplierIfKeyEquals(Key<T> key, ImmutableList.Builder<SupplierRegistration<T>> suppliersByKey, SupplierRegistration<?> registryEntry) {
        if (key.equals(registryEntry.leftValue))
            suppliersByKey.add((SupplierRegistration<T>) registryEntry);
    }

    @Override
    public <T> SupplierRegistration<T> getFirst(Key<T> key) {
        List<SupplierRegistration<?>> pairs = registry.get(key.type());
        synchronized (registry) {
            for (SupplierRegistration<?> pair : pairs) {
                if (key.equals(pair.leftValue)) {
                    return (SupplierRegistration<T>) pair;
                }
            }
        }
        return null;
    }

    @Override
    public boolean put(SupplierRegistration<?> registration) {
        return registry.put(registration.key().type(), registration);
    }

    @Override
    public boolean remove(SupplierRegistration<?> registration) {
        return registry.remove(registration.key(), registration);
    }
}
