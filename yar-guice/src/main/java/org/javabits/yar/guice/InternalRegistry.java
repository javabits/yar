/*
 * Copyright (c) 9/28/14 5:08 PM Romain Gilles
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

import org.javabits.yar.*;

import java.util.concurrent.TimeUnit;

/**
 * Internal registry.
 */
interface InternalRegistry {
    long defaultTimeout();

    TimeUnit defaultTimeUnit();

    <T> Supplier<T> getDirectly(Id<T> id);

    public <T> Registration<T> addSupplierListener(IdMatcher<T> idMatcher, SupplierListener supplierListener);
}
