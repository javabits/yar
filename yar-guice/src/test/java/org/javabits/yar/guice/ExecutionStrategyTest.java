/*
 * Copyright (c) 10/23/13 8:46 PM Romain Gilles
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

import com.google.common.collect.ImmutableList;
import org.hamcrest.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.javabits.yar.guice.AbstractExecutionStrategy.newExecutionStrategy;
import static org.javabits.yar.guice.ExecutionStrategy.Type.PARALLEL;
import static org.javabits.yar.guice.ExecutionStrategy.Type.SAME_THREAD;
import static org.javabits.yar.guice.ExecutionStrategy.Type.SERIALIZED;

/**
 * Date: 10/23/13
 *
 * @author Romain Gilles
 */
public class ExecutionStrategyTest {

    @Test
    public void testExecute_NotBlockingOnSERIALIZED() throws Exception {
        executeAblockinglistOfTask(newExecutionStrategy(SERIALIZED));
        //succeed
    }
    @Test
    public void testExecute_NotBlockingOnPARALLEL() throws Exception {
        executeAblockinglistOfTask(newExecutionStrategy(PARALLEL));
        //succeed
    }

    @Test
    public void testExecute_NotBlockingOnSAME_THREAD() throws Exception {
        //use a local thread variables not synchronised to check that the current thread modify it.
        final int[] count = new int[]{0};
        Callable<Void> barrierCallable = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                count[0]=1;
                return null;
            }
        };

        List<Callable<Void>> callables = ImmutableList.of(newDummyCallable(), newDummyCallable(), barrierCallable);
        newExecutionStrategy(SAME_THREAD).execute(callables, 5, MILLISECONDS);
        assertThat(count[0], is(1));
        //succeed
    }

    private void executeAblockinglistOfTask(ExecutionStrategy executionStrategy) throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Callable<Void> barrierCallable = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                countDownLatch.await();
                return null;
            }
        };

        List<Callable<Void>> callables = ImmutableList.of(newDummyCallable(), newDummyCallable(), barrierCallable);
        executionStrategy.execute(callables, 5, MILLISECONDS);
        countDownLatch.countDown();
    }

    private Callable<Void> newDummyCallable() {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                return null;
            }
        };
    }
}
