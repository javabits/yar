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

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.yar.Matcher;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.yar.guice.Reflections.getRowType;
import static org.yar.guice.Reflections.getUniqueParameterType;
import static org.yar.guice.Reflections.isParameterizedType;

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

    public static <T> org.yar.Key<T> getYarKey(Matcher<Key<T>> matcher) {
        return GuiceKey.of(getKey(matcher));
    }

    public static <T> Key<T> getKey(Matcher<Key<T>> matcher) {
        return Key.get(getTargetTypeLiteral(matcher));
    }

    public static <T> TypeLiteral<T> getTargetTypeLiteral(Matcher<Key<T>> matcher) {
        Type keyTargetType = getTargetType(matcher);
        return getTypeLiteral(keyTargetType);
    }

    @SuppressWarnings("unchecked")
    private static <T> TypeLiteral<T> getTypeLiteral(Type keyTargetType) {
        return (TypeLiteral<T>)TypeLiteral.get(keyTargetType);
    }

    private static <T> Type getTargetType(Matcher<Key<T>> matcher) {
        Type matcherTargetType = getUniqueParameterType(matcher.getClass(), Matcher.class, KEY_MATCHER_SIGNATURE);
        if (isParameterizedType(matcherTargetType) && Key.class.isAssignableFrom(getRowType(matcherTargetType))) {
            return getUniqueParameterType((ParameterizedType) matcherTargetType, "Key<T>");
        } else {
            throw new IllegalArgumentException(MATCHER_PARAMETER_TYPE_ERROR);
        }

    }

}
