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
import org.javabits.yar.Id;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static java.util.Objects.requireNonNull;

/**
 * TODO comment
 * Date: 2/10/13
 * Time: 11:15 PM
 *
 * @author Romain Gilles
 */
@Immutable
public class GuiceId<T> implements Id<T> {

    private final com.google.inject.Key<T> key;

    private GuiceId(com.google.inject.Key<T> key) {
        this.key = requireNonNull(key, "key");
    }

    @Override
    public Type type() {
        return key.getTypeLiteral().getType();
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return key.getAnnotationType();
    }

    @Override
    public Annotation annotation() {
        return key.getAnnotation();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GuiceId guiceKey = (GuiceId) o;

        return key.equals(guiceKey.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return key.toString();
    }

    public static <T> Id<T> of(com.google.inject.Key<T> key) {
        return new GuiceId<>(key);
    }

    @SuppressWarnings("unchecked")
    public static <T> Id<T> of(Type type, @Nullable Key<?> annotatedKey) {
        if (annotatedKey != null) {
            if (annotatedKey.getAnnotation() != null) {
                return of((Key<T>) Key.get(type, annotatedKey.getAnnotation()));
            } else if (annotatedKey.getAnnotationType() != null) {
                return of((Key<T>) Key.get(type, annotatedKey.getAnnotationType()));
            }
        }
        return of((Key<T>) Key.get(type));

    }

    public static <T> Id<T> of(Class<T> type) {
        return new GuiceId<>(com.google.inject.Key.get(type));
    }
}
