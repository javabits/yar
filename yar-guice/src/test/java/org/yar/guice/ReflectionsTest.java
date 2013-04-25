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

import org.junit.Test;

import java.lang.reflect.Type;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.yar.guice.Reflections.getUniqueParameterType;

/**
 * TODO comment
 * Date: 3/20/13
 *
 * @author Romain Gilles
 */
public class ReflectionsTest {

    @Test
    public void testGetParameterizedStringTypeAnonymousInterface() {
        Class<? extends MonoParameterized<String>> given = givenAnonymousInterfaceStringImpl();
        Type actual = whenGetUniqueParameterType(given);
        thenReturnedParameterTypeMustBeString(actual);
    }

    private Class<? extends MonoParameterized<String>> givenAnonymousInterfaceStringImpl() {
        return new MonoParameterized<String>() {
        }.getClass();
    }

    private Type whenGetUniqueParameterType(Class<? extends MonoParameterized<String>> type) {
        return getUniqueParameterType(type, MonoParameterized.class, "MonoParameterized<T>");
    }

    private void thenReturnedParameterTypeMustBeString(Type actual) {
        thenReturnedParameterTypeMustBe(actual, stringType());
    }

    private void thenReturnedParameterTypeMustBe(Type actual, Type expectedType) {
        assertThat(actual, is(expectedType));
    }

    private Type stringType() {
        return String.class;
    }

    @Test
    public void testGetParameterizedStringTypeAnonymousAbstract() {
        Class<? extends MonoParameterized<String>> given = givenAnonymousAbstractStringImpl();
        Type actual = whenGetUniqueParameterType(given);
        thenReturnedParameterTypeMustBeString(actual);
    }

    private Class<? extends AbstractMonoParameterized<String>> givenAnonymousAbstractStringImpl() {
        return new AbstractMonoParameterized<String>() {
        }.getClass();
    }

    @Test
    public void testGetParameterizedStringTypeInterfaceImpl() {
        Class<? extends MonoParameterized<String>> given = givenStringInterfaceImpl();
        Type actual = whenGetUniqueParameterType(given);
        thenReturnedParameterTypeMustBeString(actual);
    }

    private Class<MonoParameterizedImpl> givenStringInterfaceImpl() {
        return MonoParameterizedImpl.class;
    }

    @Test
    public void testGetParameterizedStringTypeAbstractImpl() {
        Class<? extends MonoParameterized<String>> given = givenStringAbstractImpl();
        Type actual = whenGetUniqueParameterType(given);
        thenReturnedParameterTypeMustBeString(actual);
    }

    private Class<AbstractMonoParameterizedImpl> givenStringAbstractImpl() {
        return AbstractMonoParameterizedImpl.class;
    }

    @SuppressWarnings("unused")
    static interface MonoParameterized<T> {
    }

    static abstract class AbstractMonoParameterized<T> implements MonoParameterized<T> {
    }

    static class MonoParameterizedImpl implements MonoParameterized<String> {
    }

    static class AbstractMonoParameterizedImpl extends AbstractMonoParameterized<String> {
    }

}
