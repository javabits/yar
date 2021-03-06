package org.javabits.yar.guice;

import com.google.inject.*;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

import static java.util.Objects.requireNonNull;

/**
 * TODO comment
 * Date: 2/8/13
 * Time: 10:25 PM
 *
 * @author Romain Gilles
 * @since 1.0
 */
abstract class RegistryAnnotatedBindingBuilderImpl<T> implements RegistryAnnotatedBindingBuilder<T> {


    private final Binder binder;
    private Key<T> key;
    private LinkedBindingBuilder<T> linkedBindingBuilder;
    //Lax type binding represent qualified versus non qualified types
    private boolean laxTypeBinding = false;
    private AnnotatedBindingBuilder<T> annotatedBindingBuilder;


    RegistryAnnotatedBindingBuilderImpl(Binder binder, Key<T> key, LinkedBindingBuilder<T> bindingBuilder) {
        this.binder = requireNonNull(binder, "binder");
        this.key = requireNonNull(key, "key");
        laxTypeBinding = false;
        annotatedBindingBuilder = null;
        linkedBindingBuilder = bindingBuilder;
    }

    RegistryAnnotatedBindingBuilderImpl(Binder binder, TypeLiteral<T> typeLiteral, AnnotatedBindingBuilder<T> bindingBuilder) {
        this.binder = requireNonNull(binder, "binder");
        key = Key.get(typeLiteral);
        laxTypeBinding = true;
        linkedBindingBuilder = annotatedBindingBuilder = bindingBuilder;
    }

    RegistryAnnotatedBindingBuilderImpl(Binder binder, Class<T> clazz, AnnotatedBindingBuilder<T> bindingBuilder) {
        this.binder = requireNonNull(binder, "binder");
        key = Key.get(clazz);
        laxTypeBinding = true;
        linkedBindingBuilder = annotatedBindingBuilder = bindingBuilder;
    }

    Key<T> key() {
        return key;
    }

    @Override
    public RegistryLinkedBindingBuilder<T> annotatedWith(Class<? extends Annotation> annotationType) {
        key = Key.get(key.getTypeLiteral(), annotationType);
        laxTypeBinding = false;
        linkedBindingBuilder = annotatedBindingBuilder.annotatedWith(annotationType);
        return this;
    }

    @Override
    public RegistryLinkedBindingBuilder<T> annotatedWith(Annotation annotation) {
        key = Key.get(key.getTypeLiteral(), annotation);
        laxTypeBinding = false;
        linkedBindingBuilder = annotatedBindingBuilder.annotatedWith(annotation);
        return this;
    }

    @Override
    public RegistryBindingBuilder toRegistry() {
        Iterable<RegistryProvider<?>> registryProviders = doToRegistry();
        return new RegistryBindingBuilderImpl<>(registryProviders);
    }

    abstract Iterable<RegistryProvider<?>> doToRegistry();

    @Override
    public ScopedBindingBuilder to(Class<? extends T> implementation) {
        linkedBindingBuilder().to(implementation);
        return this;
    }

    @Override
    public ScopedBindingBuilder to(TypeLiteral<? extends T> implementation) {
        linkedBindingBuilder().to(implementation);
        return this;
    }

    @Override
    public ScopedBindingBuilder to(Key<? extends T> targetKey) {
        linkedBindingBuilder().to(targetKey);
        return this;
    }

    @Override
    public void toInstance(T instance) {
        linkedBindingBuilder().toInstance(instance);
    }

    @Override
    public ScopedBindingBuilder toProvider(Provider<? extends T> provider) {
        linkedBindingBuilder().toProvider(provider);
        return this;
    }

    @Override
    public ScopedBindingBuilder toProvider(javax.inject.Provider<? extends T> provider) {
        linkedBindingBuilder().toProvider(provider);
        return this;
    }

    @Override
    public ScopedBindingBuilder toProvider(Class<? extends javax.inject.Provider<? extends T>> providerType) {
        linkedBindingBuilder().toProvider(providerType);
        return this;
    }

    @Override
    public ScopedBindingBuilder toProvider(TypeLiteral<? extends javax.inject.Provider<? extends T>> providerType) {
        linkedBindingBuilder().toProvider(providerType);
        return this;
    }

    @Override
    public ScopedBindingBuilder toProvider(Key<? extends javax.inject.Provider<? extends T>> providerKey) {
        linkedBindingBuilder().toProvider(providerKey);
        return this;
    }

    @Override
    public <S extends T> ScopedBindingBuilder toConstructor(Constructor<S> constructor) {
        linkedBindingBuilder().toConstructor(constructor);
        return this;
    }

    @Override
    public <S extends T> ScopedBindingBuilder toConstructor(Constructor<S> constructor, TypeLiteral<? extends S> type) {
        linkedBindingBuilder().toConstructor(constructor, type);
        return this;
    }

    @Override
    public void in(Class<? extends Annotation> scopeAnnotation) {
        linkedBindingBuilder().in(scopeAnnotation);
    }

    @Override
    public void in(Scope scope) {
        linkedBindingBuilder().in(scope);
    }

    @Override
    public void asEagerSingleton() {
        linkedBindingBuilder().asEagerSingleton();
    }

    LinkedBindingBuilder<T> linkedBindingBuilder() {
        return requireNonNull(linkedBindingBuilder, "linkedBindingBuilder");
    }

    Binder binder() {
        return binder;
    }


    boolean isLaxTypeBinding() {
        return laxTypeBinding;
    }

}
