/*
 * Copyright (c) 4/30/15 10:13 PM Romain Gilles
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

package org.javabits.yar.guice;

import com.google.inject.Key;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * Utility class to manipulate guice .
 */
public final class Keys {
    private Keys() {
        throw new AssertionError("Not for you!");
    }

    @SuppressWarnings("unchecked")
    public static <T> Key<T> of(Type type, @Nullable Key<?> annotatedKey) {
        if (annotatedKey != null) {
            if (annotatedKey.getAnnotation() != null) {
                return (Key<T>) Key.get(type, annotatedKey.getAnnotation());
            } else if (annotatedKey.getAnnotationType() != null) {
                return (Key<T>) Key.get(type, annotatedKey.getAnnotationType());
            }
        }
        return (Key<T>) Key.get(type);
    }
}
