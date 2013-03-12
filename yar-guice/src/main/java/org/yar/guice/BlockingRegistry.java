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

import org.yar.Key;
import org.yar.Supplier;
import org.yar.Watcher;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;
import static org.yar.guice.GuiceWatchableRegistrationContainer.newLoadingCacheGuiceWatchableRegistrationContainer;
import static org.yar.guice.GuiceWatchableRegistrationContainer.newMultimapGuiceWatchableRegistrationContainer;

/**
 * TODO comment
 * Date: 2/25/13
 * Time: 10:30 PM
 *
 * @author Romain Gilles
 */
public class BlockingRegistry extends SimpleRegistry implements org.yar.BlockingRegistry {

    public static final long DEFAULT_TIMEOUT = 0L;

    private final long defaultTimeout;

    public BlockingRegistry() {
        this(DEFAULT_TIMEOUT);
    }

    public BlockingRegistry(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public BlockingRegistry(WatchableRegistrationContainer registrationContainer, long defaultTimeout) {
        super(registrationContainer);
        this.defaultTimeout = defaultTimeout;
    }


    @Nullable
    @Override
    public <T> Supplier<T> get(Key<T> key) {
        return createSupplier(key, super.get(key), defaultTimeout, TimeUnit.MILLISECONDS);
    }

    @Nullable
    @Override
    public <T> Supplier<T> get(Class<T> type, long timeout, TimeUnit unit) {
        return get(GuiceKey.of(type), timeout, unit);
    }

    @Nullable
    @Override
    public <T> Supplier<T> get(Key<T> key, long timeout, TimeUnit unit) {
        return createSupplier(key, super.get(key), timeout, unit);
    }


    private <T> Supplier<T> createSupplier(Key<T> key, Supplier<T> originalValue, long timeout, TimeUnit unit) {
        if (timeout == 0) { //direct
            return originalValue;
        } else if (timeout < 0) {
            return new InfiniteBlockingSupplier<>(key, originalValue);
        } else {
            return new TimeoutBlockingSupplier<>(key, originalValue, timeout, unit);
        }
    }

    abstract class AbstractBlockingSupplier<T> extends org.yar.guice.AbstractBlockingSupplier<T> {

        AbstractBlockingSupplier(Key<T> key, Supplier<T> delegate) {
            super(delegate);
            addWatcher(key, this);
        }
    }

    class TimeoutBlockingSupplier<T> extends AbstractBlockingSupplier<T> {
        private final long timeout;
        private final TimeUnit unit;

        TimeoutBlockingSupplier(Key<T> key, Supplier<T> delegate, long timeout, TimeUnit unit) {
            super(key, delegate);
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
        InfiniteBlockingSupplier(Key<T> key, Supplier<T> delegate) {
            super(key, delegate);
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

    static BlockingRegistry newMultimapRegistry(long defaultTimeout) {
        return new BlockingRegistry(newMultimapGuiceWatchableRegistrationContainer(), defaultTimeout);
    }
    static BlockingRegistry newLoadingCacheRegistry(long defaultTimeout) {
        return new BlockingRegistry(newLoadingCacheGuiceWatchableRegistrationContainer(), defaultTimeout);
    }
}
