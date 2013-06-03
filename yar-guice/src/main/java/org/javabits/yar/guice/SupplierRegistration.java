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
import org.javabits.yar.Supplier;

/**
 * TODO comment
 * Date: 2/20/13
 * Time: 9:50 AM
 *
 * @author Romain Gilles
 */
class SupplierRegistration<T> extends StrongPair<Id<T>, Supplier<T>> implements org.javabits.yar.Registration<T> {
    SupplierRegistration(Id<T> leftValue, Supplier<T> rightValue) {
        super(leftValue, rightValue);
    }

    @Override
    public Id<T> id() {
        return left();
    }

    @Override
    public String toString() {
        return "SupplierRegistration{" +
                "super=" + super.toString() +
                '}';
    }
}
