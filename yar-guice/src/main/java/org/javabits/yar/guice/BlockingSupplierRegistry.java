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

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.javabits.yar.BlockingSupplier;
import org.javabits.yar.Id;
import org.javabits.yar.Supplier;

import javax.annotation.Nullable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static org.javabits.yar.IdMatchers.newKeyMatcher;
import static org.javabits.yar.guice.ExecutionStrategy.SYNCHRONOUS;
import static org.javabits.yar.guice.GuiceWatchableRegistrationContainer.newLoadingCacheGuiceWatchableRegistrationContainer;
import static org.javabits.yar.guice.GuiceWatchableRegistrationContainer.newMultimapGuiceWatchableRegistrationContainer;

/**
 * TODO comment
 * Date: 2/28/13
 * Time: 10:57 AM
 *
 * @author Romain Gilles
 */
public class BlockingSupplierRegistry extends SimpleRegistry implements org.javabits.yar.BlockingSupplierRegistry {
    public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;
    public static final long DEFAULT_TIMEOUT = 0L;

    private final long defaultTimeout;
    private final TimeUnit defaultTimeUnit;


    public BlockingSupplierRegistry() {
        this(DEFAULT_TIMEOUT);
    }

    public BlockingSupplierRegistry(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
        defaultTimeUnit = DEFAULT_TIME_UNIT;
    }

    public BlockingSupplierRegistry(WatchableRegistrationContainer registrationContainer, long defaultTimeout) {
        super(registrationContainer);
        this.defaultTimeout = defaultTimeout;
        defaultTimeUnit = DEFAULT_TIME_UNIT;
    }


    @Override
    public <T> BlockingSupplier<T> get(Class<T> type) {
        return get(GuiceId.of(type));
    }

    @Override
    public <T> BlockingSupplier<T> get(final Id<T> id) {
        return new TimeoutBlockingSupplier<>(id, super.get(id), defaultTimeout, defaultTimeUnit);
    }

    class TimeoutBlockingSupplier<T> extends AbstractBlockingSupplier<T> implements BlockingSupplier<T> {
        private final long timeout;
        private final TimeUnit unit;
        private final Queue<SettableFuture<T>> abstractFutures;
        TimeoutBlockingSupplier(final Id<T> id, Supplier<T> delegate, long timeout, TimeUnit unit) {
            super(new FirstSupplierProvider<>(id), delegate);
            this.timeout = timeout;
            this.unit = unit;
            this.abstractFutures = new ConcurrentLinkedQueue<>();
            addSupplierListener(newKeyMatcher(id), this);
        }

        @Nullable
        @Override
        public T get() {
            return get(timeout, unit);
        }

        @Nullable
        @Override
        public T get(long timeout, TimeUnit unit) {
            readLock.lock();
            try {
                if (delegate != null) {
                    return delegate.get();
                } else if (timeout == 0L) {
                    return null;
                }
            } finally {
                readLock.unlock();
            }
            if (timeout < 0L) {
                return infiniteBlockingGet();
            } else {
                return timeoutBlockingGet(timeout, unit);
            }
        }

        @Override
        public ListenableFuture<T> getAsynch() {
            readLock.lock();
            try {
                if (delegate != null) {
                    return Futures.immediateFuture(delegate.get());
                }
            } finally {
                readLock.unlock();
            }
            writeLock.lock();
            try {
                if (delegate != null) {
                    return Futures.immediateFuture(delegate.get());
                } else {
                    SettableFuture<T> future = SettableFuture.create();
                    abstractFutures.add(future);
                    return future;
                }
            } finally {
                writeLock.unlock();
            }
        }

        @Nullable
        @Override
        Supplier<T> add(Supplier<T> element) {
            Supplier<T> supplier = super.add(element);
            for (SettableFuture<T> poll = abstractFutures.poll(); poll != null;poll = abstractFutures.poll()) {
                poll.set(element.get());
            }
            return supplier;
        }

        private T timeoutBlockingGet(long timeout, TimeUnit unit) {
            long nanos = unit.toNanos(timeout);
            writeLock.lock();
            try {
                while (delegate == null) {
                    if (nanos <= 0L) {
                        return null; // TODO maybe throw an exception instead!!!
                    }
                    nanos = notEmpty.awaitNanos(nanos);
                }
                return delegate.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e); // TODO see what to do with the interrupted exception!!!
            } finally {
                writeLock.unlock();
            }
        }


        private T infiniteBlockingGet() {
            writeLock.lock();
            try {
                while (delegate == null) {
                    notEmpty.await();
                }
                return delegate.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e); // TODO see what to do with the interrupted exception!!!
            } finally {
                writeLock.unlock();
            }
        }


    }

    static BlockingSupplierRegistry newMultimapBlockingSupplierRegistry() {
        return newMultimapBlockingSupplierRegistry(DEFAULT_TIMEOUT);
    }

    static BlockingSupplierRegistry newMultimapBlockingSupplierRegistry(long defaultTimeout) {
        return new BlockingSupplierRegistry(newMultimapGuiceWatchableRegistrationContainer(), defaultTimeout);
    }

    static BlockingSupplierRegistry newLoadingCacheBlockingSupplierRegistry() {
        return newLoadingCacheBlockingSupplierRegistry(SYNCHRONOUS);
    }

    static BlockingSupplierRegistry newLoadingCacheBlockingSupplierRegistry(ExecutionStrategy executionStrategy) {
        return newLoadingCacheBlockingSupplierRegistry(DEFAULT_TIMEOUT, executionStrategy);
    }

    static BlockingSupplierRegistry newLoadingCacheBlockingSupplierRegistry(long defaultTimeout) {
        return newLoadingCacheBlockingSupplierRegistry(defaultTimeout, SYNCHRONOUS);
    }

    static BlockingSupplierRegistry newLoadingCacheBlockingSupplierRegistry(long defaultTimeout, ExecutionStrategy executionStrategy) {
        return new BlockingSupplierRegistry(newLoadingCacheGuiceWatchableRegistrationContainer(executionStrategy), defaultTimeout);
    }

    private class FirstSupplierProvider<T> implements org.javabits.yar.guice.FirstSupplierProvider<T> {
        private final Id<T> id;

        public FirstSupplierProvider(Id<T> id) {
            this.id = id;
        }

        @Nullable
        @Override
        public Supplier<T> get() {
            return BlockingSupplierRegistry.super.get(id);
        }
    }
}
