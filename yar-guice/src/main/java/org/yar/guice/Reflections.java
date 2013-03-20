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

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * TODO comment
 * Date: 3/18/13
 *
 * @author Romain Gilles
 */
public final class Reflections {
    private Reflections() {
        throw new AssertionError("not for you");
    }


    static Type getUniqueParameterType(ParameterizedType matcherType, String expectedForm) {
        return getUniqueParameterType(matcherType, null, expectedForm);
    }

    static Type getUniqueParameterType(ParameterizedType matcherType, @Nullable Class<?> expectedType, String expectedForm) {
        Type[] actualTypeArguments = matcherType.getActualTypeArguments();
        if (actualTypeArguments.length == 1 && ( expectedType == null || expectedType.isAssignableFrom(getRowType(actualTypeArguments[0])))) {
            return actualTypeArguments[0];
        }
        throw new IllegalArgumentException(String.format("matcher is not a parametrized type of %s but it's %s", expectedForm, matcherType));
    }

    static Type getUniqueParameterType(Class<?> type, Class<?> expectedOwner, String expectedForm) {
        return getUniqueParameterType(getParameterizedType(type, expectedOwner), expectedForm);
    }

    static Class<?> getRowType(Type type) {
        if (isClassType(type)) {
            return (Class<?>) type;
        }
        if (isParameterizedType(type)) {
            return (Class<?>)((ParameterizedType) type).getRawType();
        }

        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    public static boolean isClassType(Type type) {
        return type instanceof Class;
    }

    public static boolean isParameterizedType(Type type) {
        return type instanceof ParameterizedType;
    }

    private static ParameterizedType getParameterizedType(Class<?> matcher, Class<?> expectedType) {
        ParameterizedType matcherType = null;

        Type superType = matcher.getGenericSuperclass();
        if (expectedType.isAssignableFrom(getRowType(superType)) && isParameterizedType(superType)) {
            return (ParameterizedType) superType;
        }
        for (Type type : matcher.getGenericInterfaces()) {
            matcherType = asParameterizedType(type, expectedType);
        }
        if (matcherType == null) {
            throw new IllegalArgumentException("type must be of type: " + expectedType);
        }
        return matcherType;
    }

    private static ParameterizedType asParameterizedType(Type type, Class<?> expectedType) {
        ParameterizedType result = null;
        if (isParameterizedType(type)) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (expectedType.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                result = parameterizedType;
            }
        }
        return result;
    }
}
