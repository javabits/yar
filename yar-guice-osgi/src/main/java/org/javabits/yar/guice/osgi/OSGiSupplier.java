package org.javabits.yar.guice.osgi;

import org.javabits.yar.BlockingSupplier;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;

import javax.annotation.Nullable;

/**
 * OSGi oriented extension of the {@link BlockingSupplier}.
 * This interface provide access to {@code Bundle}.
 */
public interface OSGiSupplier<T> extends BlockingSupplier<T>, BundleSupplier<T> {
    /**
     * Returns the bundle that registered the Supplier referenced by this
     * {@code OSGiSupplier} object.
     *
     * <p>
     * This method must return <t>null</t> when the Supplier has been
     * unregistered.
     *
     * @return The bundle that registered the supplier referenced by this
     *         {@code BlockingSupplier} object; {@code null} if that
     *         supplier has already been unregistered.
     */
    @Nullable
    Bundle getBundle();
}
