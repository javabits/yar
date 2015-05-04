package org.javabits.yar.guice;

/**
 * TODO comment
 * Date: 5/14/13
 * Time: 10:08 AM
 *
 * @author Romain Gilles
 */
class RegistryBindingBuilderImpl<T> implements RegistryBindingBuilder {

    private final Iterable<RegistryProvider<?>> registryProviders;

    public RegistryBindingBuilderImpl(Iterable<RegistryProvider<?>> registryProviders) {
        this.registryProviders = registryProviders;
    }

    @Override
    public void noWait() {
        for (RegistryProvider<?> registryProvider : registryProviders) {
            registryProvider.noWait();
        }
    }
}
