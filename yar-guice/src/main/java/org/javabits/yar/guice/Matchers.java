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
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import org.javabits.yar.Id;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.javabits.yar.guice.Reflections.getUniqueParameterType;
import static org.javabits.yar.guice.Reflections.isParameterizedType;

/**
 * TODO comment
 * Date: 3/17/13
 * Time: 10:01 PM
 *
 * @author Romain Gilles
 */
public final class Matchers {

    private static final String KEY_MATCHER_SIGNATURE = "Matcher<Key<T>>";
    private static final String MATCHER_PARAMETER_TYPE_ERROR = "The matcher type must be implementation of " + KEY_MATCHER_SIGNATURE;

    public static <T> Id<T> getId(Matcher<Key<T>> matcher) {
        return GuiceId.of(getKey(matcher));
    }

    public static <T> Key<T> getKey(Matcher<Key<T>> matcher) {
        return Key.get(getTargetTypeLiteral(matcher));
    }

    public static <T> TypeLiteral<T> getTargetTypeLiteral(Matcher<Key<T>> matcher) {
        if (matcher instanceof KeyProvider) {
            @SuppressWarnings("unchecked")
            KeyProvider<T> keyProvider = (KeyProvider<T>) matcher;
            return keyProvider.get().getTypeLiteral();
        }
        Type keyTargetType = getTargetType(matcher);
        return getTypeLiteral(keyTargetType);
    }

    @SuppressWarnings("unchecked")
    private static <T> TypeLiteral<T> getTypeLiteral(Type keyTargetType) {
        return (TypeLiteral<T>) TypeLiteral.get(keyTargetType);
    }

    private static <T> Type getTargetType(Matcher<Key<T>> matcher) {
        Type matcherTargetType = getUniqueParameterType(matcher.getClass(), Matcher.class, KEY_MATCHER_SIGNATURE);
        if (isParameterizedType(matcherTargetType) && Key.class.isAssignableFrom(Reflections.getRawType(matcherTargetType))) {
            return getUniqueParameterType((ParameterizedType) matcherTargetType, "Key<T>");
        } else {
            throw new IllegalArgumentException(MATCHER_PARAMETER_TYPE_ERROR);
        }
    }

    public static <T> Matcher<Key<T>> newKeyMatcher(final Key<T> key) {
        return new KeyMatcher<>(key);
    }

    public static <T> Matcher<Key<T>> newKeyMatcher(final Class<T> type) {
        return new KeyMatcher<>(Key.get(type));
    }

    public static <T> Matcher<Key<T>> newKeyTypeMatcher(final Key<T> key) {
        return new KeyTypeMatcher<>(key);
    }

    public static <T> Matcher<Key<T>> newKeyTypeMatcher(final Class<T> type) {
        return new KeyTypeMatcher<>(Key.get(type));
    }

    public static Matcher<? super TypeLiteral<?>> subclassesOf(final Class<?> superclass) {
        return new SubClassesOf(superclass);
    }

    static class KeyMatcher<T> extends AbstractMatcher<Key<T>> implements KeyProvider<T> {

        private final Key<T> key;

        public KeyMatcher(Key<T> key) {
            this.key = key;
        }

        @Override
        public boolean matches(Key<T> other) {
            return key.equals(other);
        }

        @Override
        public Key<T> get() {
            return key;
        }
    }

    static class KeyTypeMatcher<T> extends AbstractMatcher<Key<T>> implements KeyProvider<T> {

        private final Key<T> key;

        public KeyTypeMatcher(Key<T> key) {
            this.key = key;
        }

        @Override
        public boolean matches(Key<T> other) {
            return key.getTypeLiteral().equals(other.getTypeLiteral());
//            return true; //always true ;)
        }

        @Override
        public Key<T> get() {
            return key;
        }
    }

    private static class SubClassesOf extends AbstractMatcher<TypeLiteral<?>> {

        private final Class<?> superclass;

        public SubClassesOf(Class<?> superclass) {
            this.superclass = checkNotNull(superclass);
        }

        @Override
        public boolean matches(TypeLiteral<?> o) {
            return superclass.isAssignableFrom(o.getRawType());
        }
    }
}
