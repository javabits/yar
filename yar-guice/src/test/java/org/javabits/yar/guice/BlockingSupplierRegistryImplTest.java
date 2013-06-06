/*
 * Copyright 2013 Romain Gilles
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javabits.yar.guice;

import com.google.common.util.concurrent.ListenableFuture;
import org.javabits.yar.BlockingSupplier;
import org.javabits.yar.BlockingSupplierRegistry;
import org.javabits.yar.Id;
import org.junit.Test;

import javax.inject.Provider;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
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
    public void testNonBlockingGet() {
        BlockingSupplierRegistry registry = newBlockingSupplierRegistry();
        BlockingSupplier<MyInterface> supplier = registry.get(MyInterface.class);

        assertNotNull(supplier);
        assertNull(supplier.get());

        final MyInterface myService = new MyInterfaceImpl();
        registry.put(GuiceId.of(MyInterface.class), new GuiceSupplier<>(new Provider<MyInterface>() {
            @Override
            public MyInterface get() {
                return myService;
            }
        }));

        assertThat(supplier.get(), is(myService));
    }

    @Test
    public void testGetSync() throws InterruptedException {
        final BlockingSupplierRegistry registry = newBlockingSupplierRegistry();
        final MyInterfaceImpl myService = new MyInterfaceImpl();
        final Object[] myServiceSupplier = new Object[1];
        final Lock lock = new ReentrantLock();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                lock.lock();
                try {
                    BlockingSupplier<MyInterface> myServiceBlockingSupplier = registry
                            .get(MyInterface.class);
                    assertThat(myServiceBlockingSupplier, is(not(nullValue())));
                    myServiceSupplier[0] = myServiceBlockingSupplier.getSync(200, MILLISECONDS);
                } catch (InterruptedException | TimeoutException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        });
        thread.start();
        Thread.sleep(100);
        Id<MyInterface> id = GuiceId.of(MyInterface.class);
        registry.put(id, new GuiceSupplier<>(new Provider<MyInterface>() {
            @Override
            public MyInterface get() {
                return myService;
            }
        }));
        lock.lock();
        try {
            assertThat(myServiceSupplier[0], is(not(nullValue())));
            assertThat((MyInterfaceImpl) myServiceSupplier[0], is(myService));
        } finally {
            lock.unlock();
        }
    }

    @Test
    public void testGetAsync() throws Exception {
        final BlockingSupplierRegistry registry = newBlockingSupplierRegistry();
        final MyInterfaceImpl myService = new MyInterfaceImpl();
        final Lock lock = new ReentrantLock();
        final ListenableFuture<MyInterface>[] listenableFuture = new ListenableFuture[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                lock.lock();
                try {
                    BlockingSupplier<MyInterface> myServiceBlockingSupplier = registry
                            .get(MyInterface.class);
                    assertThat(myServiceBlockingSupplier, is(not(nullValue())));
                    listenableFuture[0] = myServiceBlockingSupplier.getAsync();
                } finally {
                    lock.unlock();
                }
            }
        });
        thread.start();
        Thread.sleep(100);
        Id<MyInterface> id = GuiceId.of(MyInterface.class);
        registry.put(id, new GuiceSupplier<>(new Provider<MyInterface>() {
            @Override
            public MyInterface get() {
                return myService;
            }
        }));
        lock.lock();
        try {
            assertThat(listenableFuture[0], is(not(nullValue())));
            assertThat(listenableFuture[0].get(1, MILLISECONDS), is((MyInterface) myService));
        } finally {
            lock.unlock();
        }
    }
}
