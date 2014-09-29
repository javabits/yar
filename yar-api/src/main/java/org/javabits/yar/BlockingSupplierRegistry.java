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

import com.google.common.reflect.TypeToken;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

/**
 * Date: 3/5/13
 * Time: 10:03 PM
 *
 * @author Romain Gilles
 * @since 1.0
 */
public interface BlockingSupplierRegistry extends Registry {

    @Nullable
    @Override
    <T> BlockingSupplier<T> get(Class<T> type);

    @Nullable
    @Override
    <T> BlockingSupplier<T> get(Id<T> id);

    @Nullable
    @Override
    <T> BlockingSupplier<T> get(TypeToken<T> type);


    /**
     * Returns the default timeout used for blocking operations.
     * The associated time unit is provided by {@link #defaultTimeUnit()}.
     * @return default timeout used for blocking operations.
     * @see #defaultTimeUnit()
     */
    long defaultTimeout();

    /**
     * Returns the default time unit used for blocking operations.
     * The associated timeout is provided by {@link #defaultTimeout()}.
     * @return default timeout used for blocking operations.
     * @see #defaultTimeout()
     */
    TimeUnit defaultTimeUnit();
}
