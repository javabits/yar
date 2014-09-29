package org.javabits.yar.guice.osgi;

import org.javabits.yar.BlockingSupplierRegistry;

/**
 * Created by Romain on 29/09/2014.
 */
public interface BlockingSupplierRegistryAware extends Aware {
    void setBlockingSupplierRegistry(BlockingSupplierRegistry registry);
}
