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

package org.yaor.guice;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.yaor.IdMatcher;
import org.yaor.Registry;
import org.yaor.Watcher;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;

/**
 * TODO comment
 * Date: 4/3/13
 * Time: 9:10 PM
 *
 * @author Romain Gilles
 */
@RunWith(MockitoJUnitRunner.class)
public class RegistryListenerBindingHandlerTest {
    @Mock
    IdMatcher idMatcher;
    @Mock
    Watcher watcher;
    @Mock
    GuiceWatcherRegistration guiceWatcherRegistration;
    @Mock
    Provider<GuiceWatcherRegistration> guiceWatcherRegistrationProvider;
    @Mock
    Injector injector;
    @Mock
    Registry registry = null;
    @Mock
    Binding<GuiceWatcherRegistration> guiceWatcherRegistrationBinding;

    RegistryListenerBindingHandler registryListenerBindingHandler;

    @Test
    public void testConstructOnEmptyListOfWatcherRegistration() throws Exception {
        List<Binding<GuiceWatcherRegistration>> bindings = newEmptyGuiceWatcherRegistrations();
        when(injector.findBindingsByType(TypeLiteral.get(GuiceWatcherRegistration.class))).thenReturn(bindings);
        registryListenerBindingHandler = new RegistryListenerBindingHandler(injector, registry);
    }

    private List<Binding<GuiceWatcherRegistration>> newEmptyGuiceWatcherRegistrations() {
        return emptyList();
    }

    @Test
    public void testConstructListOfWatcherRegistrationNotEmpty() throws Exception {
        List<Binding<GuiceWatcherRegistration>> bindings = newNotEmptyGuiceWatcherRegistrations();
        when(injector.findBindingsByType(TypeLiteral.get(GuiceWatcherRegistration.class))).thenReturn(bindings);
        registryListenerBindingHandler = new RegistryListenerBindingHandler(injector, registry);
    }

    private List<Binding<GuiceWatcherRegistration>> newNotEmptyGuiceWatcherRegistrations() {
        when(guiceWatcherRegistrationBinding.getProvider()).thenReturn(guiceWatcherRegistrationProvider);
        when(guiceWatcherRegistrationProvider.get()).thenReturn(guiceWatcherRegistration);
        when(guiceWatcherRegistration.matcher()).thenReturn(idMatcher);
        when(guiceWatcherRegistration.watcher()).thenReturn(watcher);
        return singletonList(guiceWatcherRegistrationBinding);
    }
}
