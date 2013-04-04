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
import com.google.inject.*;
import org.osgi.framework.*;
import org.yar.BlockingSupplierRegistry;
import org.yar.guice.RegistrationHandler;
import org.yar.guice.RegistryListenerHandler;

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
        return start(bundleContext, Guice.createInjector(getModules(bundleContext, asList(modules))));
    }

    public static Injector newInjector(BundleContext bundleContext, Stage stage, Module... modules) {
        return start(bundleContext, Guice.createInjector(stage, getModules(bundleContext, asList(modules))));
    }

    public static Injector newInjector(BundleContext bundleContext, Iterable<Module> modules) {
        return start(bundleContext, Guice.createInjector(getModules(bundleContext, modules)));
    }

    public static Injector newInjector(BundleContext bundleContext, Stage stage, Iterable<Module> modules) {
        return start(bundleContext, Guice.createInjector(stage, getModules(bundleContext, modules)));
    }

    public static Injector start(BundleContext bundleContext, Injector injector) {
        registerInjector(bundleContext, injector);
        registerRegistrationHandler(bundleContext, getRegistrationHandler(injector));
        registerListenerHandler(bundleContext, getRegistryListenerHandler(injector));
        attachStoppingListener(bundleContext, injector);
        return injector;
    }

    private static void registerInjector(BundleContext bundleContext, Injector injector) {
        bundleContext.registerService(Injector.class, injector, null);
    }

    private static void registerRegistrationHandler(BundleContext bundleContext, RegistrationHandler registrationHandler) {
        bundleContext.registerService(RegistrationHandler.class, registrationHandler, null);
    }

    private static RegistrationHandler getRegistrationHandler(Injector injector) {
        return injector.getInstance(RegistrationHandler.class);
    }

    private static void registerListenerHandler(BundleContext bundleContext, RegistryListenerHandler registryListenerHandler) {
        bundleContext.registerService(RegistryListenerHandler.class, registryListenerHandler, null);
    }

    private static RegistryListenerHandler getRegistryListenerHandler(Injector injector) {
        return injector.getInstance(RegistryListenerHandler.class);
    }

    private static void attachStoppingListener(BundleContext bundleContext, Injector injector) {
        bundleContext.addBundleListener(new BundleStoppingListener(injector));
    }

    private static Iterable<Module> getModules(BundleContext bundleContext, Iterable<Module> modules) {
        ImmutableList.Builder<Module> modulesBuilder = ImmutableList.builder();
        modulesBuilder.add(newYarOSGiModule(bundleContext));
        modulesBuilder.addAll(modules);
        return modulesBuilder.build();
    }

    public static Module newYarOSGiModule(final BundleContext bundleContext) {
        final BlockingSupplierRegistry blockingSupplierRegistry = getBlockingSupplierRegistry(bundleContext);
        return new AbstractModule() {
            @Override
            protected void configure() {
                install(newRegistryDeclarationModule(blockingSupplierRegistry));
                install(newOSGiModule(bundleContext));
            }
        };
    }

    private static Module newOSGiModule(final BundleContext bundleContext) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(BundleContext.class).toInstance(bundleContext);
                bind(Bundle.class).toInstance(bundleContext.getBundle());
            }
        };
    }

    private static BlockingSupplierRegistry getBlockingSupplierRegistry(BundleContext bundleContext) {
        ServiceReference<BlockingSupplierRegistry> serviceReference = checkNotNull(bundleContext.getServiceReference(BlockingSupplierRegistry.class)
                , SERVICE_REGISTRY_ERROR_MESSAGE);
        return checkNotNull(bundleContext.getService(serviceReference), "BlockingSupplierRegistry service not available");
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
            clearSupplierRegistration();
            clearListenerRegistration();
        }

        private boolean isStopping(BundleEvent bundleEvent) {
            return BundleEvent.STOPPING == bundleEvent.getType();
        }

        private void clearSupplierRegistration() {
            RegistrationHandler registrationHandler = getRegistrationHandler(injector);
            registrationHandler.clear();
        }

        private void clearListenerRegistration() {
            RegistryListenerHandler registryListenerHandler = getRegistryListenerHandler(injector);
            registryListenerHandler.clear();
        }
    }
}
