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
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.javabits.yar.Id;
import org.javabits.yar.Registration;
import org.javabits.yar.Registry;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static org.javabits.yar.guice.Concurrents.executeWithLog;

/**
 * TODO comment
 * This class handles the register the providing supplier into the registry.
 * Date: 2/10/13
 *
 * @author Romain Gilles
 */
@Singleton
public class RegistrationBindingHandler implements RegistrationHandler {
    private static final Logger LOG = Logger.getLogger(RegistrationBindingHandler.class.getName());

    private final Injector injector;
    private final Registry registry;
    volatile private List<RegistrationHolder> registrations;

    @Inject
    public RegistrationBindingHandler(Injector injector, Registry registry) {
        this.injector = injector;
        this.registry = registry;
    }

    @Override
    public void init() {
        registrations = registerBindings();
    }

    private List<RegistrationHolder> registerBindings() {
        List<RegistrationHolder> registrationsBuilder = newArrayList();
        for (Pair<Id, GuiceSupplier> idGuiceSupplierPair : getSuppliers()) {
            registrationsBuilder.add(putRegistrationToRegistry(idGuiceSupplierPair));
        }
        return registrationsBuilder;
    }

    //enforce load all providers before register them
    private List<Pair<Id, GuiceSupplier>> getSuppliers() {
        ImmutableList.Builder<Pair<Id, GuiceSupplier>> suppliersBuilder = ImmutableList.builder();
        for (Binding<GuiceRegistration> registrationBinding : injector.findBindingsByType(TypeLiteral.get(GuiceRegistration.class))) {
            Key<?> key = registrationBinding.getProvider().get().key();
            suppliersBuilder.add(newPair(key));
        }
        return suppliersBuilder.build();
    }

    @SuppressWarnings("unchecked")
    private StrongPair<Id, GuiceSupplier> newPair(Key<?> key) {
        Id<?> id = GuiceId.of(key);
        return new StrongPair<Id, GuiceSupplier>(id, new GuiceSupplier(injector.getProvider(key)));
    }

    @SuppressWarnings("unchecked")
    private RegistrationHolder putRegistrationToRegistry(Pair<Id, GuiceSupplier> idGuiceSupplierPair) {
        ListenableFuture<Registration<?>> future = registry.put(idGuiceSupplierPair.left(), idGuiceSupplierPair.right());
        return new RegistrationHolder(future, idGuiceSupplierPair.left());
    }

    @Override
    public List<Id<?>> registrations() {
        List<RegistrationHolder> registrations = this.registrations;
        return transform(registrations, new Function<RegistrationHolder, Id<?>>() {
            @Nullable
            @Override
            public Id<?> apply(@Nullable RegistrationHolder registrationHolder) {
                if (registrationHolder == null) {
                    throw new NullPointerException("registrationHolder");
                }
                return registrationHolder.id;
            }
        });
    }

    @Override
    public List<Id<?>> ids() {
        return registrations();
    }

    @Override
    public void clear() {
        List<RegistrationHolder> registrations = this.registrations;
        if (registrations == null) {
            return;
        }
        for (final RegistrationHolder registration : registrations) {
            executeWithLog(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    registry.remove(registration.futureRegistration.get());
                    return null;
                }
            }, registration.id, "Registration request");
        }
        registrations.clear();
    }

    private class RegistrationHolder {
        private final ListenableFuture<Registration<?>> futureRegistration;
        private final Id<?> id;

        private RegistrationHolder(ListenableFuture<Registration<?>> futureRegistration, Id<?> id) {
            this.futureRegistration = futureRegistration;
            this.id = id;
        }
    }
}
