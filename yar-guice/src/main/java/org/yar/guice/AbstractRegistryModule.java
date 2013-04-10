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

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;
import org.yar.BlockingSupplier;
import org.yar.Supplier;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import static org.yar.guice.RegistrationBindingBuilderImpl.bindRegistration;

/**
 * TODO comment
 * Date: 2/28/13
 * Time: 11:36 PM
 *
 * @author Romain Gilles
 */
public abstract class AbstractRegistryModule extends AbstractModule {

    @Override
    protected final void configure() {
        doBeforeConfiguration();
        configureRegistry();
        bindProviderMethods();
    }

    void doBeforeConfiguration() {
    }

    private void bindProviderMethods() {
        for (Method method : this.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Register.class) && method.isAnnotationPresent(Provides.class)) {
                bindRegistration(binder(), Key.get(method.getGenericReturnType()));
            }
        }
    }

    /**
     * TODO
     */
    protected abstract void configureRegistry();

    protected <T> void bindRegistryListener(Matcher<Key<T>> matcher, Key<? extends RegistryListener<? super T>> key) {
        if (isBlockingSupplier(matcher)) {
            throw new IllegalArgumentException("Only simple Supplier are supported. BlockingSupplier are only available as injectable element (constructor/field/method param).");
        } else if (isSupplier(matcher)) {
            //TODO
        } else {
            bind(Key.get(GuiceWatcherRegistration.class, UniqueAnnotations.create())).toInstance(GuiceWatcherRegistration.get(matcher, key));
        }
    }

    private <T> boolean isBlockingSupplier(Matcher<Key<T>> matcher) {
        return isBlockingSupplier(Matchers.getTargetTypeLiteral(matcher));  //To change body of created methods use File | Settings | File Templates.
    }

    protected <T> boolean isSupplier(Matcher<Key<T>> matcher) {
        return isSupplier(Matchers.getTargetTypeLiteral(matcher));
    }

    protected <T> void bindRegistryListener(Matcher<Key<T>> matcher, RegistryListener<? super T> listener) {
        if (isBlockingSupplier(matcher)) {
            throw new IllegalArgumentException("Only simple Supplier are supported. BlockingSupplier are only available as injectable element (constructor/field/method param).");
        } else if (isSupplier(matcher)) {
            //TODO
        } else {
            bind(Key.get(GuiceWatcherRegistration.class, UniqueAnnotations.create())).toInstance(GuiceWatcherRegistration.get(matcher, listener));
        }
    }


