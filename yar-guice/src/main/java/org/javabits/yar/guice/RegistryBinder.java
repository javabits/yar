package org.javabits.yar.guice;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;

/**
 * This interface is responsible to provide access to the registry
 * through the Guice EDSL.
 *
 * @author Romain Gilles
 *         Date: 10/9/13
 */
public interface RegistryBinder extends Binder {

    <T> void bindRegistryListener(Matcher<Key<T>> matcher, Key<? extends RegistryListener<? super T>> key);

    <T> void bindRegistryListener(Matcher<Key<T>> matcher, RegistryListener<? super T> listener);

    <T> RegistrationLinkedBindingBuilder<T> register(Key<T> key);

    <T> RegistrationAnnotatedBindingBuilder<T> register(TypeLiteral<T> typeLiteral);

    <T> RegistrationAnnotatedBindingBuilder<T> register(Class<T> type);

    @Override
    <T> RegistryLinkedBindingBuilder<T> bind(Key<T> key);

    @Override
    <T> RegistryAnnotatedBindingBuilder<T> bind(TypeLiteral<T> typeLiteral);

    @Override
    <T> RegistryAnnotatedBindingBuilder<T> bind(Class<T> clazz);

}
