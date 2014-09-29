/*
 * Copyright (c) 9/27/14 6:24 AM Romain Gilles
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

package org.javabits.yar.guice.osgi.internal;

import org.javabits.yar.guice.osgi.OSGiRegistry;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

/**
 * Factory .
 */
public class OSGiRegistryFactory implements ServiceFactory<OSGiRegistry> {
    @Override
    public OSGiRegistry getService(Bundle bundle, ServiceRegistration<OSGiRegistry> registration) {
        return null;
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration<OSGiRegistry> registration, OSGiRegistry service) {

    }
}
