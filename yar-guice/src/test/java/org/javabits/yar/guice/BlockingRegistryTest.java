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

import org.junit.Test;
import org.javabits.yar.Supplier;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * TODO comment
 * Date: 2/26/13
 * Time: 5:10 PM
 *
 * @author Romain Gilles
 */
public class BlockingRegistryTest {

    @Test
    public void testGetWithTimeout() throws Exception {
        final BlockingRegistry registry = new BlockingRegistry();
        final Supplier<?>[] myServiceSupplier = new Supplier[1];
        final Lock lock = new ReentrantLock();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                lock.lock();
                try {
                    myServiceSupplier[0] = registry.get(MyService.class, 1, TimeUnit.SECONDS);
                } finally {
                    lock.unlock();
                }
            }
        });
        thread.start();
        registry.put(GuiceId.of(MyService.class), new GuiceSupplier<MyService>(null));
        lock.lock();
        try {
            assertThat(myServiceSupplier[0], is(not(nullValue())));
        } finally {
            lock.unlock();
        }
    }

    @Test
    public void testGetWithTimeoutInError() throws Exception {
        final BlockingRegistry blockingRegistry = new BlockingRegistry();
        final Supplier<?>[] myServiceSupplier = new Supplier[1];
        final Lock lock = new ReentrantLock();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                lock.lock();
                try {
                    myServiceSupplier[0] = blockingRegistry.get(MyService.class, 10, TimeUnit.MILLISECONDS);
                } finally {
                    lock.unlock();
                }
            }
        });
        thread.start();

        lock.lock();
        try {
            assertThat(myServiceSupplier[0], is(nullValue()));
        } finally {
            lock.unlock();
        }
    }


    static interface MyService {
    }
}
