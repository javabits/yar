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

import com.google.common.annotations.VisibleForTesting;
import org.javabits.yar.RegistryHook;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * This class is responsible to provide different execution strategies
 * for the watchers / listeners of supplier events.
 *
 * @author Romain Gilles
 */
public interface ExecutionStrategy {

    enum Type {
        @VisibleForTesting
        SAME_THREAD,
        PARALLEL,
        SERIALIZED
    }

    void execute(final List<Callable<Void>> tasks, final long timeout, final TimeUnit unit) throws InterruptedException;

    /**
     * Returns {@code true} if this execution strategy contains no pending task.
     * This information in a lightly concurrent environment is really not relevant.
     * It can be used at startup time when the system is not under evy stress.
     *
     * @return {@code true} if at this point of time there is no pending task.
     */
    boolean hasPendingTasks();

    void addEndOfListenerUpdateTasksListener(RegistryHook.EndOfListenerUpdateTasksListener pendingTaskListener);
}
