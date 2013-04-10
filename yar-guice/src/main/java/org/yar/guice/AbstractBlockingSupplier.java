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

import org.yar.Supplier;

import javax.annotation.Nullable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.Objects.requireNonNull;

/**
 * TODO comment
 * Date: 2/28/13
 * Time: 11:11 AM
 *
 * @author Romain Gilles
 */
abstract class AbstractBlockingSupplier<T> implements Supplier<T>, SupplierListener {

    final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    final Lock readLock = readWriteLock.readLock();
    final Lock writeLock = readWriteLock.writeLock();
    /**
     * Condition for waiting takes
     */
    final Condition notEmpty = writeLock.newCondition();

    private final FirstSupplierProvider<T> firstSupplierProvider;

    Supplier<T> delegate;

    AbstractBlockingSupplier(FirstSupplierProvider<T> firstSupplierProvider, Supplier<T> delegate) {
        this.firstSupplierProvider = firstSupplierProvider;
        this.delegate = delegate;
    }


    @Nullable
    private Supplier<T> add(Supplier<T> element) {
        requireNonNull(element, "element");
        element = firstSupplierProvider.get();
        readLock.lock();
        try {
            if (delegate == element) {
                return null;
            }
        } finally {
            readLock.unlock();
        }
        writeLock.lock();
        try {
            if (delegate == null) {
                delegate = element;
                notEmpty.signal();
                return element;
            }
            return null;

        } finally {
            writeLock.unlock();
        }
    }

    private void remove(Supplier<T> element) {
        requireNonNull(element, "element");
        readLock.lock();
        try {
            if (delegate != element) { //identity test handle delegate == null also
                return;
            }
        } finally {
            readLock.unlock();
        }
        writeLock.lock();
        try {
            if (delegate == element) { //identity test handle delegate == null also
                delegate = firstSupplierProvider.get();
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void supplierChanged(SupplierEvent supplierEvent) {
        SupplierEvent.Type type = supplierEvent.type();
        switch (type) {
            case ADD:
                add((Supplier<T>) supplierEvent.supplier());
                break;
            case REMOVE:
                remove((Supplier<T>) supplierEvent.supplier());
                break;
            default:
                throw new IllegalStateException("Unknown supplier state: " + supplierEvent);
        }


    }
}
