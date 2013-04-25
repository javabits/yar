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
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

import static java.util.Objects.requireNonNull;

/**
 * TODO comment
 * Date: 2/10/13
 * Time: 10:13 AM
 *
 * @author Romain Gilles
 */
class RegistrationBindingBuilderImpl<T> implements RegistrationAnnotatedBindingBuilder<T> {

    private final Binder binder;
    private Key<T> key;

    private AnnotatedBindingBuilder<T> annotatedBindingBuilder;
    private LinkedBindingBuilder<T> linkedBindingBuilder;

    RegistrationBindingBuilderImpl(Binder binder, Class<T> type) {
        this.binder = requireNonNull(binder, "binder");
        key = Key.get(type);
        linkedBindingBuilder = annotatedBindingBuilder = binder.bind(type);
    }

    RegistrationBindingBuilderImpl(Binder binder, TypeLiteral<T> typeLiteral) {
        this.binder = requireNonNull(binder, "binder");
        key = Key.get(typeLiteral);
        linkedBindingBuilder = annotatedBindingBuilder = binder.bind(typeLiteral);
    }

    RegistrationBindingBuilderImpl(Binder binder, Key<T> key) {
        this.binder = requireNonNull(binder, "binder");
        requireNonNull(key, "key");
        this.key = key;
        linkedBindingBuilder = binder.bind(key);
    }


    @Override
    public RegistrationLinkedBindingBuilder<T> annotatedWith(Class<? extends Annotation> annotationType) {
        key = Key.get(key.getTypeLiteral(), annotationType);
        linkedBindingBuilder = annotatedBindingBuilder.annotatedWith(annotationType);
        return this;
    }

    @Override
    public RegistrationLinkedBindingBuilder<T> annotatedWith(Annotation annotation) {
        key = Key.get(key.getTypeLiteral(), annotation);
        linkedBindingBuilder = annotatedBindingBuilder.annotatedWith(annotation);
        return this;
    }

    private void bindRegistration() {
        bindRegistration(binder, key);
    }

    static void bindRegistration(Binder binder, Key<?> key) {
        binder.bind(Key.get(GuiceRegistration.class, UniqueAnnotations.create())).toInstance(GuiceRegistration.get(key));
    }

    @Override
    public ScopedBindingBuilder to(Class<? extends T> implementation) {
        bindRegistration();
        return linkedBindingBuilder.to(implementation);
    }

    @Override
    public ScopedBindingBuilder to(TypeLiteral<? extends T> implementation) {
        bindRegistration();
        return linkedBindingBuilder.to(implementation);
    }

    @Override
    public ScopedBindingBuilder to(Key<? extends T> targetKey) {
        bindRegistration();
        return linkedBindingBuilder.to(targetKey);
    }

    @Override
    public void toInstance(T instance) {
        bindRegistration();
        linkedBindingBuilder.toInstance(instance);
    }

    @Override
    public ScopedBindingBuilder toProvider(Provider<? extends T> provider) {
        bindRegistration();
        return linkedBindingBuilder.toProvider(provider);
    }

    @Override
    public ScopedBindingBuilder toProvider(Class<? extends javax.inject.Provider<? extends T>> providerType) {
        bindRegistration();
        return linkedBindingBuilder.toProvider(providerType);
    }

    @Override
    public ScopedBindingBuilder toProvider(TypeLiteral<? extends javax.inject.Provider<? extends T>> providerType) {
        bindRegistration();
        return linkedBindingBuilder.toProvider(providerType);
    }

    @Override
    public ScopedBindingBuilder toProvider(Key<? extends javax.inject.Provider<? extends T>> providerKey) {
        bindRegistration();
        return linkedBindingBuilder.toProvider(providerKey);
    }

    @Override
    public <S extends T> ScopedBindingBuilder toConstructor(Constructor<S> constructor) {
        bindRegistration();
        return linkedBindingBuilder.toConstructor(constructor);
    }

    @Override
    public <S extends T> ScopedBindingBuilder toConstructor(Constructor<S> constructor, TypeLiteral<? extends S> type) {
        bindRegistration();
        return linkedBindingBuilder.toConstructor(constructor, type);
    }

    @Override
    public void in(Class<? extends Annotation> scopeAnnotation) {
        linkedBindingBuilder.in(scopeAnnotation);
    }

    @Override
    public void in(Scope scope) {
        linkedBindingBuilder.in(scope);
    }

    @Override
    public void asEagerSingleton() {
        linkedBindingBuilder.asEagerSingleton();
    }
}
