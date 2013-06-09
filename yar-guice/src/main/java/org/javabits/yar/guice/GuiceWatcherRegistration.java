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

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.matcher.Matcher;
import org.javabits.yar.Id;
import org.javabits.yar.IdMatcher;
import org.javabits.yar.Watcher;

import static org.javabits.yar.guice.Matchers.getId;

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

    public abstract Watcher<T> watcher();

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
        public Watcher<T> watcher() {
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
        public Watcher<T> watcher() {
            return newWatcherAdapter(listener);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Watcher<T> newWatcherAdapter(final RegistryListener listener) {
        return listener;
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

        @Override
        public String toString() {
            return "IdMatcherWrapper{" +
                    "id=" + id +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "GuiceWatcherRegistration{" +
                "matcher=" + matcher +
                '}';
    }
}
