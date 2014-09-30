package org.javabits.yar.guice.osgi;

import org.javabits.yar.Registry;

/**
 * This interface can be used to indicate that your are interesting in the registry.
 *
 * @see org.javabits.yar.guice.osgi.Aware
 */
public interface RegistryAware extends Aware {
    /**
     * Set the registry from where the supplied instance is requested before returned the instance.
     *
     * @param registry the registry from where the supplied instance is requested.
     */
    void setRegistry(Registry registry);
}
