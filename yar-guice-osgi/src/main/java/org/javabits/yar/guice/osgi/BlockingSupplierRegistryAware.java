package org.javabits.yar.guice.osgi;

import org.javabits.yar.BlockingSupplierRegistry;

/**
 * This interface can be used to indicate that your are interesting in the registry.
 *
 * @see org.javabits.yar.guice.osgi.Aware
 */
public interface BlockingSupplierRegistryAware extends Aware {
    /**
     * Set the registry from where the supplied instance is requested before returned it.
     *
     * @param registry the registry from where the supplied instance is requested.
     */
    void setBlockingSupplierRegistry(BlockingSupplierRegistry registry);
}
