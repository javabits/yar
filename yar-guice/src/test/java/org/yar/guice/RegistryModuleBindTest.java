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
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(Registry.class).toInstance(registry);
            }
        };

        registry.put(GuiceKey.of(MyInterface.class),GuiceSupplier.of(new Provider<MyInterface>() {
            @Override
            public MyInterface get() {
                return new MyInterface() {};
            }
        }));
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
    public void testBindSupplier() {
        final Registry registry = new SimpleRegistry();
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(Registry.class).toInstance(registry);
            }
        };

        registry.put(GuiceKey.of(MyInterface.class),GuiceSupplier.of(new Provider<MyInterface>() {
            @Override
            public MyInterface get() {
                return new MyInterface() {};
            }
        }));
        Injector injector = Guice.createInjector(module, new RegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(new TypeLiteral<Supplier<MyInterface>> (){}).toRegistry();
            }
        });
        assertThat(injector.getInstance(Key.get(new TypeLiteral<Supplier<MyInterface>> (){})), is(not(nullValue())));
    }

    @Test(expected = CreationException.class)
    public void testBindBlockingSupplierErrorOnNonBlockingRegistry() {
        final Registry registry = new SimpleRegistry();
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(Registry.class).toInstance(registry);
            }
        };

        registry.put(GuiceKey.of(MyInterface.class),GuiceSupplier.of(new Provider<MyInterface>() {
            @Override
            public MyInterface get() {
                return new MyInterface() {};
            }
        }));
        Injector injector = Guice.createInjector(module, new RegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(new TypeLiteral<BlockingSupplier<MyInterface>> (){}).toRegistry();
            }
        });
        assertThat(injector.getInstance(Key.get(new TypeLiteral<BlockingSupplier<MyInterface>> (){})), is(not(nullValue())));
    }

    @Test
    public void testBindBlockingSupplier() {
        final Registry registry = new BlockingSupplierRegistry();
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(Registry.class).toInstance(registry);
            }
        };

        registry.put(GuiceKey.of(MyInterface.class),GuiceSupplier.of(new Provider<MyInterface>() {
            @Override
            public MyInterface get() {
                return new MyInterface() {};
            }
        }));
        Injector injector = Guice.createInjector(module, new RegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(new TypeLiteral<Supplier<MyInterface>> (){}).toRegistry();
            }
        });
        assertThat(injector.getInstance(Key.get(new TypeLiteral<Supplier<MyInterface>> (){})), is(not(nullValue())));
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

        registry.put(GuiceKey.of(MyInterface.class),GuiceSupplier.of(new Provider<MyInterface>() {
            @Override
            public MyInterface get() {
                return new MyInterface() {};
            }
        }));
        Injector injector = Guice.createInjector(module, new RegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(new TypeLiteral<BlockingSupplier<MyInterface>> (){}).toRegistry();
            }
        });
        assertThat(injector.getInstance(Key.get(new TypeLiteral<BlockingSupplier<MyInterface>> (){})), is(not(nullValue())));
    }

    static interface MyInterface {

    }
}
