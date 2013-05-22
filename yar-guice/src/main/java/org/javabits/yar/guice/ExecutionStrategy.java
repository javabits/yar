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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TODO comment Date: 4/19/13 Time: 9:00 PM
 * 
 * @author Romain Gilles
 */
public enum ExecutionStrategy {

    SYNCHRONOUS {
        @Override
        void execute(Callable<Void> runnable) {
            try {
                runnable.call();
            } catch (Exception e) {
                logTaskExecutionException(runnable, e);
            }
        }

        @Override
        void execute(Collection<Callable<Void>> runnables) {
            for (Callable<Void> runnable : runnables) {
                execute(runnable);
            }
        }
    },
    ASYNCHRONOUS {

        private final ExecutorService executorService = Executors
                .newCachedThreadPool(new DaemonThreadFactory("registry"));

        @Override
        void execute(Callable<Void> runnable) throws InterruptedException {
            Future<?> submit = executorService.submit(runnable);
            try {
                submit.get(1, TimeUnit.SECONDS);
            } catch (ExecutionException | TimeoutException  e) {
                logTaskExecutionException(runnable, e);
            }
        }

        @Override
        void execute(Collection<Callable<Void>> tasks) throws InterruptedException {
            try {
                List<Future<Void>> futures = executorService.invokeAll(tasks, 1, TimeUnit.SECONDS);
                for (Future<Void> future : futures) {
                    try {
                        if (future.isDone())
                            future.get();
                        else
                            LOG.finest("task not done");
                    } catch (ExecutionException e) {
                        logTaskExecutionException(future, e);
                    }
                }
            } catch (RuntimeException e) {
                LOG.log(Level.SEVERE, "Error when execute tasks", e);
            }

        }
    };

    private static void logTaskExecutionException(Object task, Throwable e) {
        LOG.log(Level.SEVERE, "cannot execute the submitted a task: " + task, e);
    }

    private static final Logger LOG = Logger.getLogger(ExecutionStrategy.class.getName());

    abstract void execute(Callable<Void> runnable) throws InterruptedException;

    abstract void execute(Collection<Callable<Void>> runnables) throws InterruptedException;

    private static class DaemonThreadFactory implements ThreadFactory {
        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;
        static final String nameSuffix = "]";

        public DaemonThreadFactory(String poolName) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "YAR " + poolName + " Pool [Thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement()
                    + nameSuffix, 0);
            t.setDaemon(true);
            // if (t.getPriority() != Thread.NORM_PRIORITY)
            // t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

}
