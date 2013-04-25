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

import org.javabits.yar.Supplier;

import java.util.EventObject;

/**
 * TODO comment
 * Date: 4/2/13
 * Time: 10:20 PM
 *
 * @author Romain Gilles
 */
public class SupplierEvent extends EventObject {

    enum Type {
        ADD, REMOVE;
    }

    private final Type type;
    private final Supplier<?> supplier;

    public SupplierEvent(Type type, Supplier<?> supplier) {
        super(supplier);
        this.type = type;
        this.supplier = supplier;
    }

    public Type type() {
        return type;
    }

    public Supplier<?> supplier() {
        return supplier;
    }
}
