/*
 * Copyright 2013 Romain Gilles
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javabits.yar.guice;

import static org.javabits.yar.SupplierEvent.Type.ADD;
import static org.javabits.yar.SupplierEvent.Type.REMOVE;

import java.lang.ref.WeakReference;
import java.util.IdentityHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import org.javabits.yar.*;

import com.google.common.base.FinalizableReferenceQueue;
import com.google.common.base.FinalizableWeakReference;

/**
 * TODO comment Date: 2/20/13 Time: 7:04 PM
 *
 * @author Romain Gilles
 */
class WatcherRegistration<T> extends FinalizableWeakReference<Object> implements
        Pair<IdMatcher<T>, Watcher<T>>, org.javabits.yar.Registration<T> {

    private static final Logger LOG = Logger.getLogger(WatcherRegistration.class.getName());
    private final IdMatcher<T> left;
    private final Watcher<T> right;
    private final Registry registry;


    static <T> WatcherRegistration<T> newWatcherRegistration(IdMatcher<T> leftValue, SupplierListener supplierListener, FinalizableReferenceQueue referenceQueue, Registry registry) {
        return new WatcherRegistration<>(leftValue, supplierListener, new SupplierWatcherToSupplierListenerAdapter<T>(supplierListener), referenceQueue, registry);
    }

    @SuppressWarnings("unchecked")
    static <T> WatcherRegistration<T> newWatcherRegistration(IdMatcher<T> leftValue, Watcher<T> rightValue, FinalizableReferenceQueue referenceQueue, Registry registry) {
        return new WatcherRegistration(leftValue, rightValue, new WatcherDecorator<>(rightValue), referenceQueue, registry);
    }

    WatcherRegistration(IdMatcher<T> leftValue, Object weakReference, Watcher<T> rightValue, FinalizableReferenceQueue referenceQueue, Registry registry) {
        super(weakReference, referenceQueue);
        left = leftValue;
        right = rightValue;
        this.registry = registry;
    }

    @Override
    public Id<T> id() {
        return left().id();
    }

    @Override
    public IdMatcher<T> left() {
        return left;
    }

    @Override
    public Watcher<T> right() {
        return right;
    }

    @Override
    public void finalizeReferent() {
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("Remove watcher registration on GC: " + left);
        }
        registry.removeWatcher(this);
    }

    @Override
    public String toString() {
        return "WatcherRegistration{" +
                "left=" + left +
                ", right=" + right +
                '}';
    }

    static class WatcherDecorator<T> implements Watcher<T> {
        private final WeakReference<Watcher<T>> delegate;
        private final IdentityHashMap<Supplier<T>, Supplier<T>> trackedElements = new IdentityHashMap<>();

        WatcherDecorator(Watcher<T> delegate) {
            this.delegate = new WeakReference<>(delegate);
        }

        @Nullable
        @Override
        public Supplier<T> add(Supplier<T> element) {
            Watcher<T> watcher = delegate.get();
            if (watcher != null) {
                Supplier<T> trackedElement = watcher.add(element);
                if (trackedElement != null) {
                    trackedElements.put(element, trackedElement);
                }
                return trackedElement;
            } else {
                clearTrackedElements();
                return null;
            }
        }

        private void clearTrackedElements() {
            trackedElements.clear();
        }

        @Override
        public void remove(Supplier<T> element) {
            Supplier<T> trackedElement = trackedElements.remove(element);
            if (trackedElement != null) {
                Watcher<T> watcher = delegate.get();
                if (watcher != null) {
                    watcher.remove(trackedElement);
                } else {
                    clearTrackedElements();
                }
            }
        }

        @Override
        public String toString() {
            return "WatcherDecorator{" +
                    "delegate=" + delegate.get() +
                    '}';
        }
    }

    private static class SupplierWatcherToSupplierListenerAdapter<T> implements Watcher<T> {
        private final WeakReference<SupplierListener> delegate;

        public SupplierWatcherToSupplierListenerAdapter(SupplierListener delegate) {
            this.delegate = new WeakReference<>(delegate);
        }

        @Nullable
        @Override
        public Supplier<T> add(Supplier<T> supplier) {
            SupplierListener supplierListener = delegate.get();
            if (supplierListener != null) {
                supplierListener.supplierChanged(new SupplierEvent(ADD, supplier));
            }
            return supplier;
        }

        @Override
        public void remove(Supplier<T> supplier) {
            delegate.get().supplierChanged(new SupplierEvent(REMOVE, supplier));
        }

        @Override
        public String toString() {
            return "SupplierWatcherToSupplierListenerAdapter{" +
                    "delegate=" + delegate.get() +
                    '}';
        }
    }
}
