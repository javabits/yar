package org.javabits.yar.guice;

import com.google.inject.Provider;

/**
 * TODO comment
 * Date: 5/14/13
 * Time: 10:44 AM
 *
 * @author Romain Gilles
 */
public interface RegistryProvider<T> extends Provider<T> {
    void noWait();
}
