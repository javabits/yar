package org.javabits.yar.guice;

import com.google.common.base.Function;
import com.google.inject.Inject;
import com.google.inject.Key;
import org.javabits.yar.BlockingSupplier;
import org.javabits.yar.BlockingSupplierRegistry;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Objects.requireNonNull;
import static org.javabits.yar.InterruptedException.newInterruptedException;
import static org.javabits.yar.TimeoutException.newTimeoutException;
import static org.javabits.yar.guice.BlockingSupplierRegistryImpl.DEFAULT_TIMEOUT;
import static org.javabits.yar.guice.BlockingSupplierRegistryImpl.DEFAULT_TIME_UNIT;

/**
 * TODO comment
 * Date: 5/14/13
 * Time: 10:21 AM
 *
 * @author Romain Gilles
 */ //TODO introduce dynamic management through Watcher and regarding the ?scope? maybe
class RegistryProviderImpl<T> implements RegistryProvider<T> {

    private final Key<T> key;
    private BlockingSupplierRegistry registry;
    private Function<BlockingSupplier<T>, T> supplierGetStrategy;
    private final long timeout;
    private final TimeUnit timeUnit;

    RegistryProviderImpl(Key<T> key) {
        this(key, DEFAULT_TIMEOUT, DEFAULT_TIME_UNIT);
    }

    RegistryProviderImpl(Key<T> key, long timeout, TimeUnit timeUnit) {
        this.key = key;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        supplierGetStrategy = new SynchronousStrategy();
    }

    Key<T> key() {
        return key;
    }

    @Override
    public T get() {
        BlockingSupplier<T> supplier = getSupplier();
        return supplierGetStrategy.apply(supplier);
    }

    private BlockingSupplier<T> getSupplier() {
        BlockingSupplier<T> supplier = registry().get(GuiceId.of(key));
        return supplier;
    }

    BlockingSupplierRegistry registry() {
        return requireNonNull(registry, "registry");
    }

    @Inject
    public void setRegistry(BlockingSupplierRegistry registry) {
        this.registry = registry;
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

    private class SynchronousStrategy implements Function<BlockingSupplier<T>, T> {
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
