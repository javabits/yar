package org.javabits.yar.guice.osgi;

import org.osgi.framework.Bundle;

/**
 * This interface can be used to indicate that your are interesting in the bundle.
 *
 * @see org.javabits.yar.guice.osgi.Aware
 */
public interface BundleAware extends Aware {
    /**
     * Set the bundle from where the supplied instance is requested before returned it.
     *
     * @param registry the registry from where the supplied instance is requested.
     */
    void setBundle(Bundle bundle);
}
