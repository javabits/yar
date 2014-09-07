package org.javabits.yar.guice.osgi;

import org.javabits.yar.BlockingSupplier;
import org.javabits.yar.BlockingSupplierRegistry;
import org.javabits.yar.Id;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * This interface provide access to specific action from and to the registry
 * its associated has done against the registry.
 *
 * @author Romain Gilles
 */
@SuppressWarnings("WeakerAccess")
public interface BundleRegistryWrapper extends BlockingSupplierRegistry {

    /**
     * Returns all the watchers registered into the registry from this bundle.
     */
    Collection<Id<?>> getBundleWatchers();

    /**
     * Returns all the suppliers registered into the registry from this bundle.
     */
    Collection<Id<?>> getBundleSuppliers();

    /**
     * Remove all the suppliers and the watchers that this bundle as registered.
     */
    void clear();

    @Nullable
    @Override
    <T> OSGiBlockingSupplier<T> get(Class<T> type);

    @Nullable
    @Override
    <T> OSGiBlockingSupplier<T> get(Id<T> id);
}
