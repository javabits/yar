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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.javabits.yar.BlockingSupplierRegistry;
import org.javabits.yar.Registry;
import org.javabits.yar.guice.ExecutionStrategy;
import org.javabits.yar.guice.GuiceYars;

import static java.lang.Boolean.parseBoolean;
import static org.javabits.yar.guice.ExecutionStrategy.ASYNCHRONOUS;
import static org.javabits.yar.guice.ExecutionStrategy.SYNCHRONOUS;

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
public class Activator implements BundleActivator {
    /**
     * property use to lockup the timeout that operator can provide through bundle context.
     */
    public static final String YAR_DEFAULT_TIMEOUT = "yar.default.timeout";
    /**
     * property use to lockup to activate synchronous execution strategy that operator can provide through bundle context.
     */
    public static final String YAR_SYNCHRONOUS_EXECUTION = "yar.synchronous.execution";
    /**
     * Default timeout value of 5 min if no external property is provided by the framework.
     */
    public static final long DEFAULT_TIMEOUT = 1000 * 60 * 5;
    /**
     * Default timeout value of execution strategy if no external property is provided by the framework.
     */
    public static final ExecutionStrategy DEFAULT_EXECUTION_STRATEGY = ASYNCHRONOUS;

    private static final String[] REGISTRY_INTERFACES = new String[]{Registry.class.getName()
            , BlockingSupplierRegistry.class.getName()};

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        bundleContext.registerService(REGISTRY_INTERFACES, newRegistry(bundleContext), null);
    }

    private BlockingSupplierRegistry newRegistry(BundleContext bundleContext) {
        return GuiceYars.newLoadingCacheBasedBlockingSupplierRegistry(getDefaultTimeout(bundleContext), getExecutionStrategy(bundleContext));
    }

    private ExecutionStrategy getExecutionStrategy(BundleContext bundleContext) {
        String synchronously = bundleContext.getProperty(YAR_SYNCHRONOUS_EXECUTION);
        if (synchronously != null && parseBoolean(synchronously)) {
            return SYNCHRONOUS;
        } else {
            return DEFAULT_EXECUTION_STRATEGY;
        }
    }

    private long getDefaultTimeout(BundleContext bundleContext) {
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
