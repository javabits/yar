package org.javabits.yar.guice.osgi;

import org.javabits.yar.Registry;

/**
 * Created by Romain on 29/09/2014.
 */
public interface RegistryAware extends Aware {
    void setRegistry(Registry registry);
}
