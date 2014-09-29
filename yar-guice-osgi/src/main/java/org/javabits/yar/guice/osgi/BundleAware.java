package org.javabits.yar.guice.osgi;

import org.osgi.framework.Bundle;

/**
 * Created by Romain on 29/09/2014.
 */
public interface BundleAware extends Aware {
    void setBundle(Bundle bundle);
}
