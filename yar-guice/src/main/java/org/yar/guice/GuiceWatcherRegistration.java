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
import com.google.inject.matcher.Matcher;
import org.yar.*;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

import static org.yar.guice.Matchers.getId;
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

    public static <T> GuiceWatcherRegistration<T> get(Matcher<Key<T>> matcher, Key<? extends RegistryListener<? super T>> key) {
        return new KeyGuiceWatcherRegistration<>(matcher, key);
    }

    public static <T> GuiceWatcherRegistration<T> get(Matcher<Key<T>> matcher, RegistryListener<? super T> listener) {
        return new InstanceGuiceWatcherRegistration<>(matcher, listener);
    }

    public abstract Watcher<Supplier<T>> watcher();

    public IdMatcher<T> matcher() {
        return newKeyMatcher(matcher);
    }

    private static <T> IdMatcher<T> newKeyMatcher(final Matcher<Key<T>> keyMatcher) {
        return new IdMatcherWrapper<>(keyMatcher);
    }

    static class KeyGuiceWatcherRegistration<T> extends GuiceWatcherRegistration<T> {

        private final Key<? extends RegistryListener<? super T>> key;
        private Injector injector;

        KeyGuiceWatcherRegistration(Matcher<Key<T>> matcher, Key<? extends RegistryListener<? super T>> key) {
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

    @SuppressWarnings("unchecked")
    private static <T> Watcher<Supplier<T>> newWatcherAdapter(final RegistryListener listener) {
        if (Supplier.class.isAssignableFrom(Reflections.getRawType(getUniqueParameterType(listener.getClass()
                , RegistryListener.class, "RegistryListener<T>")))) {
            return new SupplierWatcher(listener);
        } else if (listener instanceof AbstractSingleElementWatcher) {
            return new SupplierSingleTargetWatcher(listener);
        } else {
            return new SupplierMultiTargetWatcher(listener);
        }
    }

    private static class SupplierWatcher<T> implements Watcher<T> {
        private final Watcher<T> registryListener;

        public SupplierWatcher(Watcher<T> listener) {
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
        private final Map<Supplier<T>, Boolean> trackedElements = new IdentityHashMap<>();

        public SupplierMultiTargetWatcher(Watcher<T> listener) {
            super(listener);
        }

        @Override
        protected Supplier<T> track(Supplier<T> element) {
            trackedElements.put(element, Boolean.TRUE);
            return element;
        }

        @Override
        protected Supplier<T> removeTracked(Supplier<T> element) {
            if (trackedElements.remove(element)) {
                return element;
            }
            throw new IllegalArgumentException("Try to remove ");
        }
    }

    private static class SupplierSingleTargetWatcher<T> extends AbstractSupplierTargetWatcher<T> {
        private Supplier<T> trackedElement;

        public SupplierSingleTargetWatcher(Watcher<T> listener) {
            super(listener);
        }

        @Override
        protected Supplier<T> track(Supplier<T> element) {
            this.trackedElement = element;
            return element;
        }

        @Override
        protected Supplier<T> removeTracked(Supplier<T> element) {
            return trackedElement;
        }
    }

    private static class IdMatcherWrapper<T> implements IdMatcher<T> {
        private final Matcher<Key<T>> matcher;
        private final Id<T> id;

        public IdMatcherWrapper(Matcher<Key<T>> keyMatcher) {
            matcher = keyMatcher;
            id = getId(matcher);
        }

        @Override
        public boolean matches(Id<T> otherId) {
            return matcher.matches(getKey(otherId));
        }

        @SuppressWarnings("unchecked")
        private Key<T> getKey(Id<T> otherId) {
            Key<T> otherGuiceKey;
            if (otherId.annotation() != null)
                otherGuiceKey = (Key<T>) Key.get(otherId.type(), otherId.annotation());
            else if (otherId.annotationType() != null) {
                otherGuiceKey = (Key<T>) Key.get(otherId.type(), otherId.annotationType());
            } else {
                otherGuiceKey = (Key<T>) Key.get(otherId.type());
            }
            return otherGuiceKey;
        }

        @Override
        public Id<T> id() {
            return id;
        }
    }
}
