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

package org.javabits.yar.guice;

import com.google.inject.binder.LinkedBindingBuilder;

import java.util.concurrent.TimeUnit;

/**
 * TODO comment
 * Date: 2/9/13
 * Time: 10:19 AM
 *
 * @author Romain Gilles
 */
public interface RegistryLinkedBindingBuilder<T> extends LinkedBindingBuilder<T> {
    /**
     * Bind the current key to the Yar registry.
     * By default this binding produce a provider that will block in when you call
     * the {@code Provider#get()) method until the default timeout is reach.
     * You can skip this default blocking behavior by calling {@link org.javabits.yar.guice.RegistryBindingBuilder#noWait()
     * noWait()} method on the return type.
     * @return {@code RegistryBindingBuilder} instance to skip default blocking behavior if needed.
     */
    RegistryBindingBuilder toRegistry();

    void toRegistry(long timeout, TimeUnit unit);
}
