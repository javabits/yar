package org.javabits.yar.guice.osgi;

import org.javabits.yar.Id;

import java.util.Set;

/**
 * This interface provide access to specific action from and to the registry
 * its associated has done against the registry.
 *
 * @author Romain Gilles
 */
public interface BundleRegistryWrapper {

    /**
     * Returns all the watchers registered to the registry from this bundle.
     */
    Set<Id<?>> getBundleWatchers();

    /**
     * Returns all the suppliers registered to the registry from this bundle.
     */
    Set<Id<?>> getBundleSuppliers();

    /**
     * Remove all the suppliers and the watchers that this bundle as registered.
     */
    void clear();
}
