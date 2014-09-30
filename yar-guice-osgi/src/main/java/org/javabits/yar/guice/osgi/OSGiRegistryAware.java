package org.javabits.yar.guice.osgi;

/**
 * This interface can be used to indicate that your are interesting in the OSGi registry.
 *
 * @see org.javabits.yar.guice.osgi.Aware
 */
public interface OSGiRegistryAware {
    /**
     * Set the OSGi registry from where the supplied instance is requested before returned it.
     *
     * @param registry the registry from where the supplied instance is requested.
     */
    void setOSGiRegistry(OSGiRegistry registry);
}
