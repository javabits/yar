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

package org.javabits.yar.guice;

import com.google.inject.*;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.javabits.yar.*;
import org.javabits.yar.BlockingSupplierRegistry;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.javabits.yar.guice.BlockingSupplierRegistryImpl.newBlockingSupplierRegistry;
import static org.javabits.yar.guice.YarGuices.*;


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
        final BlockingSupplierRegistry registry = newBlockingSupplierRegistry();
        Module module = newRegistryDeclarationModule(registry);
        putMyInterfaceSupplierToRegistry(registry);
        Injector injector = Guice.createInjector(module, new RegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(MyInterface.class).toRegistry(100, TimeUnit.MICROSECONDS);
            }
        });
        assertThat(injector.getInstance(MyInterface.class), is(not(nullValue())));
        assertThat(injector.getInstance(MyInterface.class), is(not(injector.getInstance(MyInterface.class))));
    }


    @Test
    public void testBindNoWait() {
        final BlockingSupplierRegistry registry = newBlockingSupplierRegistry();
        Module module = newRegistryDeclarationModule(registry);
        Injector injector = Guice.createInjector(module, new RegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(MyInterface.class).toRegistry().noWait();
            }
        });
        assertThat(injector.getInstance(MyInterface.class), is(nullValue()));
    }

    @Test
    public void testBindWaitWithException() {
        final BlockingSupplierRegistry registry = newBlockingSupplierRegistry();
        Module module = newRegistryDeclarationModule(registry);
        Injector injector = Guice.createInjector(module, new RegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(MyInterface.class).toRegistry().noWait();
            }
        });
        assertThat(injector.getInstance(MyInterface.class), is(nullValue()));
    }

    @Test
    public void testBindWithAnnotation() {
        final BlockingSupplierRegistry registry = newBlockingSupplierRegistry();
        Module module = newRegistryDeclarationModule(registry);
        final Named test = Names.named("test");
        final Key<MyInterface> key = Key.get(MyInterface.class, test);
        Id<MyInterface> id = GuiceId.of(key);
        getMyInterfaceRegistration(registry, id);
        Injector injector = Guice.createInjector(module, new RegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(key).toRegistry(100, TimeUnit.MICROSECONDS);
            }
        });
        assertThat(injector.getInstance(key), is(not(nullValue())));
    }


    @Test
    public void testBindListToRegistry() {
        Module module = createRegistryDeclarationModuleWithSimpleRegistry();
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

    private Module createRegistryDeclarationModuleWithSimpleRegistry() {
        final Registry registry = createLoadingCacheRegistryWithMyInterfaceSupplier();
        return YarGuices.newRegistryDeclarationModule(registry);
    }

    private Registry createLoadingCacheRegistryWithMyInterfaceSupplier() {
        final Registry registry = newLoadingCacheBasedRegistry();
        putMyInterfaceSupplierToRegistry(registry);
        return registry;
    }

    @Test
    public void testBindCollectionToRegistry() {
        Module module = createRegistryDeclarationModuleWithSimpleRegistry();
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
        Module module = createRegistryDeclarationModuleWithSimpleRegistry();
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
        final BlockingSupplierRegistry registry = newBlockingSupplierRegistry();
        Module module = newRegistryDeclarationModule(registry);
        Injector injector = createSupplierBindingInjector(module);
        assertThat(injector.getInstance(Key.get(new TypeLiteral<Supplier<MyInterface>>() {
        })), is(not(nullValue())));
    }

    @Test
    public void testBindSupplierWithAnnotation() {
        final BlockingSupplierRegistry registry = newBlockingSupplierRegistry();
        Module module = newRegistryDeclarationModule(registry);
        final Named test = Names.named("test");
        final Key<MyInterface> key = Key.get(MyInterface.class, test);
        final Key<Supplier<MyInterface>> supplierKey = Key.get(new TypeLiteral<Supplier<MyInterface>>() {
        }, test);
        Id<MyInterface> id = GuiceId.of(key);
        getMyInterfaceRegistration(registry, id);
        RegistryModule supplierBindingRegistryModule = new RegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(supplierKey).toRegistry(100, TimeUnit.MICROSECONDS);
            }
        };
        Injector injector = createInjector(module, supplierBindingRegistryModule);
        Supplier<MyInterface> supplier = injector.getInstance(supplierKey);
        assertThat(supplier, is(not(nullValue())));
        assertThat(supplier.get(), is(not(nullValue())));

    }


    @Test
    public void testBindGuavaSupplier() {
        final BlockingSupplierRegistry registry = newBlockingSupplierRegistry();
        Module module = newRegistryDeclarationModule(registry);
        final TypeLiteral<com.google.common.base.Supplier<MyInterface>> supplierTypeLiteral = new TypeLiteral<com.google.common.base.Supplier<MyInterface>>() {
        };
        RegistryModule supplierBindingRegistryModule = new RegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(supplierTypeLiteral).toRegistry(100, TimeUnit.MICROSECONDS);
            }
        };
        Injector injector = createInjector(module, supplierBindingRegistryModule);
        assertThat(injector.getInstance(Key.get(supplierTypeLiteral)), is(not(nullValue())));
    }

    @Test(expected = CreationException.class)
    public void testBindBlockingSupplierErrorOnNonBlockingRegistry() {
        Module module = createRegistryDeclarationModuleWithSimpleRegistry();
        Injector injector = createBlockingSupplierBindingInjector(module);
        assertThat(injector.getInstance(Key.get(new TypeLiteral<BlockingSupplier<MyInterface>>() {
        })), is(not(nullValue())));
    }

    @Test
    public void testBindBlockingSupplier() {
        final BlockingSupplierRegistry registry = newBlockingSupplierRegistry();
        Module module = newRegistryDeclarationModule(registry);
        putMyInterfaceSupplierToRegistry(registry);
        Injector injector = createSupplierBindingInjector(module);
        assertThat(injector.getInstance(Key.get(new TypeLiteral<Supplier<MyInterface>>() {
        })), is(not(nullValue())));
    }

    private Injector createSupplierBindingInjector(Module module) {
        RegistryModule supplierBindingRegistryModule = createSupplierBindingRegistryModule();
        return createInjector(module, supplierBindingRegistryModule);
    }

    private Injector createInjector(Module module, RegistryModule registryModule) {
        Injector injector = Guice.createInjector(Stage.PRODUCTION, module, registryModule);
        injector.getInstance(RegistrationHandler.class).init();
        injector.getInstance(RegistryListenerHandler.class).init();
        return injector;
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

    private Registration<MyInterface> putMyInterfaceSupplierToRegistry(Registry registry) {
        Id<MyInterface> id = Ids.newId(MyInterface.class);
        return getMyInterfaceRegistration(registry, id);
    }

    private Registration<MyInterface> getMyInterfaceRegistration(Registry registry, Id<MyInterface> id) {
        return registry.put(id, GuiceSupplier.of(new Provider<MyInterface>() {
            @Override
            public MyInterface get() {
                return new MyInterface() {
                };
            }
        }));
    }

    @Test
    public void testBindBlockingSupplier2() {
        final org.javabits.yar.BlockingSupplierRegistry registry = newLoadingCacheBlockingSupplierRegistry();
        Module module = newRegistryDeclarationModule(registry);
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

    @Test
    public void testBindBlockingSupplierWithAnnotation() {
        final org.javabits.yar.BlockingSupplierRegistry registry = newLoadingCacheBlockingSupplierRegistry();
        final Named test = Names.named("test");
        final Key<MyInterface> key = Key.get(MyInterface.class, test);
        final Key<BlockingSupplier<MyInterface>> supplierKey = Key.get(new TypeLiteral<BlockingSupplier<MyInterface>>() {
        }, test);
        Id<MyInterface> id = GuiceId.of(key);
        getMyInterfaceRegistration(registry, id);
        Module module = newRegistryDeclarationModule(registry);
        RegistryModule supplierBindingRegistryModule = new RegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(supplierKey).toRegistry();
            }
        };
        Injector injector = createInjector(module, supplierBindingRegistryModule);
        Supplier<MyInterface> supplier = injector.getInstance(supplierKey);
        assertThat(supplier, is(not(nullValue())));
        assertThat(supplier.get(), is(not(nullValue())));

    }

    @Test
    public void testBindListenerBasic() {
        final org.javabits.yar.BlockingSupplierRegistry registry = newLoadingCacheBlockingSupplierRegistry();
        Module module = newRegistryDeclarationModule(registry);
        final Object[] matches = new Object[]{0, null, null};

        Injector injector = createInjector(module, new RegistryModule() {
            @Override
            protected void configureRegistry() {
                bindRegistryListener(
                        new AbstractMatcher<Key<MyInterface>>() {
                            @Override
                            public boolean matches(Key<MyInterface> item) {
                                boolean equals = Key.get(MyInterface.class).equals(item);
                                matches[0] = (Integer) matches[0] + (equals ? 1 : 0);
                                return equals;
                            }
                        }, new RegistryListener<MyInterface>() {
                            @Nullable
                            @Override
                            public Supplier<MyInterface> add(Supplier<MyInterface> element) {
                                matches[1] = element;
                                return element;
                            }

                            @Override
                            public void remove(Supplier<MyInterface> element) {
                                matches[2] = element;
                            }
                        }
                );
            }
        });
        assertThat(injector, is(not(nullValue())));
        SupplierRegistration<MyInterface> myInterfaceRegistration = (SupplierRegistration<MyInterface>) putMyInterfaceSupplierToRegistry(registry);
        registry.remove(myInterfaceRegistration);
        assertThat(((Integer) matches[0]), is(2));
        assertThat((matches[1]), is(notNullValue()));
        assertThat((matches[2]), is(matches[1]));
    }


    @Test
    public void testBindListenerSingleElement() {
        final org.javabits.yar.BlockingSupplierRegistry registry = newLoadingCacheBlockingSupplierRegistry();
        Module module = newRegistryDeclarationModule(registry);
        final Object[] matches = new Object[]{0, null, null};

        Injector injector = createInjector(module, new RegistryModule() {
            @Override
            protected void configureRegistry() {
                bindRegistryListener(
                        new AbstractMatcher<Key<MyInterface>>() {
                            @Override
                            public boolean matches(Key<MyInterface> item) {
                                boolean equals = Key.get(MyInterface.class).equals(item);
                                matches[0] = (Integer) matches[0] + (equals ? 1 : 0);
                                return equals;
                            }
                        }, new MyInterfaceSingleElementWatcher(matches)
                );
            }
        });
        SupplierRegistration<MyInterface> myInterfaceRegistration = (SupplierRegistration<MyInterface>) putMyInterfaceSupplierToRegistry(registry);
        registry.remove(myInterfaceRegistration);

        assertThat(injector, is(not(nullValue())));
        assertThat(((Integer) matches[0]), is(2));
        assertThat(matches[1], is(notNullValue()));
        assertThat((matches[2]), is(matches[1]));
    }


    @Test
    public void testBindListener() {
        final org.javabits.yar.BlockingSupplierRegistry registry = newLoadingCacheBlockingSupplierRegistry();
        Module module = newRegistryDeclarationModule(registry);
        Injector injector = createInjector(module, new RegistryModule() {
            @Override
            protected void configureRegistry() {
                bindRegistryListener(new AbstractMatcher<Key<MyInterface>>() {
                                         @Override
                                         public boolean matches(Key<MyInterface> item) {
                                             return false;
                                         }
                                     }, new RegistryListener<MyInterface>() {
                                         @Nullable
                                         @Override
                                         public Supplier<MyInterface> add(Supplier<MyInterface> element) {
                                             return element;
                                         }

                                         @Override
                                         public void remove(Supplier<MyInterface> element) {

                                         }
                                     }
                );

                bindRegistryListener(new AbstractMatcher<Key<MyInterface>>() {
                                         @Override
                                         public boolean matches(Key<MyInterface> item) {
                                             return false;
                                         }
                                     }, new RegistryListener<Object>() {
                                         @Nullable
                                         @Override
                                         public Supplier<Object> add(Supplier<Object> element) {
                                             return element;
                                         }

                                         @Override
                                         public void remove(Supplier<Object> element) {

                                         }
                                     }
                );

//                bindListenerBounded(new org.javabits.yar.Matcher<Id<? extends MyInterface>>() {
//                                 @Override
//                                 public boolean matches(Id<? extends MyInterface> item) {
//                                     return false;  //To change body of implemented methods use File | Settings | File Templates.
//                                 }
//                             }, new RegistryListener<MyInterface>() {
//                                 @Nullable
//                                 @Override
//                                 public MyInterface add(MyInterface element) {
//                                     return null;  //To change body of implemented methods use File | Settings | File Templates.
//                                 }
//
//                                 @Override
//                                 public void remove(MyInterface element) {
//                                     //To change body of implemented methods use File | Settings | File Templates.
//                                 }
//                             }
//                );
//                bindListenerBounded(new org.javabits.yar.Matcher<Id<? extends MyInterface>>() {
//                                 @Override
//                                 public boolean matches(Id<? extends MyInterface> item) {
//                                     return false;  //To change body of implemented methods use File | Settings | File Templates.
//                                 }
//                             }, new RegistryListener<Object>() {
//                                 @Nullable
//                                 @Override
//                                 public MyInterface add(Object element) {
//                                     return null;  //To change body of implemented methods use File | Settings | File Templates.
//                                 }
//
//                                 @Override
//                                 public void remove(Object element) {
//                                     //To change body of implemented methods use File | Settings | File Templates.
//                                 }
//                             }
//                );
            }
        });
        assertThat(injector, is(not(nullValue())));
        putMyInterfaceSupplierToRegistry(registry);

    }

    @Test
    public void testMultiParametersGeneric() {
        final BlockingSupplierRegistry registry = newBlockingSupplierRegistry();
        Module module = newRegistryDeclarationModule(registry);
        final TypeLiteral<MultiParametersGeneric<String, String>> typeLiteral = new TypeLiteral<MultiParametersGeneric<String, String>>() {
        };
        Injector injector = Guice.createInjector(module, new RegistryModule() {
            @Override
            protected void configureRegistry() {

                register(typeLiteral).to(MultiParametersGenericImpl.class);
            }
        });
        assertThat(injector.getInstance(Key.get(typeLiteral)), is(not(nullValue())));
        assertThat(injector.getInstance(Key.get(typeLiteral)).accept("test"), is("test"));
    }

    static interface MultiParametersGeneric<I, O> {
        O accept(I input);
    }

    static class MultiParametersGenericImpl implements MultiParametersGeneric<String, String> {
        @Override
        public String accept(String input) {
            return input;
        }
    }

    static interface MyInterface {

    }

    static class MyInterfaceSingleElementWatcher extends AbstractSingleElementWatcher<MyInterface> implements RegistryListener<MyInterface> {

        final private Object[] matches;

        MyInterfaceSingleElementWatcher(Object[] matches) {
            this.matches = matches;
        }

        @Nullable
        @Override
        public Supplier<MyInterface> doAdd(Supplier<MyInterface> element) {
            if (matches[1] != null) {
                return null;
            }
            matches[1] = element;
            return element;
        }

        @Override
        public void doRemove(Supplier<MyInterface> element) {
            matches[2] = element;
        }
    }
}
