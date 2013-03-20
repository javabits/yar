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

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import org.yar.*;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

import static org.yar.guice.Matchers.getYarKey;
import static org.yar.guice.Reflections.getRowType;
import static org.yar.guice.Reflections.getUniqueParameterType;

/**
 * TODO comment
 * Date: 3/17/13
 *
 * @author Romain Gilles
 */
abstract class GuiceWatcherRegistration<T> {

    private final Matcher<Key<T>> matcher;

    GuiceWatcherRegistration(Matcher<Key<T>> matcher) {
        this.matcher = matcher;
    }

    public static <T> GuiceWatcherRegistration<T> get(Matcher<Key<T>> matcher, Key<RegistryListener<? super T>> key) {
        return new KeyGuiceWatcherRegistration<>(matcher, key);
    }

    public static <T> GuiceWatcherRegistration<T> get(Matcher<Key<T>> matcher, RegistryListener<? super T> listener) {
        return new InstanceGuiceWatcherRegistration<>(matcher, listener);
    }

    public abstract Watcher<Supplier<T>> watcher();

    public KeyMatcher<T> matcher() {
        return newKeyMatcher(matcher);
    }

    private static <T> KeyMatcher<T> newKeyMatcher(final Matcher<Key<T>> keyMatcher) {
        return new KeyMatcherWrapper<>(keyMatcher);
    }

    static class KeyGuiceWatcherRegistration<T> extends GuiceWatcherRegistration<T> {

        private final Key<RegistryListener<? super T>> key;
        private Injector injector;

        KeyGuiceWatcherRegistration(Matcher<Key<T>> matcher, Key<RegistryListener<? super T>> key) {
            super(matcher);
            this.key = key;
        }

        @Override
        public Watcher<Supplier<T>> watcher() {
            return newWatcherAdapter(injector.getInstance(key));
        }

        @Inject
        public void setInjector(Injector injector) {
            this.injector = injector;
        }
    }

    static class InstanceGuiceWatcherRegistration<T> extends GuiceWatcherRegistration<T> {

        private final RegistryListener<? super T> listener;

        InstanceGuiceWatcherRegistration(Matcher<Key<T>> matcher, RegistryListener<? super T> listener) {
            super(matcher);
            this.listener = listener;
        }

        @Override
        public Watcher<Supplier<T>> watcher() {
            return newWatcherAdapter(listener);
        }
    }

    private static <T> Watcher<Supplier<T>> newWatcherAdapter(final RegistryListener listener) {
        if (Supplier.class.isAssignableFrom(getRowType(getUniqueParameterType(listener.getClass()
                , RegistryListener.class, "RegistryListener<T>")))) {
            return new SupplierWatcher<>(listener);
        } else if (listener instanceof AbstractSingleElementWatcher) {
            return new SupplierSingleTargetWatcher<>(listener);
        } else {
            return new SupplierMultiTargetWatcher<>(listener);
        }
    }

    private static class SupplierWatcher<T> implements Watcher<Supplier<T>> {
        private final Watcher<? super Supplier<T>> registryListener;

        @SuppressWarnings("unchecked")
        public SupplierWatcher(Watcher listener) {
            registryListener = listener;
        }

        @Nullable
        @Override
        public Supplier<T> add(Supplier<T> element) {
            if (registryListener.add(element) != null) {
                return element;
            } else {
                return null;
            }
        }

        @Override
        public void remove(Supplier<T> element) {
            registryListener.remove(element);
        }
    }

    private static class SupplierMultiTargetWatcher<T> extends AbstractSupplierTargetWatcher<T> {
        private final Map<Supplier<T>, T> trackedElements = new IdentityHashMap<>();

        public SupplierMultiTargetWatcher(Watcher listener) {
            super(listener);
        }

        @Override
        protected Supplier track(Supplier<T> element, T trackedElement) {
            trackedElements.put(element, trackedElement);
            return element;
        }

        @Override
        protected T removeTracked(Supplier<T> element) {
            return trackedElements.remove(element);
        }
    }

    private static class SupplierSingleTargetWatcher<T> extends AbstractSupplierTargetWatcher<T> {
        private T trackedElement;

        public SupplierSingleTargetWatcher(Watcher listener) {
            super(listener);
        }

        @Override
        protected Supplier track(Supplier<T> element, T trackedElement) {
            this.trackedElement = trackedElement;
            return element;
        }

        @Override
        protected T removeTracked(Supplier<T> element) {
            return trackedElement;
        }
    }

    private static class KeyMatcherWrapper<T> implements KeyMatcher<T> {
        private final Matcher<Key<T>> matcher;
        private final org.yar.Key<T> key;

        public KeyMatcherWrapper(Matcher<Key<T>> keyMatcher) {
            matcher = keyMatcher;
            key = getYarKey(matcher);
        }

        @Override
        public boolean matches(org.yar.Key<T> otherKey) {
            return matcher.matches(getKey(otherKey));
        }

        @SuppressWarnings("unchecked")
        private Key<T> getKey(org.yar.Key<T> otherKey) {
            Key<T> otherGuiceKey;
            if (otherKey.annotationType() != null)
                otherGuiceKey = (Key<T>) Key.get(otherKey.type(), otherKey.annotation());
            else if (otherKey.annotationType() != null) {
                otherGuiceKey = (Key<T>) Key.get(otherKey.type(), otherKey.annotationType());
            } else {
                otherGuiceKey = (Key<T>) Key.get(otherKey.type());
            }
            return otherGuiceKey;
        }

        @Override
        public org.yar.Key<T> key() {
            return key;
        }
    }
}
