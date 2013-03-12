/*
 * Copyright 2013 Romain Gilles
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.yar.guice;

import com.google.inject.*;
import org.junit.Test;
import org.yar.BlockingSupplier;
import org.yar.Registry;
import org.yar.Supplier;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * TODO comment
 * Date: 2/19/13
 * Time: 9:05 AM
 *
 * @author Romain Gilles
 */
public class RegistryModuleBindTest {
    @Test
    public void testBind() {
        final Registry registry = new SimpleRegistry();
        Module module = createModuleRegistryDeclaration(registry, Registry.class);
        putMyInterfaceSupplierToRegistry(registry);
        Injector injector = Guice.createInjector(module, new RegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(MyInterface.class).toRegistry();
            }
        });
        assertThat(injector.getInstance(MyInterface.class), is(not(nullValue())));
        assertThat(injector.getInstance(MyInterface.class), is(not(injector.getInstance(MyInterface.class))));
    }


    @Test
    public void testBindListToRegistry() {
        final Registry registry = createLoadingCacheRegistryWithMyInterfaceSupplier();
        Module module = createModuleRegistryDeclaration(registry, Registry.class);
        final TypeLiteral<List<MyInterface>> listTypeLiteral = new TypeLiteral<List<MyInterface>>() {
        };
        Injector injector = Guice.createInjector(module, new RegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(listTypeLiteral).toRegistry();
            }
        });
        Key<List<MyInterface>> listKey = Key.get(listTypeLiteral);
        Iterable<MyInterface> myInterfaceList = injector.getInstance(listKey);
        assertThat(myInterfaceList, is(not(nullValue())));
        assertThat(myInterfaceList, is(not(emptyIterable())));
    }

    private Registry createLoadingCacheRegistryWithMyInterfaceSupplier() {
        final Registry registry = SimpleRegistry.newLoadingCacheRegistry();
        putMyInterfaceSupplierToRegistry(registry);
        return registry;
    }

    @Test
    public void testBindCollectionToRegistry() {
        final Registry registry = createLoadingCacheRegistryWithMyInterfaceSupplier();
        Module module = createModuleRegistryDeclaration(registry, Registry.class);
        final TypeLiteral<Collection<MyInterface>> listTypeLiteral = new TypeLiteral<Collection<MyInterface>>() {
        };
        Injector injector = Guice.createInjector(module, new RegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(listTypeLiteral).toRegistry();
            }
        });
        Key<Collection<MyInterface>> listKey = Key.get(listTypeLiteral);
        Iterable<MyInterface> myInterfaceList = injector.getInstance(listKey);
        assertThat(myInterfaceList, is(not(nullValue())));
        assertThat(myInterfaceList, is(not(emptyIterable())));
    }

    @Test
    public void testBindIterableToRegistry() {
        final Registry registry = createLoadingCacheRegistryWithMyInterfaceSupplier();
        Module module = createModuleRegistryDeclaration(registry, Registry.class);
        final TypeLiteral<Iterable<MyInterface>> listTypeLiteral = new TypeLiteral<Iterable<MyInterface>>() {
        };
        Injector injector = Guice.createInjector(module, new RegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(listTypeLiteral).toRegistry();
            }
        });
        Key<Iterable<MyInterface>> listKey = Key.get(listTypeLiteral);
        Iterable<MyInterface> myInterfaceList = injector.getInstance(listKey);
        assertThat(myInterfaceList, is(not(nullValue())));
        assertThat(myInterfaceList, is(not(emptyIterable())));
    }

    @Test
    public void testBindSupplier() {
        final Registry registry = new SimpleRegistry();
        putMyInterfaceSupplierToRegistry(registry);
        Module module = createModuleRegistryDeclaration(registry, Registry.class);

        Injector injector = createSupplierBindingInjector(module);
        assertThat(injector.getInstance(Key.get(new TypeLiteral<Supplier<MyInterface>> (){})), is(not(nullValue())));
    }

    @Test(expected = CreationException.class)
    public void testBindBlockingSupplierErrorOnNonBlockingRegistry() {
        final Registry registry = new SimpleRegistry();
        final Class<Registry> clazz = Registry.class;
        Module module = createModuleRegistryDeclaration(registry, clazz);

        putMyInterfaceSupplierToRegistry(registry);
        Injector injector = createBlockingSupplierBindingInjector(module);
        assertThat(injector.getInstance(Key.get(new TypeLiteral<BlockingSupplier<MyInterface>> (){})), is(not(nullValue())));
    }

    private <T extends Registry> Module createModuleRegistryDeclaration(final T registry, final Class<T> clazz) {
        return new AbstractModule() {
                @Override
                protected void configure() {
                    bind(clazz).toInstance(registry);
                }
            };
    }

    @Test
    public void testBindBlockingSupplier() {
        final Registry registry = new BlockingSupplierRegistry();
        Module module = createModuleRegistryDeclaration(registry, Registry.class);
        putMyInterfaceSupplierToRegistry(registry);
        Injector injector = createSupplierBindingInjector(module);
        assertThat(injector.getInstance(Key.get(new TypeLiteral<Supplier<MyInterface>> (){})), is(not(nullValue())));
    }

    private Injector createSupplierBindingInjector(Module module) {
        RegistryModule supplierBindingRegistryModule = createSupplierBindingRegistryModule();
        return createInjector(module, supplierBindingRegistryModule);
    }

    private Injector createInjector(Module module, RegistryModule registryModule) {
        return Guice.createInjector(Stage.PRODUCTION, module, registryModule);
    }

    private RegistryModule createSupplierBindingRegistryModule() {
        return new RegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(new TypeLiteral<Supplier<MyInterface>>() {
                }).toRegistry();
            }
        };
    }
    private RegistryModule createBlockingSupplierBindingRegistryModule() {
        return new RegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(new TypeLiteral<BlockingSupplier<MyInterface>>() {
                }).toRegistry();
            }
        };
    }

    private void putMyInterfaceSupplierToRegistry(Registry registry) {
        registry.put(GuiceKey.of(MyInterface.class), GuiceSupplier.of(new Provider<MyInterface>() {
            @Override
            public MyInterface get() {
                return new MyInterface() {
                };
            }
        }));
    }

    @Test
    public void testBindBlockingSupplier2() {
        final org.yar.BlockingSupplierRegistry registry = new BlockingSupplierRegistry();
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                Key<org.yar.BlockingSupplierRegistry> blockingSupplierRegistryKey = Key.get(org.yar.BlockingSupplierRegistry.class);
                bind(Registry.class).to(blockingSupplierRegistryKey);
                bind(blockingSupplierRegistryKey).toInstance(registry);
            }
        };

        putMyInterfaceSupplierToRegistry(registry);
        Injector injector = createBlockingSupplierBindingInjector(module);
        Supplier<MyInterface> supplier = injector.getInstance(Key.get(new TypeLiteral<BlockingSupplier<MyInterface>>() {
        }));
        assertThat(supplier, is(not(nullValue())));
        assertThat(supplier.get(), is(not(nullValue())));
    }

    private Injector createBlockingSupplierBindingInjector(Module module) {
        return createInjector(module, createBlockingSupplierBindingRegistryModule());
    }

    static interface MyInterface {

    }
}
