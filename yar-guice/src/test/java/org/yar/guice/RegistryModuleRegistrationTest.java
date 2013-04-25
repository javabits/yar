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
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.junit.Test;
import org.yar.Id;
import org.yar.Registry;
import org.yar.Supplier;

import javax.inject.Provider;
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * TODO comment
 * Date: 2/8/13
 * Time: 5:47 PM
 *
 * @author Romain Gilles
 */
public class RegistryModuleRegistrationTest {

    private static final String ANNOTATION_NAME = "name";
    private static final Named NAMED_ANNOTATION = Names.named(ANNOTATION_NAME);

    @Test
    public void testConfigureRegisterToClass() {
        Injector injector = createInjector(new RegistryModule() {
            @Override
            protected void configureRegistry() {
                register(MyServiceInterface.class).to(MyService.class);
            }
        });
        checkRegisteredService(injector, Key.get(MyServiceInterface.class));
    }

    private Injector createInjector(RegistryModule registryModule) {
        Injector injector = Guice.createInjector(Stage.PRODUCTION, new AbstractModule() {
            @Override
            protected void configure() {
                bind(Registry.class).to(SimpleRegistry.class).in(Singleton.class);
            }
        }, registryModule);
        injector.getInstance(RegistrationHandler.class).init();
        injector.getInstance(RegistryListenerHandler.class).init();
        return injector;
    }

    private void checkRegisteredService(Injector injector, Key<MyServiceInterface> key) {
        Id<MyServiceInterface> registryId = GuiceId.of(key);
        Registry registry = injector.getInstance(Registry.class);
        Supplier<MyServiceInterface> actualServiceSupplier = registry.get(registryId);
        assertThat(actualServiceSupplier, is(not(nullValue())));
        assertThat(actualServiceSupplier.get(), is(not(nullValue())));
    }

    @Test
    public void testConfigureRegisterToAnnotatedClass() {
        Injector injector = createInjector(new RegistryModule() {
            @Override
            protected void configureRegistry() {
                register(MyServiceInterface.class).annotatedWith(MyAnnotation.class).to(MyService.class);
            }

        });
        checkRegisteredService(injector, Key.get(MyServiceInterface.class, MyAnnotation.class));
    }

    @Test
    public void testConfigureRegisterToProvider() {
        Injector injector = createInjector(new RegistryModule() {
            @Override
            protected void configureRegistry() {
                register(MyServiceInterface.class).toProvider(MyServiceProvider.class);
            }
        });
        checkRegisteredService(injector, Key.get(MyServiceInterface.class));
    }

    @Test
    public void testConfigureRegisterToNamedKey() {
        Injector injector = createInjector(new RegistryModule() {
            @Override
            protected void configureRegistry() {
                Key<MyServiceInterface> key = Key.get(MyServiceInterface.class, NAMED_ANNOTATION);
                bind(key).to(MyService.class);
                register(MyServiceInterface.class).to(key);

            }
        });
        checkRegisteredService(injector, Key.get(MyServiceInterface.class));
    }

    @Test
    public void testConfigureRegisterToCustumAnnotatedKey() {
        Injector injector = createInjector(new RegistryModule() {
            @Override
            protected void configureRegistry() {
                Key<MyServiceInterface> key = Key.get(MyServiceInterface.class, MyAnnotation.class);
                bind(key).to(MyService.class);
                register(MyServiceInterface.class).to(key);
            }
        });
        checkRegisteredService(injector, Key.get(MyServiceInterface.class));
    }

    @Test
    public void testConfigureRegisterToMethodProvider() {
        Injector injector = createInjector(new RegistryModule() {
            @Override
            protected void configureRegistry() {
            }

            @Provides
            @Register
            public MyServiceInterface myMethodServiceProvider() {
                return new MyService();
            }
        });
        checkRegisteredService(injector, Key.get(MyServiceInterface.class));
    }

    //    @Test
    public void testConfigureBindFromRegistry() {
        Injector injector = createInjector(new RegistryModule() {
            @Override
            protected void configureRegistry() {
            }

            @Provides
            @Register
            public MyServiceInterface myMethodServiceProvider(Registry registry) {
                return new MyService();
            }
        });
    }

    interface MyServiceInterface {
    }

    @Target({TYPE})
    @Retention(RUNTIME)
    @Qualifier
    static @interface MyAnnotation {
    }

    @MyAnnotation
    static class MyService implements MyServiceInterface {
    }

    static class MyServiceProvider implements Provider<MyService> {
        @Override
        public MyService get() {
            return new MyService();
        }
    }
}
