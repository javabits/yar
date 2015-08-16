/*
 * Copyright (c) 3/7/14 12:16 PM Romain Gilles
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

import static java.lang.Boolean.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.javabits.yar.IdMatchers.newIdMatcher;
import static org.javabits.yar.Ids.newId;
import static org.javabits.yar.guice.BlockingSupplierRegistryImpl.newBlockingSupplierRegistry;

import java.lang.InterruptedException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import org.javabits.yar.*;
import org.junit.Test;

/**
 * This Test case aims to validate the weak reference management on the watcher. To run this test
 * you must fix the Xmx and Xms to the same value.
 */
public class WatcherWeakReferenceTest {

    public static final int SLEEP_TIME_MILLIS = 1;
    public static final int MAIN_STEP_WAIT_TIME = 1;
    public static final Id<Boolean> ID = newId(Boolean.class);

    @Test
    public void testWeakWatcher() {
        sleep();
        BlockingSupplierRegistry registry = newBlockingSupplierRegistry();
        final AtomicInteger counter = new AtomicInteger();
        registerWatcherAndSupplier(registry, counter);
        forceGC();

        sleep(MAIN_STEP_WAIT_TIME);
        registry.put(ID, ()-> TRUE);
        assertThat(counter.get(), is(1));
    }

    private void
            registerWatcherAndSupplier(BlockingSupplierRegistry registry, AtomicInteger counter) {
        Watcher<Boolean> watcher = new BooleanWatcher(counter);
        registry.addWatcher(newIdMatcher(ID), watcher);
        assertThat(counter.get(), is(0));
        registry.put(ID, ()-> TRUE);
        sleep();
        assertThat(counter.get(), is(1));
        System.out.println("End of watcher");
        sleep(MAIN_STEP_WAIT_TIME);
    }

    @Test
    public void testWeakWatcherNoCleanup() {
        sleep();
        BlockingSupplierRegistry registry = newBlockingSupplierRegistry();
        Id<Boolean> id = newId(Boolean.class);
        final AtomicInteger counter = new AtomicInteger();
        Watcher<Boolean> watcher = new BooleanWatcher(counter);

        Registration<Boolean> registration = registry.addWatcher(newIdMatcher(id), watcher);
        assertThat(counter.get(), is(0));
        registry.put(id, ()-> TRUE);
        sleep();
        assertThat(counter.get(), is(1));
        System.out.println("End of watcher");
        sleep(MAIN_STEP_WAIT_TIME);

        forceGC();
        System.out.println("End of Memory cleanup");
        sleep(MAIN_STEP_WAIT_TIME);
        registry.put(id, ()-> TRUE);
        List<Supplier<Boolean>> idSuppliers = registry.getAll(id);
        assertThat(idSuppliers.size(), is(2));
        assertThat(counter.get(), is(2));
    }

    private void forceGC() {
        System.out.println("Start Memory cleanup");
        Runtime runtime = Runtime.getRuntime();
        int tableSize = Double.valueOf(runtime.freeMemory() / 8 * 0.4).intValue();
        for (int i = 0; i < 3; i++) {
            fillMemoryConsumer(tableSize);
            System.gc();
        }
        System.out.println("End of Memory cleanup");
    }

    private void fillMemoryConsumer(int tableSize) {
        long[] memoryConsumer = new long[tableSize];
        for (int i = 0; i < tableSize; i++) {
            memoryConsumer[i] = i;
        }
    }

    private void sleep() {
        sleep(SLEEP_TIME_MILLIS);
    }

    private void sleep(int sleepTimeMillis) {
        try {
            Thread.sleep(sleepTimeMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testWeakWatcherWithBlockingSupplier() throws InterruptedException {
        BlockingSupplierRegistry registry = newBlockingSupplierRegistry();
        getBlockingSupplierAndProvideSupplier(registry);
        forceGC();
        System.out.println("End of Memory cleanup");
        sleep(MAIN_STEP_WAIT_TIME);
    }

    private void getBlockingSupplierAndProvideSupplier(BlockingSupplierRegistry registry)
            throws InterruptedException {
        BlockingSupplier<Boolean> blockingSupplier = registry.get(ID);
        assert blockingSupplier != null;
        assertThat(blockingSupplier.get(), is(nullValue()));
        registry.put(ID, ()-> TRUE);
        sleep();
        assertThat(blockingSupplier.getSync(), is(TRUE));
        System.out.println("End of watcher");
        sleep(MAIN_STEP_WAIT_TIME);
    }

    private static class BooleanWatcher implements Watcher<Boolean> {
        private final AtomicInteger counter;

        public BooleanWatcher(AtomicInteger counter) {
            this.counter = counter;
        }

        @Nullable
        @Override
        public Supplier<Boolean> add(Supplier<Boolean> element) {
            counter.incrementAndGet();
            return element;
        }

        @Override
        public void remove(Supplier<Boolean> element) {
            counter.decrementAndGet();
        }
    }
}
