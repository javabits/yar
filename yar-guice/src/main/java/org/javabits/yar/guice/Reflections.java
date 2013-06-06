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

import javax.annotation.Nullable;
import java.lang.reflect.*;

import static com.google.common.base.Preconditions.checkArgument;

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

    @Nullable
    public static String typeToString(@Nullable Type type) {
        if (type == null) {
            return null;
        }
        return type instanceof Class ? ((Class) type).getName() : type.toString();
    }

    static Type getUniqueParameterType(ParameterizedType matcherType, String expectedForm) {
        return getUniqueParameterType(matcherType, null, expectedForm);
    }

    static Type getUniqueParameterType(ParameterizedType matcherType, @Nullable Class<?> expectedType, String expectedForm) {
        Type[] actualTypeArguments = matcherType.getActualTypeArguments();
        if (actualTypeArguments.length == 1 && (expectedType == null || expectedType.isAssignableFrom(getRawType(actualTypeArguments[0])))) {
            return actualTypeArguments[0];
        }
        throw new IllegalArgumentException(String.format("matcher is not a parametrized type of %s but it's %s", expectedForm, matcherType));
    }

    public static Type getUniqueParameterType(Class<?> type, Class<?> expectedOwner, String expectedForm) {
        return getUniqueParameterType(getParameterizedType(type, expectedOwner), expectedForm);
    }

    /**
     * Copy paste from Guice MoreTypes
     */
    @SuppressWarnings("unckecked")
    public static Class<?> getRawType(Type type) {
        if (type instanceof Class<?>) {
            // type is a normal class.
            return (Class<?>) type;

        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            // I'm not exactly sure why getRawType() returns Type instead of Class.
            // Neal isn't either but suspects some pathological case related
            // to nested classes exists.
            Type rawType = parameterizedType.getRawType();
            checkArgument(rawType instanceof Class,
                    "Expected a Class, but <%s> is of type %s", type, type.getClass().getName());
            return (Class<?>) rawType;

        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();

        } else if (type instanceof TypeVariable) {
            // we could use the variable's bounds, but that'll won't work if there are multiple.
            // having a raw type that's more general than necessary is okay
            return Object.class;

        } else {
            throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                    + "GenericArrayType, but <" + type + "> is of type " + type.getClass().getName());
        }
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
        if (expectedType.isAssignableFrom(getRawType(superType)) && isParameterizedType(superType)) {
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
