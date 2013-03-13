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

package org.yar.guice.osgi;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.SynchronousBundleListener;
import org.yar.BlockingSupplierRegistry;
import org.yar.guice.RegistrationHandler;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;
import static org.yar.guice.GuiceYars.newRegistryDeclarationModule;

/**
 * This class provides utility methods to help you to handle Guice Injector creation.
 * TODO add tracker to remove service from registry when bundle is stopped
 * Date: 3/12/13
 * Time: 11:36 PM
 *
 * @author Romain Gilles
 */
public final class OSGiYars {

    private static final String SERVICE_REGISTRY_ERROR_MESSAGE = "no BlockingSupplierRegistry service reference found in OSGi service registry";

    private OSGiYars() {
        throw new AssertionError("not for you!");
    }

    public static Injector newInjector(BundleContext bundleContext, Module... modules) {
        return Guice.createInjector(getModules(bundleContext, asList(modules)));
    }

    public static Injector newInjector(BundleContext bundleContext, Stage stage, Module... modules) {
        return Guice.createInjector(stage, getModules(bundleContext, asList(modules)));
    }

    public static Injector newInjector(BundleContext bundleContext, Iterable<Module> modules) {
        return Guice.createInjector(getModules(bundleContext, modules));
    }

    public static Injector newInjector(BundleContext bundleContext, Stage stage, Iterable<Module> modules) {
        return trackBundle(bundleContext, Guice.createInjector(stage, getModules(bundleContext, modules)));
    }

    private static Injector trackBundle(BundleContext bundleContext, Injector injector) {
        registerInjector(bundleContext, injector);
        registerRegistrationHandler(bundleContext, injector);
        attachStoppingListener(bundleContext, injector);
        return injector;
    }

    private static void registerInjector(BundleContext bundleContext, Injector injector) {
        bundleContext.registerService(Injector.class, injector, null);
    }

    private static void registerRegistrationHandler(BundleContext bundleContext, Injector injector) {
        bundleContext.registerService(RegistrationHandler.class, injector.getInstance(RegistrationHandler.class), null);
    }

    private static void attachStoppingListener(BundleContext bundleContext, Injector injector) {
        bundleContext.addBundleListener(new BundleStoppingListener(injector));
    }

    private static Iterable<Module> getModules(BundleContext bundleContext, Iterable<Module> modules) {
        BlockingSupplierRegistry blockingSupplierRegistry = getBlockingSupplierRegistry(bundleContext);
        ImmutableList.Builder<Module> modulesBuilder = ImmutableList.builder();
        modulesBuilder.add(newRegistryDeclarationModule(blockingSupplierRegistry));
        modulesBuilder.addAll(modules);
        return modulesBuilder.build();
    }

    private static BlockingSupplierRegistry getBlockingSupplierRegistry(BundleContext bundleContext) {
        ServiceReference<BlockingSupplierRegistry> serviceReference = checkNotNull(bundleContext.getServiceReference(BlockingSupplierRegistry.class)
                , SERVICE_REGISTRY_ERROR_MESSAGE);
        return checkNotNull(bundleContext.getService(serviceReference), "BlockingSupplierRegistry service");
    }


    private static class BundleStoppingListener implements SynchronousBundleListener {
        private final Injector injector;

        private BundleStoppingListener(Injector injector) {
            this.injector = injector;
        }

        @Override
        public void bundleChanged(BundleEvent bundleEvent) {
            if (!isStopping(bundleEvent)) {
                return;
            }
            RegistrationHandler registrationHandler = injector.getInstance(RegistrationHandler.class);
            registrationHandler.clear();
        }

        private boolean isStopping(BundleEvent bundleEvent) {
            return BundleEvent.STOPPING == bundleEvent.getType();
        }
    }
}
