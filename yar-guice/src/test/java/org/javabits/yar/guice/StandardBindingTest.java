/*
 * Copyright (c) 5/29/15 11:04 AM Romain Gilles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javabits.yar.guice;

import com.google.inject.*;
import com.google.inject.Module;
import org.javabits.yar.BlockingSupplierRegistry;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Singleton;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.javabits.yar.guice.RegistryModuleBindTest.newBlockingSupplierRegistry;
import static org.javabits.yar.guice.YarGuices.newRegistryDeclarationModule;

/**
 * This test case validate the usage of standard bindings with Yar modules.
 */
public class StandardBindingTest {

    @Test
    public void testBindSimple() {
        Injector injector = newInjector(new AbstractRegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(MyInterface.class).to(MyInterfaceImpl.class);
            }
        });

        assertThat(injector.getInstance(MyInterface.class), is(not(nullValue())));
        assertThat(injector.getInstance(MyInterface.class), is(not(injector.getInstance(MyInterface.class))));
    }
    @Test
    public void testBindSingletonBinding() {
        Injector injector = newInjector(new AbstractRegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(MyInterface.class).to(MyInterfaceImpl.class).in(Singleton.class);
            }
        });

        assertThat(injector.getInstance(MyInterface.class), is(not(nullValue())));
        assertThat(injector.getInstance(MyInterface.class), is(injector.getInstance(MyInterface.class)));
    }

    @Test
    public void testBindSingletonAnnotationBinding() {
        Injector injector = newInjector(new AbstractRegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(MyInterface.class).to(MySingleton.class);
            }
        });

        assertThat(injector.getInstance(MyInterface.class), is(not(nullValue())));
        assertThat(injector.getInstance(MyInterface.class), is(injector.getInstance(MyInterface.class)));
    }

    @Test
    public void testBindClassWithSingletonDepBasedOnJIT() {
        Injector injector = newInjector(new AbstractRegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(MyInterfaceSingleton.class).to(MyInterfaceWithDep.class);
            }
        });

        assertThat(injector.getInstance(MyInterfaceSingleton.class).getSingleton(), is(injector.getInstance(MyInterfaceSingleton.class).getSingleton()));
    }

    @Test
    public void testBindSingletonImpl() {
        Injector injector = newInjector(new AbstractRegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(MySingleton.class);
            }
        });

        assertThat(injector.getInstance(MySingleton.class), is(injector.getInstance(MySingleton.class)));
    }

    @Test
    public void testBindSingletonBackgroundCreation() {
        MySingletonCreation.created.set(FALSE);
        Injector injector = newInjector(new AbstractRegistryModule() {
            @Override
            protected void configureRegistry() {
                bind(MySingletonCreation.class);
            }
        });
        injector.getBindings();
        assertThat(MySingletonCreation.created.get(), is(TRUE));
    }

    @Test
    public void testBindSingletonBackgroundCreationStandard() {
        MySingletonCreation.created.set(FALSE);
        Injector injector = Guice.createInjector(Stage.PRODUCTION, new AbstractModule() {
            @Override
            protected void configure() {
                bind(MySingletonCreation.class);
            }
        });
        injector.getBindings();
        assertThat(MySingletonCreation.created.get(), is(TRUE));
    }

    @Test
    public void testBindSingletonBackgroundCreationJITStandard() {
        MySingletonCreation.created.set(FALSE);
        Injector injector = Guice.createInjector(Stage.PRODUCTION, new AbstractModule() {
            @Override
            protected void configure() {
            }
        });
        assertThat(injector.getInstance(MySingletonCreation.class), is(injector.getInstance(MySingletonCreation.class)));
        assertThat(MySingletonCreation.created.get(), is(TRUE));
    }


    private Injector newInjector(AbstractRegistryModule registryModule) {
        final BlockingSupplierRegistry registry = newBlockingSupplierRegistry();
        Module module = newRegistryDeclarationModule(registry);

        return Guice.createInjector(Stage.PRODUCTION, module, registryModule, new RegistryModule() {
            @Override
            protected void configureRegistry() {
            }
        });
    }

    interface MyInterfaceSingleton {
        MyInterface getSingleton();
    }

    @Singleton
    static class MySingleton implements MyInterface {}


    static class MyInterfaceWithDep implements MyInterfaceSingleton {
        private final MySingleton singleton;

        @Inject
        MyInterfaceWithDep(MySingleton singleton) {
            this.singleton = singleton;
        }

        public MySingleton getSingleton() {
            return singleton;
        }
    }

    @Singleton
    static class MySingletonCreation {
        static ThreadLocal<Boolean> created = ThreadLocal.withInitial(() -> FALSE);

        public MySingletonCreation() {
            created.set(TRUE);
        }
    }

}
