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

import org.javabits.yar.Id;
import org.javabits.yar.Supplier;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

import static org.javabits.yar.IdMatchers.newKeyMatcher;
import static org.javabits.yar.guice.GuiceWatchableRegistrationContainer.newLoadingCacheGuiceWatchableRegistrationContainer;
import static org.javabits.yar.guice.GuiceWatchableRegistrationContainer.newMultimapGuiceWatchableRegistrationContainer;

/**
 * TODO comment
 * Date: 2/25/13
 * Time: 10:30 PM
 *
 * @author Romain Gilles
 */
public class BlockingRegistry extends SimpleRegistry implements org.javabits.yar.BlockingRegistry {

    public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;
    public static final long DEFAULT_TIMEOUT = 0L;

    private final long defaultTimeout;
    private final TimeUnit defaultTimeUnit;

    public BlockingRegistry() {
        this(DEFAULT_TIMEOUT);
    }

    public BlockingRegistry(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
        defaultTimeUnit = DEFAULT_TIME_UNIT;
    }

    public BlockingRegistry(WatchableRegistrationContainer registrationContainer, long defaultTimeout) {
        super(registrationContainer);
        this.defaultTimeout = defaultTimeout;
        defaultTimeUnit = DEFAULT_TIME_UNIT;
    }


    @Nullable
    @Override
    public <T> Supplier<T> get(Id<T> id) {
        return createSupplier(id, super.get(id), defaultTimeout, defaultTimeUnit);
    }

    @Nullable
    @Override
    public <T> Supplier<T> get(Class<T> type, long timeout, TimeUnit unit) {
        return get(GuiceId.of(type), timeout, unit);
    }

    @Nullable
    @Override
    public <T> Supplier<T> get(Id<T> id, long timeout, TimeUnit unit) {
        return createSupplier(id, super.get(id), timeout, unit);
    }


    private <T> Supplier<T> createSupplier(Id<T> id, Supplier<T> originalValue, long timeout, TimeUnit unit) {
        if (timeout == 0) { //direct
            return originalValue;
        } else if (timeout < 0) {
            return new InfiniteBlockingSupplier<>(id, originalValue);
        } else {
            return new TimeoutBlockingSupplier<>(id, originalValue, timeout, unit);
        }
    }

    abstract class AbstractBlockingSupplier<T> extends org.javabits.yar.guice.AbstractBlockingSupplier<T> {

        AbstractBlockingSupplier(Id<T> id, Supplier<T> delegate) {
            super(new FirstSupplierProvider<>(id), delegate);
            addSupplierListener(newKeyMatcher(id), this);
        }
    }

    class TimeoutBlockingSupplier<T> extends AbstractBlockingSupplier<T> {
        private final long timeout;
        private final TimeUnit unit;

        TimeoutBlockingSupplier(Id<T> id, Supplier<T> delegate, long timeout, TimeUnit unit) {
            super(id, delegate);
            this.timeout = timeout;
            this.unit = unit;
        }

        @Override
        public T get() {
            readLock.lock();
            try {
                if (delegate != null) {
                    return delegate.get();
                }
            } finally {
                readLock.unlock();
            }
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
    }


    class InfiniteBlockingSupplier<T> extends AbstractBlockingSupplier<T> {
        InfiniteBlockingSupplier(Id<T> id, Supplier<T> delegate) {
            super(id, delegate);
        }

        @Override
        public T get() {
            readLock.lock();
            try {
                if (delegate != null) {
                    return delegate.get();
                }
            } finally {
                readLock.unlock();
            }
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

    static BlockingRegistry newMultimapBlockingRegistry(long defaultTimeout) {
        return new BlockingRegistry(newMultimapGuiceWatchableRegistrationContainer(), defaultTimeout);
    }

    static BlockingRegistry newLoadingCacheBlockingRegistry(long defaultTimeout) {
        return new BlockingRegistry(newLoadingCacheGuiceWatchableRegistrationContainer(), defaultTimeout);
    }

    private class FirstSupplierProvider<T> implements org.javabits.yar.guice.FirstSupplierProvider<T> {
        private final Id<T> id;

        public FirstSupplierProvider(Id<T> id) {
            this.id = id;
        }

        @Nullable
        @Override
        public Supplier<T> get() {
            return BlockingRegistry.super.get(id);
        }
    }
}
