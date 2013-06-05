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

import javax.annotation.Nullable;

/**
 * Extension of the guava {@code Supplier} with it {@link Id}.
 * This class represents the registration that associates a {@code Supplier} to an {@code Id}.
 * It is created on registration phase and returned by all pull methods.
 *
 * @author Romain Gilles
 * @since 1.0
 */
public interface Supplier<T> extends com.google.common.base.Supplier<T> {

    Id<T> id();

    @Override
    @Nullable
    T get();
}
