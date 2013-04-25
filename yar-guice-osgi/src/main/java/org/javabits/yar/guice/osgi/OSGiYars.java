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

package org.javabits.yar.guice.osgi;

import com.google.common.collect.ImmutableList;
import com.google.inject.*;
import org.osgi.framework.*;
import org.javabits.yar.BlockingSupplierRegistry;
import org.javabits.yar.guice.RegistrationHandler;
import org.javabits.yar.guice.RegistryListenerHandler;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;
import static org.javabits.yar.guice.GuiceYars.newRegistryDeclarationModule;

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
        RegistrationHandler registrationHandler = getRegistrationHandler(injector);
        registerRegistrationHandler(bundleContext, registrationHandler);
        RegistryListenerHandler registryListenerHandler = getRegistryListenerHandler(injector);
        registerListenerHandler(bundleContext, registryListenerHandler);
        attachStoppingListener(bundleContext, registrationHandler, registryListenerHandler);
        initHandlers(registrationHandler, registryListenerHandler);
        return injector;
    }

    private static void initHandlers(RegistrationHandler registrationHandler, RegistryListenerHandler registryListenerHandler) {
        registrationHandler.init();
        registryListenerHandler.init();
    }

    private static void registerInjector(BundleContext bundleContext, Injector injector) {
        bundleContext.registerService(Injector.class, injector, null);
    }

    private static ServiceRegistration<RegistrationHandler> registerRegistrationHandler(BundleContext bundleContext, RegistrationHandler registrationHandler) {
        return bundleContext.registerService(RegistrationHandler.class, registrationHandler, null);
    }

    private static RegistrationHandler getRegistrationHandler(Injector injector) {
        return injector.getInstance(RegistrationHandler.class);
    }

    private static ServiceRegistration<RegistryListenerHandler> registerListenerHandler(BundleContext bundleContext, RegistryListenerHandler registryListenerHandler) {
        return bundleContext.registerService(RegistryListenerHandler.class, registryListenerHandler, null);
    }

    private static RegistryListenerHandler getRegistryListenerHandler(Injector injector) {
        return injector.getInstance(RegistryListenerHandler.class);
    }

    private static void attachStoppingListener(BundleContext bundleContext, RegistrationHandler registrationHandler, RegistryListenerHandler registryListenerHandler) {
        bundleContext.addBundleListener(new BundleStoppingListener(registrationHandler, registryListenerHandler, bundleContext.getBundle().getBundleId()));
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
        private final RegistrationHandler registrationHandler;
        private final RegistryListenerHandler registryListenerHandler;
        private final long bundleId;

        private BundleStoppingListener(RegistrationHandler registrationHandler, RegistryListenerHandler registryListenerHandler, long bundleId) {
            this.registrationHandler = registrationHandler;
            this.registryListenerHandler = registryListenerHandler;
            this.bundleId = bundleId;
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
            return BundleEvent.STOPPING == bundleEvent.getType() && bundleEvent.getBundle().getBundleId() == bundleId;
        }

        private void clearSupplierRegistration() {
            registrationHandler.clear();
        }

        private void clearListenerRegistration() {
            registryListenerHandler.clear();
        }
    }
}
