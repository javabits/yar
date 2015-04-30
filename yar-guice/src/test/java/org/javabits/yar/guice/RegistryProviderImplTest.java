/*
 * Copyright (c) 4/30/15 3:42 PM Romain Gilles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javabits.yar.guice;

import com.google.inject.Key;
import org.javabits.yar.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.InterruptedException;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RegistryProviderImplTest {

    public static final Key<String> STRING_KEY = Key.get(String.class);
    @Mock
    private BlockingSupplierRegistry registry;
    @Mock
    private BlockingSupplier<String> blockingSupplier;


    @Test
    public void testKey() {
        RegistryProviderImpl<String> provider = new RegistryProviderImpl<>(STRING_KEY);
        assertThat(provider.key(), is(STRING_KEY));
    }

    @Test
    public void testNoWait() throws Exception {
        //given
        when(registry.get(GuiceId.of(STRING_KEY))).thenReturn(blockingSupplier);
        when(blockingSupplier.defaultTimeout()).thenReturn(1L);
        when(blockingSupplier.defaultTimeUnit()).thenReturn(DAYS);
        when(blockingSupplier.get()).thenReturn("test");
        //when
        RegistryProviderImpl<String> provider = new RegistryProviderImpl<>(STRING_KEY);
        provider.setRegistry(registry);
        provider.noWait();
        String value = provider.get();
        assertThat(value, is("test"));
    }


    @Test
    public void testDefaultTimeout() throws Exception {
        //given
        when(registry.get(GuiceId.of(STRING_KEY))).thenReturn(blockingSupplier);
        when(blockingSupplier.defaultTimeout()).thenReturn(1L);
        when(blockingSupplier.defaultTimeUnit()).thenReturn(DAYS);
        when(blockingSupplier.getSync(1L, DAYS)).thenReturn("test");
        //when
        RegistryProviderImpl<String> provider = new RegistryProviderImpl<>(STRING_KEY);
        provider.setRegistry(registry);
        String value = provider.get();
        assertThat(value, is("test"));
    }


    @Test
    public void testExceptionWithDefaultTimeout() throws Exception {
        //given
        when(registry.get(GuiceId.of(STRING_KEY))).thenReturn(blockingSupplier);
        when(blockingSupplier.defaultTimeout()).thenReturn(1L);
        when(blockingSupplier.defaultTimeUnit()).thenReturn(DAYS);
        doThrow(java.util.concurrent.TimeoutException.class).when(blockingSupplier).getSync(1L, DAYS);
        //when
        RegistryProviderImpl<String> provider = new RegistryProviderImpl<>(STRING_KEY);
        provider.setRegistry(registry);
        try {
            provider.get();
            Assert.fail("Expected " + TimeoutException.class.getName() + " exception.");
        } catch (TimeoutException e) {
            //then
            assertThat(e.getTimeout(), is(1l));
            assertThat(e.getUnit(), is(DAYS));
            assertThat(e.getMessage(), containsString("" + 1 + " " + DAYS));
        }
    }

    @Test
    public void testInterruptedExceptionWithDefaultTimeout() throws Exception {
        //given
        when(registry.get(GuiceId.of(STRING_KEY))).thenReturn(blockingSupplier);
        when(blockingSupplier.defaultTimeout()).thenReturn(1L);
        when(blockingSupplier.defaultTimeUnit()).thenReturn(DAYS);
        doThrow(InterruptedException.class).when(blockingSupplier).getSync(1L, DAYS);
        //when
        RegistryProviderImpl<String> provider = new RegistryProviderImpl<>(STRING_KEY);
        provider.setRegistry(registry);
        try {
            provider.get();
            Assert.fail("Expected " + org.javabits.yar.InterruptedException.class.getName() + " exception.");
        } catch (org.javabits.yar.InterruptedException e) {
            //then test and clear the interrupted status
            assertThat(Thread.interrupted(), is(true));        }
    }

    @Test
    public void testDefineTimeoutAtBindingTime() throws Exception {
        //given
        when(registry.get(GuiceId.of(STRING_KEY))).thenReturn(blockingSupplier);
        when(blockingSupplier.getSync(2L, MINUTES)).thenReturn("test");
        //when
        RegistryProviderImpl<String> provider = new RegistryProviderImpl<>(STRING_KEY, 2, MINUTES);
        provider.setRegistry(registry);
        String value = provider.get();
        assertThat(value, is("test"));
    }

    @Test
    public void testExceptionWithDefineTimeoutAtBindingTime() throws Exception {
        //given
        when(registry.get(GuiceId.of(STRING_KEY))).thenReturn(blockingSupplier);
        doThrow(java.util.concurrent.TimeoutException.class).when(blockingSupplier).getSync(2L, MINUTES);
        //when
        RegistryProviderImpl<String> provider = new RegistryProviderImpl<>(STRING_KEY, 2, MINUTES);
        provider.setRegistry(registry);
        try {
            provider.get();
            Assert.fail("Expected " + TimeoutException.class.getName() + " exception.");
        } catch (TimeoutException e) {
            //then
            assertThat(e.getTimeout(), is(2l));
            assertThat(e.getUnit(), is(MINUTES));
            assertThat(e.getMessage(), containsString("" + 2 + " " + MINUTES));
        }
    }

    @Test
    public void testExceptionInterruptedWithDefineTimeoutAtBindingTime() throws Exception {
        //given
        when(registry.get(GuiceId.of(STRING_KEY))).thenReturn(blockingSupplier);
        doThrow(InterruptedException.class).when(blockingSupplier).getSync(2L, MINUTES);
        //when
        RegistryProviderImpl<String> provider = new RegistryProviderImpl<>(STRING_KEY, 2, MINUTES);
        provider.setRegistry(registry);
        try {
            provider.get();
            Assert.fail("Expected " + org.javabits.yar.InterruptedException.class.getName() + " exception.");
        } catch (org.javabits.yar.InterruptedException e) {
            //then test and clear the interrupted status
            assertThat(Thread.interrupted(), is(true));
        }
    }
}