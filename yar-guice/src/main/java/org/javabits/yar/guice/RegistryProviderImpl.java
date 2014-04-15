package org.javabits.yar.guice;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Key;
import org.javabits.yar.BlockingSupplier;
import org.javabits.yar.BlockingSupplierRegistry;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.javabits.yar.InterruptedException.newInterruptedException;
import static org.javabits.yar.TimeoutException.newTimeoutException;
import static org.javabits.yar.guice.BlockingSupplierRegistryImpl.DEFAULT_TIMEOUT;
import static org.javabits.yar.guice.BlockingSupplierRegistryImpl.DEFAULT_TIME_UNIT;

/**
 * This class is an adapter between the {@link BlockingSupplier} interface and
 * the guice {@code Provider} interface.
 * It adapts to {@link BlockingSupplier} to a guice {@code Provider}.
 * The {@link #get()} can block or not depending the underlying strategy.
 * By default a blocking ({@code SynchronousStrategy} is used but you can
 * override it by calling the {@link #noWait()} method to get a non-blocking
 * strategy ({@code NoWaitStrategy}).
 * Date: 5/14/13
 * Time: 10:21 AM
 *
 * @author Romain Gilles
 */ //TODO introduce dynamic management through Watcher and regarding the ?scope? maybe
class RegistryProviderImpl<T> implements RegistryProvider<T> {

    private final Key<T> key;
    private BlockingSupplier<T> blockingSupplier;
    private Function<BlockingSupplier<T>, T> supplierGetStrategy;
    private final long timeout;
    private final TimeUnit timeUnit;

    RegistryProviderImpl(Key<T> key) {
        this.key = key;
        this.timeout = DEFAULT_TIMEOUT;
        this.timeUnit = DEFAULT_TIME_UNIT;
        supplierGetStrategy = new DefaultSynchronousStrategy();
    }

    RegistryProviderImpl(Key<T> key, long timeout, TimeUnit timeUnit) {
        this.key = key;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        supplierGetStrategy = new SynchronousStrategy(timeout, timeUnit);
    }

    Key<T> key() {
        return key;
    }

    @Override
    public T get() {
        return supplierGetStrategy.apply(blockingSupplier);
    }

    @Inject
    public void setRegistry(BlockingSupplierRegistry registry) {
        // directly provision the targeted blocking supplier
        blockingSupplier = registry.get(GuiceId.of(key));
    }

    @Override
    public void noWait() {
        supplierGetStrategy = new NoWaitStrategy();
    }

    private class NoWaitStrategy implements Function<BlockingSupplier<T>, T> {

        @Nullable
        @Override
        public T apply(BlockingSupplier<T> supplier) {
            return supplier.get();
        }
    }

    private class DefaultSynchronousStrategy implements Function<BlockingSupplier<T>, T> {

        @Nullable
        @Override
        public T apply(BlockingSupplier<T> supplier) {
            try {
                Preconditions.checkNotNull(blockingSupplier, "blockingSupplier");
                return supplier.getSync(blockingSupplier.defaultTimeout()
                        , blockingSupplier.defaultTimeUnit());
            } catch (InterruptedException e) {
                throw newInterruptedException(e);
            } catch (TimeoutException e) {
                throw newTimeoutException(timeout, timeUnit, e);
            }
        }
    }

    private class SynchronousStrategy implements Function<BlockingSupplier<T>, T> {

        private final long timeout;
        private final TimeUnit timeUnit;

        private SynchronousStrategy(long timeout, TimeUnit timeUnit) {
            this.timeout = timeout;
            this.timeUnit = timeUnit;
        }

        @Nullable
        @Override
        public T apply(BlockingSupplier<T> supplier) {
            try {
                return supplier.getSync(timeout, timeUnit);
            } catch (InterruptedException e) {
                throw newInterruptedException(e);
            } catch (TimeoutException e) {
                throw newTimeoutException(timeout, timeUnit, e);
            }
        }
    }
}
