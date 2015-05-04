package org.javabits.yar.guice;

import com.google.common.annotations.Beta;
import com.google.inject.*;
import com.google.inject.binder.AnnotatedConstantBindingBuilder;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.Message;
import com.google.inject.spi.TypeConverter;
import com.google.inject.spi.TypeListener;
import org.aopalliance.intercept.MethodInterceptor;
import org.javabits.yar.BlockingSupplier;
import org.javabits.yar.Supplier;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * This class is responsible to implement the {@link RegistryBinder} interface
 * and forward the non overloaded methods of {@code Binder} to a delegate.
 */
class RegistryBinderImpl implements RegistryBinder {
    private final Binder binder;

    public RegistryBinderImpl(Binder binder) {
        this.binder = binder;
    }

    @Beta
    @Override
    public <T> void bindRegistryListener(Matcher<Key<T>> matcher, Key<? extends RegistryListener<? super T>> key) {
        if (isBlockingSupplier(matcher)) {
            throw new IllegalArgumentException("Only simple Supplier are supported. BlockingSupplier are only available as injectable element (constructor/field/method param).");
        } else if (isSupplier(matcher)) {
            throw new IllegalArgumentException("Only simple Supplier are supported. Supplier are only available as injectable element (constructor/field/method param).");
        } else {
            binder.bind(Key.get(GuiceWatcherRegistration.class, UniqueAnnotations.create())).toInstance(GuiceWatcherRegistration.get(matcher, key));
        }
    }

    private  <T> boolean isBlockingSupplier(Matcher<Key<T>> matcher) {
        return isBlockingSupplier(Matchers.getTargetTypeLiteral(matcher));  //To change body of created methods use File | Settings | File Templates.
    }

    private  <T> boolean isSupplier(Matcher<Key<T>> matcher) {
        return isSupplier(Matchers.getTargetTypeLiteral(matcher));
    }

    @Beta
    @Override
    public <T> void bindRegistryListener(Matcher<Key<T>> matcher, RegistryListener<? super T> listener) {
        if (isBlockingSupplier(matcher)) {
            throw new IllegalArgumentException("Only simple Type are supported. BlockingSupplier are only available as injectable element (constructor/field/method param).");
        } else if (isSupplier(matcher)) {
            throw new IllegalArgumentException("Only simple Supplier are supported. Supplier are only available as injectable element (constructor/field/method param).");
        } else {
            binder.bind(Key.get(GuiceWatcherRegistration.class, UniqueAnnotations.create())).toInstance(GuiceWatcherRegistration.get(matcher, listener));
        }
        binder.requestInjection(listener);
    }

    @Override
    public <T> RegistrationLinkedBindingBuilder<T> register(Key<T> key) {
        return new RegistrationBindingBuilderImpl<>(binder, key);
    }

    @Override
    public <T> RegistrationAnnotatedBindingBuilder<T> register(TypeLiteral<T> typeLiteral) {
        return new RegistrationBindingBuilderImpl<>(binder, typeLiteral);
    }
    // TODO cannot be scoped
    @Override
    public <T> RegistrationAnnotatedBindingBuilder<T> register(Class<T> type) {
        return new RegistrationBindingBuilderImpl<>(binder, type);
    }

    @Override
    public <T> RegistryLinkedBindingBuilder<T> bind(Key<T> key) {
        return getRegistryBindingBuilderFactory(key).newFrom(key);
    }

    @Override
    public <T> RegistryAnnotatedBindingBuilder<T> bind(TypeLiteral<T> typeLiteral) {
        return getRegistryBindingBuilderFactory(typeLiteral).newFrom(typeLiteral);
    }

    @Override
    public <T> RegistryAnnotatedBindingBuilder<T> bind(Class<T> clazz) {
        return new DefaultRegistryAnnotatedBindingBuilderImpl<>(binder, clazz);
    }

    //Guice Binder delegation ------------------------------------------------

    @Override
    public void bindInterceptor(Matcher<? super Class<?>> classMatcher, Matcher<? super Method> methodMatcher, MethodInterceptor... interceptors) {
        binder.bindInterceptor(classMatcher, methodMatcher, interceptors);
    }

    @Override
    public void bindScope(Class<? extends Annotation> annotationType, Scope scope) {
        binder.bindScope(annotationType, scope);
    }

    @Override
    public AnnotatedConstantBindingBuilder bindConstant() {
        return binder.bindConstant();
    }

    @Override
    public <T> void requestInjection(TypeLiteral<T> type, T instance) {
        binder.requestInjection(type, instance);
    }

    @Override
    public void requestInjection(Object instance) {
        binder.requestInjection(instance);
    }

