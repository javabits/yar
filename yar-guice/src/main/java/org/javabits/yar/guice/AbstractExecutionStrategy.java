/*
 * Copyright (c) 10/24/13 7:14 AM Romain Gilles
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.javabits.yar.RegistryHook;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.util.concurrent.Futures.addCallback;

/**
 * Date: 10/24/13
 * @author Romain Gilles
 */
public abstract class AbstractExecutionStrategy implements ExecutionStrategy {
    private static final Logger LOG = Logger.getLogger(ExecutionStrategy.class.getName());

    private final Map<ListenableFuture<Void>, Callable<Void>> pendingTasks = Maps.newConcurrentMap();

    abstract ListeningExecutorService executorService();

    public boolean hasPendingTasks() {
        return !pendingTasks.isEmpty();
    }

    public void addEndOfListenerUpdateTasksListener(final RegistryHook.EndOfListenerUpdateTasksListener pendingTaskLister) {
        final Map<ListenableFuture<Void>, Callable<Void>> pendingTaskSnapshot = new ConcurrentHashMap<>(pendingTasks);
        if (pendingTaskSnapshot.isEmpty()) {
            pendingTaskLister.completed();
            return;
        }
        for (final ListenableFuture<Void> future : pendingTaskSnapshot.keySet()) {
            future.addListener(() -> {
                pendingTaskSnapshot.remove(future);
                if (pendingTaskSnapshot.isEmpty())
                    pendingTaskLister.completed();
            }, executorService());
        }
    }

    public void execute(final List<Callable<Void>> tasks, final long timeout, final TimeUnit unit) throws InterruptedException {
        for (final Callable<Void> task : tasks) {
            final ListenableFuture<Void> future = executorService().submit(task);
            pendingTasks.put(future, task);
            future.addListener(() -> pendingTasks.remove(future), executorService());

            addCallback(future, new FutureCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    LOG.log(Level.FINE, String.format("Listener task succeeded : %s", task));
                }

                @Override
                public void onFailure(Throwable t) {
                    LOG.log(Level.SEVERE, String.format("Listener task failed: %s", task), t);
                }
            });
        }
    }

    public static ExecutionStrategy newExecutionStrategy(Type strategy) {
        switch (strategy) {
            case SAME_THREAD:
                return new SameThread();
            case PARALLEL:
                return new Parallel();
            case SERIALIZED:
                return new Serialized();
            default:
                throw new IllegalArgumentException("Unknown strategy type: " + strategy);
        }
    }

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

        @Override
        public Thread newThread(@SuppressWarnings("NullableProblems") Runnable runnable) {
            checkNotNull(runnable, "runnable");
            Thread t = new Thread(group, runnable, namePrefix + threadNumber.getAndIncrement()
                    + nameSuffix, 0);
            t.setDaemon(true);
            // if (t.getPriority() != Thread.NORM_PRIORITY)
            // t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }


    @VisibleForTesting
    private static class SameThread extends AbstractExecutionStrategy {
        private final ListeningExecutorService executorService = MoreExecutors.newDirectExecutorService();

        @Override
        ListeningExecutorService executorService() {
            return executorService;
        }
    }

    private static class Parallel extends AbstractExecutionStrategy {
        private final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors
                .newCachedThreadPool(new DaemonThreadFactory("parallel-listener-handler")));

        @Override
        ListeningExecutorService executorService() {
            return executorService;
        }
    }

    private static class Serialized extends AbstractExecutionStrategy {
        private final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors
                .newSingleThreadExecutor(new DaemonThreadFactory("serialized-listener-handler")));

        @Override
        ListeningExecutorService executorService() {
            return executorService;
        }
    }
}
