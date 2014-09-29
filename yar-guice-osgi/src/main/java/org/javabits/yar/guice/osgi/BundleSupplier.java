/*
 * Copyright (c) 9/27/14 5:58 AM Romain Gilles
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

package org.javabits.yar.guice.osgi;

import org.javabits.yar.Supplier;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;

import javax.annotation.Nullable;

/**
 * This class is responsible to return the bundle from which the supplier has been registered.
 * It can be used by don
 */
public interface BundleSupplier<T> extends Supplier<T>, BundleReference {
    /**
     * Returns the bundle that registered the Supplier referenced by this
     * {@code BundleSupplier} object.
     *
     * @return The bundle that registered the supplier referenced by this
     *         {@code BundleSupplier} object; {@code null} if that
     *         supplier has not been registered with OSGi context.
     */
    @Nullable
    Bundle getBundle();
}
