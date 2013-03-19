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

package org.yar.guice;

import javax.inject.Singleton;

/**
 * TODO comment
 * Only one {@code RegistryModule} per injector.
 * Date: 2/8/13
 * Time: 5:18 PM
 *
 * @author Romain Gilles
 * @since 1.0
 */
public abstract class RegistryModule extends AbstractRegistryModule {

    @Override
    void doBeforeConfiguration() {
        super.bind(RegistrationHandler.class).to(RegistrationBindingHandler.class).in(Singleton.class);
        super.bind(RegistryListenerHandler.class).to(RegistryListenerBindingHandler.class).in(Singleton.class);
    }
}
