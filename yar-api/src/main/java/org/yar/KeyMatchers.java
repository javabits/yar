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

package org.yar;

import static java.util.Objects.requireNonNull;

/**
 * TODO comment
 * Date: 3/15/13
 *
 * @author Romain Gilles
 */
public final class KeyMatchers {

    private KeyMatchers() {
        throw new AssertionError("not for you!");
    }

    public static <T> KeyMatcher<T> newKeyMatcher(final Key<T> key) {
        requireNonNull(key, "key");
        return new KeyMatcher<T>() {

            @Override
            public boolean matches(Key<T> otherKey) {
                return key.equals(otherKey);
            }

            @Override
            public Key<T> key() {
                return key;
            }
        };
    }
}