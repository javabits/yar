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

package org.yar;


import java.util.List;

/**
 * TODO add type token or type literal
 * TODO see if we use directly the guice key...??? and they type literal
 * Date: 2/8/13
 * Time: 5:13 PM
 *
 * @author Romain Gilles
 * @since 1.0
 */
public interface Registry {

    <T> List<Supplier<T>> getAll(Class<T> type);

    <T> Supplier<T> get(Class<T> type);

    <T> List<Supplier<T>> getAll(Key<T> key);

    <T> Supplier<T> get(Key<T> key);

    <T> Registration<T> put(Key<T> key, Supplier<? extends T> supplier);

    void remove(Registration<?> registration);

    <T> Registration<T> addWatcher(Key<T> watchedKey, Watcher<Supplier<? extends T>> watcher);

    void removeWatcher(Registration<?> watcherRegistration);
}
