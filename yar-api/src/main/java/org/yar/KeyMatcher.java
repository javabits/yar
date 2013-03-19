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

/**
 * Returns {@code true} or {@code false} for a given key input.
 * TODO future version have to support cation {@literal Matcher<?>} and bounds {@literal Matcher<? super XXX>} or {@literal Matcher<? extends XXX>}
 * Date: 3/15/13
 *
 * @param <T> the matching type
 * @author Romain Gilles
 */
public interface KeyMatcher<T> extends Matcher<Key<T>> {
    /**
     * Returns {@code true} if this matches {@code item}, {@code false} otherwise.
     */
    boolean matches(Key<T> otherKey);

    /**
     * Returns the template key that this matcher is looking for.
     * It is used to pre-filter content to avoid you to receive to many notification.
     */
    Key<T> key();

}
