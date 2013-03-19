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

import com.google.inject.matcher.Matchers;
import org.yar.BlockingSupplier;
import org.yar.Key;
import org.yar.KeyMatchers;
import org.yar.Supplier;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

import static org.yar.KeyMatchers.newKeyMatcher;
import static org.yar.guice.GuiceWatchableRegistrationContainer.newLoadingCacheGuiceWatchableRegistrationContainer;
import static org.yar.guice.GuiceWatchableRegistrationContainer.newMultimapGuiceWatchableRegistrationContainer;

/**
 * TODO comment
 * Date: 2/28/13
 * Time: 10:57 AM
 *
 * @author Romain Gilles
 */
public class BlockingSupplierRegistry extends SimpleRegistry implements org.yar.BlockingSupplierRegistry {
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
        return get(GuiceKey.of(type));
    }

    @Override
    public <T> BlockingSupplier<T> get(Key<T> key) {
        return new TimeoutBlockingSupplier<>(key, super.get(key), defaultTimeout, defaultTimeUnit);
    }

    class TimeoutBlockingSupplier<T> extends AbstractBlockingSupplier<T> implements BlockingSupplier<T> {
        private final long timeout;
        private final TimeUnit unit;

        TimeoutBlockingSupplier(Key<T> key, Supplier<T> delegate, long timeout, TimeUnit unit) {
            super(delegate);
            this.timeout = timeout;
            this.unit = unit;
            addWatcher(newKeyMatcher(key), this);
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
                }
            } finally {
                readLock.unlock();
            }
            if (timeout < 0L) {
                return infiniteBlockingGet();
            } else if (timeout == 0L) {
                if (delegate != null) {
                    return delegate.get();
                }
                return null;
            } else {
                return timeoutBlockingGet(timeout, unit);
            }
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
        return newLoadingCacheBlockingSupplierRegistry(DEFAULT_TIMEOUT);
    }

    static BlockingSupplierRegistry newLoadingCacheBlockingSupplierRegistry(long defaultTimeout) {
        return new BlockingSupplierRegistry(newLoadingCacheGuiceWatchableRegistrationContainer(), defaultTimeout);
    }

}
