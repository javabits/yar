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
public interface Supplier<T> extends java.util.function.Supplier<T> {

    /**
     * Returns the {@link Id} under which this supplier has been registered
     * through the <t>put(..)</t> methods.
     * @return the Id of this supplier under which it has been registered.
     */
    Id<T> id();

    /**
     * Retrieves an instance of the appropriate type. The returned object may or
     * may not be a new instance, depending on the implementation.
     * <p>Implementation can return <t>null</t> if it can not produce the instance.
     * </p>
     * <p>Instance of this class must be thread safe and sharable.</p>
     *
     * @return an instance of the appropriate type. It can be <t>null</t> if the underlying implementation
     * can not produce instance.
     */
    @Override
    @Nullable
    T get();

    /**
     * Returns the original / native supplier provided to this registry through a call to one of the <t>pull(..)</t>.
     *
     * @return <t>null</t> if the underlying native supplier is not yet set.
     */
    @Nullable
    java.util.function.Supplier<? extends T> getNativeSupplier();
}
