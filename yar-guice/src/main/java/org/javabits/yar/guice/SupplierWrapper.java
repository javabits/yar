/*
 * Copyright (c) 9/29/14 9:51 AM Romain Gilles
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

import java.util.function.Supplier;

/**
 * Supplier wrapper interface can be used to recursively retrieve the original supplier.
 */
public interface SupplierWrapper<T> {
    Supplier<T> getWrapped();
}
