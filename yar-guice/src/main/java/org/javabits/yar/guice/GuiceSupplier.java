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

import com.google.inject.Key;
import org.javabits.yar.Id;
import org.javabits.yar.Supplier;

import javax.inject.Provider;

/**
 * TODO comment
 * Date: 2/10/13
 * Time: 11:19 PM
 *
 * @author Romain Gilles
 */
class GuiceSupplier<T> implements Supplier<T> {

    private final Provider<T> provider;
    private final Id<T> id;

    GuiceSupplier(Id<T> id, Provider<T> provider) {
        this.provider = provider;
        this.id = id;
    }

    static <T> GuiceSupplier<T> of(Key<T> key, Provider<T> provider) {
        return new GuiceSupplier<>(GuiceId.of(key), provider);
    }

    static <T> GuiceSupplier<T> of(Id<T> id, Provider<T> provider) {
        return new GuiceSupplier<>(id, provider);
    }

    @Override
    public Id<T> id() {
        return id;
    }

    @Override
    public T get() {
        return provider.get();
    }

    @Override
    public String toString() {
        return "GuiceSupplier{" +
                "id=" + id +
                ",provider=" + provider +
                '}';
    }
}
