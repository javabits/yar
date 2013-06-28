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
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * This interface represents the identity of a {@link Supplier} in registry.
 * It doesn't imply uniqueness in other words you can find homonyms. It is
 * up to the registry implementation to impose or not unique identifier.
 * <p>The identity in the {@link Supplier} is made of:
 * <ol>
 * <li>A {@code Type} that represent the type supplied by the {@code Supplier}</li>
 * <li>A optional {@code Annotation} type and or instance that qualify
 * this specific {@code Supplier} instance. The annotation must have a
 * retention policy set to runtime</li>
 * </ol></p>
 * <p>{@code Id} support generic types.</p>
 * <p>To create instance of key have a look the {@link Ids} class.</p>
 *
 * @author Romain Gilles
 * @see Registry
 * @see Supplier
 * @see Ids
 * @since 1.0
 */
@SuppressWarnings("unused")
public interface Id<T> {
    /**
     * Returns the not {@code null} type of this {@code Id}.
     * The type is the minimum required to define an {@code Id}.
     *
     * @return the not {@code null} type of this {@code Id}.
     */
    Type type();

    /**
     * Returns the optional annotation type.
     * An annotation type can be used to qualify an {@code Id}. It can come
     * from the annotation instance or directly from a type, this depends on the construction
     * method used from {@link Ids} methods.
     *
     * @return an qualifying {@code Annotation} type or {@code null}.
     */
    @Nullable
    Class<? extends Annotation> annotationType();

    /**
     * Returns the optional annotation instance.
     * An annotation instance can be used to qualify an {@code Id}.
     *
     * @return an qualifying {@code Annotation} instance or {@code null}.
     */
    @Nullable
    Annotation annotation();
}