    @Override
    public void requestStaticInjection(Class<?>... types) {
        binder.requestStaticInjection(types);
    }

    @Override
    public void install(Module module) {
        binder.install(module);
    }

    @Override
    public Stage currentStage() {
        return binder.currentStage();
    }

    @Override
    public void addError(String message, Object... arguments) {
        binder.addError(message, arguments);
    }

    @Override
    public void addError(Throwable t) {
        binder.addError(t);
    }

    @Override
    public void addError(Message message) {
        binder.addError(message);
    }

    @Override
    public <T> Provider<T> getProvider(Key<T> key) {
        return binder.getProvider(key);
    }

    @Override
    public <T> Provider<T> getProvider(Class<T> type) {
        return binder.getProvider(type);
    }

    @Override
    public <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> typeLiteral) {
        return binder.getMembersInjector(typeLiteral);
    }

    @Override
    public <T> MembersInjector<T> getMembersInjector(Class<T> type) {
        return binder.getMembersInjector(type);
    }

    @Override
    public void convertToTypes(Matcher<? super TypeLiteral<?>> typeMatcher, TypeConverter converter) {
        binder.convertToTypes(typeMatcher, converter);
    }

    @Override
    public void bindListener(Matcher<? super TypeLiteral<?>> typeMatcher, TypeListener listener) {
        binder.bindListener(typeMatcher, listener);
    }

    @Override
    public Binder withSource(Object source) {
        return binder.withSource(source);
    }

    @Override
    public Binder skipSources(Class... classesToSkip) {
        return binder.skipSources(classesToSkip);
    }

    @Override
    public PrivateBinder newPrivateBinder() {
        return binder.newPrivateBinder();
    }

    @Override
    public void requireExplicitBindings() {
        binder.requireExplicitBindings();
    }

    @Override
    public void disableCircularProxies() {
        binder.disableCircularProxies();
    }

    //------------------------------------------------------------------------


    private  <T> RegistryBindingBuilderFactory getRegistryBindingBuilderFactory(Key<T> key) {
        return getRegistryBindingBuilderFactory(key.getTypeLiteral());
    }

    private  <T> RegistryBindingBuilderFactory getRegistryBindingBuilderFactory(TypeLiteral<T> typeLiteral) {
        if (isBlockingSupplier(typeLiteral) || isSupplier(typeLiteral)) {
            return new BlockingSupplierRegistryBindingBuilderFactory();
        } else if (isSupportedCollectionsInterface(typeLiteral)) {
            return new CollectionsRegistryBindingBuilderFactory();
        } else {
            return new SimpleRegistryBindingBuilderFactory();
        }
    }

    private  <T> boolean isBlockingSupplier(TypeLiteral<T> typeLiteral) {
        return BlockingSupplier.class.isAssignableFrom(typeLiteral.getRawType());
    }

    private  <T> boolean isSupplier(TypeLiteral<T> typeLiteral) {
        return Supplier.class.isAssignableFrom(typeLiteral.getRawType())
                || com.google.common.base.Supplier.class.isAssignableFrom(typeLiteral.getRawType());
    }

    private  <T> boolean isSupportedCollectionsInterface(TypeLiteral<T> typeLiteral) {
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
            return new DefaultRegistryAnnotatedBindingBuilderImpl<>(binder, typeLiteral);
        }

        @Override
        public <T> RegistryLinkedBindingBuilder<T> newFrom(Key<T> key) {
            return new DefaultRegistryAnnotatedBindingBuilderImpl<>(binder, key);
        }
    }

    private class BlockingSupplierRegistryBindingBuilderFactory implements RegistryBindingBuilderFactory {
        @Override
        public <T> RegistryAnnotatedBindingBuilder<T> newFrom(TypeLiteral<T> typeLiteral) {
            return new BlockingSupplierRegistryAnnotatedBindingBuilderImpl<>(binder, typeLiteral);
        }

        @Override
        public <T> RegistryLinkedBindingBuilder<T> newFrom(Key<T> key) {
            return new BlockingSupplierRegistryAnnotatedBindingBuilderImpl<>(binder, key);
        }
    }

    private class CollectionsRegistryBindingBuilderFactory implements RegistryBindingBuilderFactory {
        @Override
        public <T> RegistryAnnotatedBindingBuilder<T> newFrom(TypeLiteral<T> typeLiteral) {
            return new CollectionsRegistryAnnotatedBindingBuilderImpl<>(binder, typeLiteral);
        }

        @Override
        public <T> RegistryLinkedBindingBuilder<T> newFrom(Key<T> key) {
            return new CollectionsRegistryAnnotatedBindingBuilderImpl<>(binder, key);
        }
    }
}