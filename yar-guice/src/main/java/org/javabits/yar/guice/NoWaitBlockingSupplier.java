package org.javabits.yar.guice;

import com.google.common.util.concurrent.SettableFuture;
import org.javabits.yar.*;

import javax.annotation.Nullable;
import java.lang.InterruptedException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.javabits.yar.Registry.DEFAULT_TIMEOUT;
import static org.javabits.yar.Registry.DEFAULT_TIME_UNIT;

/**
 * @author Romain Gilles Date: 5/31/13 Time: 2:08 PM
 */
public class NoWaitBlockingSupplier<T> implements BlockingSupplier<T>, SupplierListener, SupplierWrapper<T> {

    private final Id<T> id;
    private final AtomicReference<Supplier<T>> supplierReference;
    private final AtomicReference<CompletableFuture<Supplier<T>>> supplierFutureRef;

    public NoWaitBlockingSupplier(Id<T> id, final Supplier<T> supplier) {
        this.id = id;
        supplierReference = new AtomicReference<>(supplier);
        supplierFutureRef = new AtomicReference<>();
        if (supplier != null) {
            supplierFutureRef.set(CompletableFuture.completedFuture(supplier));
        } else {
            supplierFutureRef.set(new CompletableFuture<>());
        }
    }

    @Nullable
    @Override
    public T get() {
        Supplier<T> supplier = supplierReference.get();
        return supplier != null ? supplier.get() : null;
    }

    @Override
    public T getSync() throws InterruptedException {
        return get();
    }

    @Override
    public T getSync(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        return get();
    }

    @Override
    public CompletableFuture<T> getAsync() {
        return supplierFutureRef.get().thenApply(Supplier::get);
    }

    @Override
    public long defaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    @Override
    public TimeUnit defaultTimeUnit() {
        return DEFAULT_TIME_UNIT;
    }

    @Override
    public Id<T> id() {
        return id;
    }

    @Nullable
    @Override
    public com.google.common.base.Supplier<T> getNativeSupplier() {
        return supplierReference.get();
    }

    /**
     * there no thread safety issue because the supplierFutureRef is only used for the
     * asynchronous approach. The risk is small enough to avoid to introduce more complexity.
     */
    @Override
    public void supplierChanged(SupplierEvent supplierEvent) {
        SupplierEvent.Type type = supplierEvent.type();
        @SuppressWarnings("unchecked")
        Supplier<T> supplier = (Supplier<T>) supplierEvent.supplier();
        switch (type) {
            case ADD:
                if (supplierReference.compareAndSet(null, supplier)) {
                    supplierFutureRef.get().complete(supplier);
                }
                break;
            case REMOVE:
                if (supplierReference.compareAndSet(supplier, null)) {
                    supplierFutureRef.set(new CompletableFuture<>());
                }
                break;
            default:
                throw new IllegalStateException("Unknown supplier event: " + supplierEvent);
        }
    }

    @Override
    public com.google.common.base.Supplier<T> getWrapped() {
        return getNativeSupplier();
    }
}
