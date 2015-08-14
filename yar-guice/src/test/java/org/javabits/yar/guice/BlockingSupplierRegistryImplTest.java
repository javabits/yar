/*
 * Copyright (c) 10/23/13 11:26 PM Romain Gilles
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

import org.javabits.yar.BlockingSupplier;
import org.javabits.yar.BlockingSupplierRegistry;
import org.javabits.yar.Id;
import org.javabits.yar.Ids;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.javabits.yar.guice.BlockingSupplierRegistryImpl.newBlockingSupplierRegistry;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * TODO comment Date: 2/28/13 Time: 11:43 AM
 *
 * @author Romain Gilles
 */
public class BlockingSupplierRegistryImplTest {

    @Test
    public void testNonBlockingGet() throws Exception {
        BlockingSupplierRegistry registry = newBlockingSupplierRegistry();
        BlockingSupplier<MyInterface> supplier = registry.get(MyInterface.class);

        assertNotNull(supplier);
        assertNull(supplier.get());

        final MyInterface myService = new MyInterfaceImpl();
        registry.put(Ids.newId(MyInterface.class), new GuiceSupplier<>(() -> myService));

        assertThat(supplier.getSync(1, SECONDS), is(myService));
    }

    @Test
    public void testGetSync() throws InterruptedException {
        final BlockingSupplierRegistry registry = newBlockingSupplierRegistry();
        final MyInterfaceImpl myService = new MyInterfaceImpl();
        final Object[] myServiceSupplier = new Object[1];
        final Lock lock = new ReentrantLock();

        Thread thread = new Thread(() -> {
            lock.lock();
            try {
                BlockingSupplier<MyInterface> myServiceBlockingSupplier = registry
                        .get(MyInterface.class);
                assertThat(myServiceBlockingSupplier, is(not(nullValue())));
                assert myServiceBlockingSupplier != null;
                myServiceSupplier[0] = myServiceBlockingSupplier.getSync(1, SECONDS);
            } catch (InterruptedException | TimeoutException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        });
        thread.start();
        Thread.sleep(100);
        Id<MyInterface> id = Ids.newId(MyInterface.class);
        registry.put(id, new GuiceSupplier<>(() -> myService));
        lock.lock();
        try {
            assertThat(myServiceSupplier[0], is(not(nullValue())));
            assertThat(myServiceSupplier[0], is(myService));
        } finally {
            lock.unlock();
        }
    }

    @Test
    public void testGetAsync() throws Exception {
        final BlockingSupplierRegistry registry = newBlockingSupplierRegistry();
        final MyInterfaceImpl myService = new MyInterfaceImpl();
        final Lock lock = new ReentrantLock();
        @SuppressWarnings("unchecked")
        final CompletableFuture<MyInterface>[] listenableFuture = new CompletableFuture[1];
        Thread thread = new Thread(() -> {
            lock.lock();
            try {
                BlockingSupplier<MyInterface> myServiceBlockingSupplier = registry
                        .get(MyInterface.class);
                assertThat(myServiceBlockingSupplier, is(not(nullValue())));
                assert myServiceBlockingSupplier != null;
                listenableFuture[0] = myServiceBlockingSupplier.getAsync();
            } finally {
                lock.unlock();
            }
        });
        thread.start();
        Thread.sleep(100);
        Id<MyInterface> id = Ids.newId(MyInterface.class);
        registry.put(id, new GuiceSupplier<>(() -> myService));
        lock.lock();
        try {
            assertThat(listenableFuture[0], is(not(nullValue())));
            assertThat(listenableFuture[0].get(1, SECONDS), is((MyInterface) myService));
        } finally {
            lock.unlock();
        }
    }
}
