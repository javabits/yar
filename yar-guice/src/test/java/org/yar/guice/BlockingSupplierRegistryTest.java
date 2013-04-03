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
import org.yar.BlockingSupplier;

import javax.inject.Provider;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

/**
 * TODO comment
 * Date: 2/28/13
 * Time: 11:43 AM
 *
 * @author Romain Gilles
 */
public class BlockingSupplierRegistryTest {
    @Test
    public void testGet() throws InterruptedException {
        final BlockingSupplierRegistry registry = new BlockingSupplierRegistry();
        final MyServiceImpl myService = new MyServiceImpl();
        final Object[] myServiceSupplier = new Object[1];
        final Lock lock = new ReentrantLock();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                lock.lock();
                try {
                    BlockingSupplier<MyService> myServiceBlockingSupplier = registry.get(MyService.class);
                    assertThat(myServiceBlockingSupplier, is(not(nullValue())));
                    myServiceSupplier[0] = myServiceBlockingSupplier.get(200, TimeUnit.MILLISECONDS);
                } finally {
                    lock.unlock();
                }
            }
        });
        thread.start();
        Thread.sleep(100);
        registry.put(GuiceId.of(MyService.class), new GuiceSupplier<>(new Provider<MyService>() {
            @Override
            public MyService get() {
                return myService;
            }
        }));
        lock.lock();
        try {
            assertThat(myServiceSupplier[0], is(not(nullValue())));
            assertThat((MyServiceImpl)myServiceSupplier[0], is(myService));
        } finally {
            lock.unlock();
        }
    }

    static interface MyService {
    }

    static class MyServiceImpl implements MyService {}

}
