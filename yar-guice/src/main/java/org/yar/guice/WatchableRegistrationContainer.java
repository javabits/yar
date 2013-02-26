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

import org.yar.Key;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;

/**
 * TODO comment
 * Date: 2/20/13
 * Time: 9:35 AM
 *
 * @author Romain Gilles
 */
public interface WatchableRegistrationContainer {

    List<SupplierRegistration<?>> getAll(Type type);
    @Nullable
    SupplierRegistration<?> getFirst(Type type);
    <T> List<SupplierRegistration<T>> getAll(Key<T> key);
    @Nullable
    <T> SupplierRegistration<T> getFirst(Key<T> key);

    boolean put(SupplierRegistration<?> registration);

    boolean remove(SupplierRegistration<?> registration);

    <T> boolean add(WatcherRegistration<T> watcherRegistration);

    boolean remove(WatcherRegistration<?> watcherRegistration);
}
