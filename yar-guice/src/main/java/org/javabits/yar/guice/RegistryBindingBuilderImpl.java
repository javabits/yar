package org.javabits.yar.guice;

/**
 * TODO comment
 * Date: 5/14/13
 * Time: 10:08 AM
 *
 * @author Romain Gilles
 */
class RegistryBindingBuilderImpl implements RegistryBindingBuilder {

    private final RegistryProvider<?> registryProvider;

    public RegistryBindingBuilderImpl(RegistryProvider<?> registryProvider) {
        this.registryProvider = registryProvider;
    }

    @Override
    public void noWait() {
        registryProvider.noWait();
    }
}
