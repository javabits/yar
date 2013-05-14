package org.javabits.yar.guice;


/**
 * TODO comment
 * Date: 5/14/13
 * Time: 10:06 AM
 *
 * @author Romain Gilles
 */
public interface RegistryBindingBuilder {
    /**
     * Disable the default blocking behavior of the registry provider.
     * @see org.javabits.yar.guice.RegistryLinkedBindingBuilder#toRegistry()
     */
    void noWait();
}
