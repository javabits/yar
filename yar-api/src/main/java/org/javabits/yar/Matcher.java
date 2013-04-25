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

package org.javabits.yar;

/**
 * Returns {@code true} or {@code false} for a given input.
 * TODO see if we need to move to predicate because it will be part of the next JDK 8
 * Date: 3/15/13
 *
 * @param <T> the matching type
 * @author Romain Gilles
 */
public interface Matcher<T> {
    /**
     * Returns {@code true} if this matches {@code item}, {@code false} otherwise.
     */
    boolean matches(T item);

}
