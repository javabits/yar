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

import com.google.inject.Key;

/**
 * TODO comment
 * Date: 2/10/13
 * Time: 10:11 AM
 *
 * @author Romain Gilles
 * @since 1.0
 */
class GuiceRegistration {

    private final Key<?> key;

    GuiceRegistration(Key<?> key) {
        this.key = key;
    }

    static GuiceRegistration get(Key<?> key) {
        return new GuiceRegistration(key);
    }

    public Key<?> key() {
        return key;
    }
}
