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

package org.javabits.yar.guice.osgi.internal;

import org.javabits.yar.BlockingSupplierRegistry;
import org.javabits.yar.Registry;
import org.javabits.yar.RegistryHook;
import org.javabits.yar.guice.BlockingSupplierFactory;
import org.javabits.yar.guice.NoWaitBlockingSupplierFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.logging.Logger;

import static java.lang.Boolean.parseBoolean;
import static org.javabits.yar.guice.BlockingSupplierFactory.DEFAULT_BLOCKING_SUPPLIER;
import static org.javabits.yar.guice.ExecutionStrategy.Type;
import static org.javabits.yar.guice.YarGuices.Builder;
import static org.javabits.yar.guice.YarGuices.builder;

/**
 * This class is responsible to create the Yar registry and register it into the OSGi registry under
 * {@link Registry} and {@link BlockingSupplierRegistry} interfaces.
 * <p>This class create a blocking supplier registry initialized with a default timeout value that
 * can be specified externally. If no value is specified then it use a default one {@link #DEFAULT_TIMEOUT}.
 * If the provided value cannot be parsed through {@link Long#parseLong(String)} method then a
 * {@code NumberFormatException} exception is throw and the activation fail</p>
 * TODO comment
 * Date: 3/13/13
 * Time: 9:13 AM
 *
 * @author Romain Gilles
 */
@SuppressWarnings("WeakerAccess")
public class Activator implements BundleActivator {
    private static final Logger LOG = Logger.getLogger(Activator.class.getName());
    /**
     * property use to lockup the timeout that operator can provide through bundle context.
     */
    public static final String YAR_DEFAULT_TIMEOUT = "yar.default.timeout";
    /**
     * property use to lockup to activate parallel execution strategy for watchers / listeners update that operator
     * can provide through bundle context or system properties.
     * If {@code true} then parallel mode is activated otherwise the serialized mode is used.
     */
    public static final String YAR_PARALLEL_EXECUTION_MODE = "yar.parallel.execution";
    /**
     * Default timeout value of 5 min if no external property is provided by the framework.
     * As specified in OSGi Blueprint container part.
     */
    public static final long DEFAULT_TIMEOUT = Registry.DEFAULT_TIMEOUT;

    /**
     * property use to define if the blocking strategy used for the suppliers. It can go to an no wait style.
     * This is not the standard behaviours but It can help in debug mode or if you want to go to a non blocking approach.
     */
    public static final String YAR_NO_WAIT = "yar.no.wait";

    private static final String[] REGISTRY_INTERFACES = new String[]{Registry.class.getName()
            , BlockingSupplierRegistry.class.getName(), RegistryHook.class.getName()};

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        BlockingSupplierRegistry registry = newRegistry(bundleContext);
        bundleContext.addBundleListener(new BundleTypeCleaner((RegistryHook) registry));
        bundleContext.registerService(REGISTRY_INTERFACES, registry, null);
    }

    private BlockingSupplierRegistry newRegistry(BundleContext bundleContext) {
        Builder builder = builder();
        builder.timeout(getExecutionTimeout(bundleContext))
                .timeUnit(Registry.DEFAULT_TIME_UNIT)
                .listenerUpdateExecutionStrategy(getExecutionStrategy(bundleContext))
                .blockingSupplierStrategy(getBlockingSupplierStrategy(bundleContext));
        LOG.info("Create Yar OSGi registry: " + builder);
        return builder.build();
    }

    private BlockingSupplierFactory getBlockingSupplierStrategy(BundleContext bundleContext) {
        String noWait = bundleContext.getProperty(YAR_NO_WAIT);
        if (noWait != null && parseBoolean(noWait)) {
            return new NoWaitBlockingSupplierFactory();
        }
        return DEFAULT_BLOCKING_SUPPLIER;
    }

    private Type getExecutionStrategy(BundleContext bundleContext) {
        String synchronously = bundleContext.getProperty(YAR_PARALLEL_EXECUTION_MODE);
        if (synchronously != null && parseBoolean(synchronously)) {
            return Type.PARALLEL;
        } else {
            return Type.SERIALIZED;
        }
    }

    private long getExecutionTimeout(BundleContext bundleContext) {
        String timeout = bundleContext.getProperty(YAR_DEFAULT_TIMEOUT);
        if (timeout != null) {
            return Long.parseLong(timeout);
        } else {
            return DEFAULT_TIMEOUT;
        }
    }


    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        //nothing to do let the framework un-register the registry.
    }
}
