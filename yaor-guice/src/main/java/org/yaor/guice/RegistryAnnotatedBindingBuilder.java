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

package org.yaor.guice;

import com.google.inject.binder.AnnotatedBindingBuilder;

import java.lang.annotation.Annotation;

/**
 * ¶
 *
 * @author Romain Gilles
 *         Date: 2/8/13
 *         Time: 10:23 PM
 * @since 1.0
 */
public interface RegistryAnnotatedBindingBuilder<T> extends RegistryLinkedBindingBuilder<T>, AnnotatedBindingBuilder<T> {

    /**
     * See the EDSL examples at {@link com.google.inject.Binder}.
     */
    RegistryLinkedBindingBuilder<T> annotatedWith(
            Class<? extends Annotation> annotationType);

    /**
     * See the EDSL examples at {@link com.google.inject.Binder}.
     */
    RegistryLinkedBindingBuilder<T> annotatedWith(Annotation annotation);
}