//    protected <T> void bindListenerBounded(Matcher<Key<? extends T>> typeMatcher, RegistryListener<? super T> listener) {
//        if (isBlockingSupplier(typeLiteral)) {
//
//        } else if (isSupplier(typeLiteral)) {
//
//        } else {
//
//        }
//    }

    protected <T> RegistrationLinkedBindingBuilder<T> register(Key<T> key) {
        return new RegistrationBindingBuilderImpl<>(binder(), key);
    }

    protected <T> RegistrationAnnotatedBindingBuilder<T> register(TypeLiteral<T> typeLiteral) {
        return new RegistrationBindingBuilderImpl<>(binder(), typeLiteral);
    }

    // TODO cannot be scoped
    protected <T> RegistrationAnnotatedBindingBuilder<T> register(Class<T> type) {
        return new RegistrationBindingBuilderImpl<>(binder(), type);
    }

    @Override
    protected <T> RegistryLinkedBindingBuilder<T> bind(Key<T> key) {
        return getRegistryBindingBuilderFactory(key).newFrom(key);
    }

    @Override
    protected <T> RegistryAnnotatedBindingBuilder<T> bind(TypeLiteral<T> typeLiteral) {
        return getRegistryBindingBuilderFactory(typeLiteral).newFrom(typeLiteral);
    }

    @Override
    protected <T> RegistryAnnotatedBindingBuilder<T> bind(Class<T> clazz) {
        return new RegistryBindingBuilder<>(binder(), clazz);
    }

    private <T> RegistryBindingBuilderFactory getRegistryBindingBuilderFactory(Key<T> key) {
        return getRegistryBindingBuilderFactory(key.getTypeLiteral());
    }

    private <T> RegistryBindingBuilderFactory getRegistryBindingBuilderFactory(TypeLiteral<T> typeLiteral) {
        if (isBlockingSupplier(typeLiteral)) {
            return new BlockingSupplierRegistryBindingBuilderFactory();
        }
        if (isSupplier(typeLiteral)) {
            return new SupplierRegistryBindingBuilderFactory();
        }
        if (isSupportedCollectionsInterface(typeLiteral)) {
            return new CollectionsRegistryBindingBuilderFactory();
        }
        return new SimpleRegistryBindingBuilderFactory();
    }

    private <T> boolean isBlockingSupplier(TypeLiteral<T> typeLiteral) {
        return BlockingSupplier.class.isAssignableFrom(typeLiteral.getRawType());
    }

    private <T> boolean isSupplier(TypeLiteral<T> typeLiteral) {
        return Supplier.class.isAssignableFrom(typeLiteral.getRawType());
    }

    private <T> boolean isSupportedCollectionsInterface(TypeLiteral<T> typeLiteral) {
        return isClassEqualsToLiteralRowType(List.class, typeLiteral)
                || isClassEqualsToLiteralRowType(Collection.class, typeLiteral)
                || isClassEqualsToLiteralRowType(Iterable.class, typeLiteral);
    }

    private <T> boolean isClassEqualsToLiteralRowType(Class<?> type, TypeLiteral<T> typeLiteral) {
        return type.equals(typeLiteral.getRawType());
    }

    private interface RegistryBindingBuilderFactory {
        <T> RegistryAnnotatedBindingBuilder<T> newFrom(TypeLiteral<T> typeLiteral);

        <T> RegistryLinkedBindingBuilder<T> newFrom(Key<T> key);
    }

    private class SimpleRegistryBindingBuilderFactory implements RegistryBindingBuilderFactory {
        @Override
        public <T> RegistryAnnotatedBindingBuilder<T> newFrom(TypeLiteral<T> typeLiteral) {
            return new RegistryBindingBuilder<>(binder(), typeLiteral);
        }

        @Override
        public <T> RegistryLinkedBindingBuilder<T> newFrom(Key<T> key) {
            return new RegistryBindingBuilder<>(binder(), key);
        }
    }

    private class SupplierRegistryBindingBuilderFactory implements RegistryBindingBuilderFactory {
        @Override
        public <T> RegistryAnnotatedBindingBuilder<T> newFrom(TypeLiteral<T> typeLiteral) {
            return new SupplierRegistryBindingBuilder<>(binder(), typeLiteral);
        }

        @Override
        public <T> RegistryLinkedBindingBuilder<T> newFrom(Key<T> key) {
            return new SupplierRegistryBindingBuilder<>(binder(), key);
        }
    }

    private class BlockingSupplierRegistryBindingBuilderFactory implements RegistryBindingBuilderFactory {
        @Override
        public <T> RegistryAnnotatedBindingBuilder<T> newFrom(TypeLiteral<T> typeLiteral) {
            return new BlockingSupplierRegistryBindingBuilder<>(binder(), typeLiteral);
        }

        @Override
        public <T> RegistryLinkedBindingBuilder<T> newFrom(Key<T> key) {
            return new BlockingSupplierRegistryBindingBuilder<>(binder(), key);
        }
    }

    private class CollectionsRegistryBindingBuilderFactory implements RegistryBindingBuilderFactory {
        @Override
        public <T> RegistryAnnotatedBindingBuilder<T> newFrom(TypeLiteral<T> typeLiteral) {
            return new CollectionsRegistryBindingBuilder<>(binder(), typeLiteral);
        }

        @Override
        public <T> RegistryLinkedBindingBuilder<T> newFrom(Key<T> key) {
            return new CollectionsRegistryBindingBuilder<>(binder(), key);
        }
    }
}
