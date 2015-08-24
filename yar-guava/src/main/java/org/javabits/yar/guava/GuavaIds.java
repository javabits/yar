/*
 * Copyright (c) 7/18/15 10:18 AM Romain Gilles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javabits.yar.guava;

import com.google.common.reflect.TypeToken;
import org.javabits.yar.Id;
import org.javabits.yar.Ids;

import java.lang.annotation.Annotation;

/**
 * Utility class to deal with Ids from Guava context.
 */
@SuppressWarnings("unchecked")
public final class GuavaIds {

    private GuavaIds() {
        throw new AssertionError("Not for you!");
    }

    /**
     * Returns a new {@link Id} which represents the {@code type}.
     *
     * @param type the type to which the new id has to be associated.
     * @param <T>  the type of the id.
     * @return an new {@code Id} based on the given type.
     */
    public static <T> Id<T> newId(final TypeToken<T> type) {
        return (Id<T>) Ids.newId(type.getType());
    }

    /**
     * Returns a new {@link Id} which represents the {@code type} qualified by
     * the {@code annotationClass}.
     *
     * @param type            the type to which the new id has to be associated.
     * @param annotationClass the qualifying annotation type.
     * @param <T>             the type of the id.
     * @return an new {@code Id} based on the given type and annotation type.
     */
    public static <T> Id<T> newId(final TypeToken<T> type, final Class<? extends Annotation> annotationClass) {
        return (Id<T>) Ids.newId(type.getType(), annotationClass);
    }

    /**
     * Returns a new {@link Id} which represents the {@code type} qualified by
     * the {@code annotation} instance.
     *
     * @param type       the type to which the new id has to be associated.
     * @param annotation the qualifying annotation.
     * @param <T>        the type of the id.
     * @return an new {@code Id} based on the given type and annotation.
     */
    public static <T> Id<T> newId(final TypeToken<T> type, final Annotation annotation) {
        return (Id<T>) Ids.newId(type.getType(), annotation);
    }
}
