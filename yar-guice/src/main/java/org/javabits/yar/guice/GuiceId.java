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
import org.javabits.yar.Ids;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.lang.reflect.Type;

/**
 * This class provides utility methods to convert Guice's {@code Key}s to
 * Yar {@code Id}s.
 * <p/>
 * Date: 2/10/13
 *
 * @author Romain Gilles
 */
@Immutable
public final class GuiceId {

    public static <T> Id<T> of(com.google.inject.Key<T> key) {
        return of(key.getTypeLiteral().getType(), key);
    }

    @SuppressWarnings("unchecked")
    public static <T> Id<T> of(Type type, @Nullable Key<?> annotatedKey) {
        if (annotatedKey != null) {
            if (annotatedKey.getAnnotation() != null) {
                return (Id<T>) Ids.newId(type, annotatedKey.getAnnotation());
            } else if (annotatedKey.getAnnotationType() != null) {
                return (Id<T>) Ids.newId(type, annotatedKey.getAnnotationType());
            }
        }
        return (Id<T>) Ids.newId(type);

    }
}
