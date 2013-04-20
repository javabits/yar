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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO comment
 * Date: 4/19/13
 * Time: 9:00 PM
 *
 * @author Romain Gilles
 */
public enum ExecutionStrategy {
    SYNCHRONOUS {
        @Override
        void execute(Runnable runnable) {
            runnable.run();
        }
    },
    ASYNCHRONOUS {
        private final ExecutorService executorService = Executors.newCachedThreadPool(new DaemonThreadFactory("registry"));

        @Override
        void execute(Runnable runnable) {
            executorService.submit(runnable);
        }
    };

    abstract void execute(Runnable runnable);

    private static class DaemonThreadFactory implements ThreadFactory {
        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;
        static final String nameSuffix = "]";

        public DaemonThreadFactory(String poolName) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "YAR " + poolName + " Pool [Thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group,
                    r,
                    namePrefix +
                            threadNumber.getAndIncrement() +
                            nameSuffix,
                    0);
            t.setDaemon(true);
//            if (t.getPriority() != Thread.NORM_PRIORITY)
//                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

}
