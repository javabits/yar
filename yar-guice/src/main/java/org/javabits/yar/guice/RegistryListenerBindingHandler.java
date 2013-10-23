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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import org.javabits.yar.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

/**
 * This class is responsible to lookup all the watcher registration request into
 * the injector and satisfied them by publishing them into the registry.
 * This class also provide a cleanup method.
 *
 * Date: 3/14/13
 *
 * @author Romain Gilles
 */
@Singleton
public class RegistryListenerBindingHandler implements RegistryListenerHandler {
    private static final Logger LOG = Logger.getLogger(RegistryListenerBindingHandler.class.getName());

    private final Injector injector;
    private final Registry registry;
    //we keep a strong reference on the watcher to avoid it to be garbage collected
    //therefore the lifecycle to this watcher is at least associated to the lifecycle of owning injector
    volatile private List<ListenerRegistration> listenerRegistrations;


    @Inject
    public RegistryListenerBindingHandler(Injector injector, Registry registry) {
        this.injector = injector;
        this.registry = registry;

    }

    @Override
    public void init() {
        this.listenerRegistrations = addListenerToRegistry();
    }

    //enforce creation of all watcher before register it
    @SuppressWarnings("unchecked")
    private List<ListenerRegistration> addListenerToRegistry() {
        List<ListenerRegistration> registrationsBuilder = newArrayList();
        for (Pair<IdMatcher, Watcher> guiceWatcherRegistration : getRegisteredWatchers()) {
            Watcher watcher = guiceWatcherRegistration.right();
            IdMatcher idMatcher = guiceWatcherRegistration.left();
            Registration registration = registry.addWatcher(idMatcher, watcher);
            ListenerRegistration listenerRegistration = new ListenerRegistration(registration, idMatcher, watcher);
            registrationsBuilder.add(listenerRegistration);
        }
        return registrationsBuilder;
    }

    private List<Pair<IdMatcher, Watcher>> getRegisteredWatchers() {
        ImmutableList.Builder<Pair<IdMatcher, Watcher>> registrationsBuilder = ImmutableList.builder();
        List<Binding<GuiceWatcherRegistration>> guiceWatcherRegistrationBindings = injector.findBindingsByType(TypeLiteral.get(GuiceWatcherRegistration.class));
        for (Binding<GuiceWatcherRegistration> watcherRegistrationBinding : guiceWatcherRegistrationBindings) {
            GuiceWatcherRegistration guiceWatcherRegistration = watcherRegistrationBinding.getProvider().get();
            registrationsBuilder.add(new StrongPair<>(guiceWatcherRegistration.matcher(), guiceWatcherRegistration.watcher()));
        }
        return registrationsBuilder.build();
    }

    @Override
    public List<Id<?>> listenerIds() {
        List<ListenerRegistration> listenerRegistrations = this.listenerRegistrations;
        return Lists.transform(listenerRegistrations, new Function<ListenerRegistration, Id<?>>() {
            @Nullable
            @Override
            public Id<?> apply(@Nullable ListenerRegistration listenerRegistration) {
                checkNotNull(listenerRegistration, "listenerRegistration");
                IdMatcher<?> idMatcher = checkNotNull(listenerRegistration.idMatcher, "idMatcher");
                return idMatcher.id();
            }
        });
    }

    @Override
    public List<Id<?>> ids() {
        return listenerIds();
    }

    @Override
    public void clear() {
        List<ListenerRegistration> listenerRegistrations = this.listenerRegistrations;
        if (listenerRegistrations == null) {
            return;
        }
        for (ListenerRegistration listenerRegistration : listenerRegistrations) {
            Registration watcherRegistration = listenerRegistration.registration;
            registry.removeWatcher(watcherRegistration);
        }
        listenerRegistrations.clear();
    }

    private class ListenerRegistration {
        private final Registration registration;
        private final IdMatcher<?> idMatcher;
        @SuppressWarnings("unused")
        //Strong ref is required to avoid gc.
        private final Watcher<?> watcher;

        private ListenerRegistration(Registration registration, IdMatcher<?> idMatcher, Watcher<?> watcher) {
            this.registration = registration;
            this.idMatcher = idMatcher;
            this.watcher = watcher;
        }
    }
}
