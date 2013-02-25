package org.yar.guice;

import com.google.inject.*;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import org.yar.Registry;
import org.yar.Supplier;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

import static java.util.Objects.requireNonNull;
import static org.yar.guice.RegistrationBindingBuilderImpl.checkInterface;

/**
 * TODO comment
 * Date: 2/8/13
 * Time: 10:25 PM
 *
 * @author Romain Gilles
 * @since 1.0
 */
public class RegistryBindingBuilder<T> implements RegistryAnnotatedBindingBuilder<T> {


    private Key<T> key;
    private AnnotatedBindingBuilder<T> annotatedBindingBuilder;
    private LinkedBindingBuilder<T> linkedBindingBuilder;


    public RegistryBindingBuilder(Binder binder, Key<T> key) {
        this.key = checkInterface(key);
        linkedBindingBuilder = binder.bind(key);
    }

    public RegistryBindingBuilder(Binder binder, TypeLiteral<T> typeLiteral) {
        key = Key.get(checkInterface(typeLiteral));
        linkedBindingBuilder = annotatedBindingBuilder = binder.bind(typeLiteral);
    }

    public RegistryBindingBuilder(Binder binder, Class<T> clazz) {
        key = Key.get(checkInterface(clazz));
        linkedBindingBuilder = annotatedBindingBuilder = binder.bind(clazz);
    }

    @Override
    public RegistryLinkedBindingBuilder<T> annotatedWith(Class<? extends Annotation> annotationType) {
        key = Key.get(key.getTypeLiteral(), annotationType);
        annotatedBindingBuilder.annotatedWith(annotationType);
        return this;
    }

    @Override
    public RegistryLinkedBindingBuilder<T> annotatedWith(Annotation annotation) {
        key = Key.get(key.getTypeLiteral(), annotation);
        annotatedBindingBuilder.annotatedWith(annotation);
        return this;
    }

    @Override
    public ScopedBindingBuilder toRegistry() {
        return linkedBindingBuilder.toProvider(new RegistryProvider<>(key));
    }

    @Override
    public ScopedBindingBuilder to(Class<? extends T> implementation) {
        linkedBindingBuilder.to(implementation);
        return this;
    }

    @Override
    public ScopedBindingBuilder to(TypeLiteral<? extends T> implementation) {
        linkedBindingBuilder.to(implementation);
        return this;
    }

    @Override
    public ScopedBindingBuilder to(Key<? extends T> targetKey) {
        linkedBindingBuilder.to(targetKey);
        return this;
    }

    @Override
    public void toInstance(T instance) {
        linkedBindingBuilder.toInstance(instance);
    }

    @Override
    public ScopedBindingBuilder toProvider(Provider<? extends T> provider) {
        linkedBindingBuilder.toProvider(provider);
        return this;
    }

    @Override
    public ScopedBindingBuilder toProvider(Class<? extends javax.inject.Provider<? extends T>> providerType) {
        linkedBindingBuilder.toProvider(providerType);
        return this;
    }

    @Override
    public ScopedBindingBuilder toProvider(TypeLiteral<? extends javax.inject.Provider<? extends T>> providerType) {
        linkedBindingBuilder.toProvider(providerType);
        return this;
    }

    @Override
    public ScopedBindingBuilder toProvider(Key<? extends javax.inject.Provider<? extends T>> providerKey) {
        linkedBindingBuilder.toProvider(providerKey);
        return this;
    }

    @Override
    public <S extends T> ScopedBindingBuilder toConstructor(Constructor<S> constructor) {
        linkedBindingBuilder.toConstructor(constructor);
        return this;
    }

    @Override
    public <S extends T> ScopedBindingBuilder toConstructor(Constructor<S> constructor, TypeLiteral<? extends S> type) {
        linkedBindingBuilder.toConstructor(constructor, type);
        return this;
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

    //TODO introduce dynamic management through Watcher and regarding the ?scope? maybe
    private static class RegistryProvider<T> implements Provider<T> {

        private final Key<T> key;
        private Registry registry;

        private RegistryProvider(Key<T> key) {
            this.key = key;
        }

        @Override
        public T get() {
            Supplier<T> supplier = getSupplier();
            return supplier.get();
        }

        private Supplier<T> getSupplier() {
            Supplier<T> supplier = registry().get(GuiceKey.of(key));
            return requireNonNull(supplier, "supplier");
        }

        private Registry registry() {
            return requireNonNull(registry, "registry");
        }

        @Inject
        public void setRegistry(Registry registry) {
            this.registry = registry;
        }
    }
}
