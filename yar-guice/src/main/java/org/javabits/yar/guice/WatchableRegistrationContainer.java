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

import org.javabits.yar.Id;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

/**
 * TODO comment
 * Date: 2/20/13
 * Time: 9:35 AM
 *
 * @author Romain Gilles
 */
public interface WatchableRegistrationContainer {

    Set<Type> types();

    List<SupplierRegistration<?>> getAll(Type type);

    @Nullable
    SupplierRegistration<?> getFirst(Type type);

    <T> List<SupplierRegistration<T>> getAll(Id<T> id);

    @Nullable
    <T> SupplierRegistration<T> getFirst(Id<T> id);

    boolean put(SupplierRegistration<?> registration) throws InterruptedException;

    boolean remove(SupplierRegistration<?> registration) throws InterruptedException;

    <T> boolean add(WatcherRegistration<T> watcherRegistration) throws InterruptedException;

    boolean remove(WatcherRegistration<?> watcherRegistration);
}
