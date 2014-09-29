package org.javabits.yar.guice;

import static com.google.common.util.concurrent.Futures.addCallback;
import static org.javabits.yar.Registry.DEFAULT_TIMEOUT;
import static org.javabits.yar.Registry.DEFAULT_TIME_UNIT;

import java.lang.InterruptedException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import com.google.common.base.*;
import org.javabits.yar.*;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.javabits.yar.Supplier;

/**
 * @author Romain Gilles Date: 5/31/13 Time: 2:08 PM
 */
public class NoWaitBlockingSupplier<T> implements BlockingSupplier<T>, SupplierListener, SupplierWrapper<T> {

    private final Id<T> id;
    private final AtomicReference<Supplier<T>> supplierReference;
    private final AtomicReference<SettableFuture<Supplier<T>>> supplierFutureRef;

    public NoWaitBlockingSupplier(Id<T> id, final Supplier<T> supplier) {
        this.id = id;
        supplierReference = new AtomicReference<>(supplier);
        supplierFutureRef = new AtomicReference<>();
        SettableFuture<Supplier<T>> settableFuture = SettableFuture.create();
        supplierFutureRef.set(settableFuture);
        if (supplier != null) {
            settableFuture.set(supplier);
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
    public ListenableFuture<T> getAsync() {
        final SettableFuture<T> future = SettableFuture.create();
        // The future callback will be executed either on the current thread (if the future is
        // already completed) or on the registry's action handler thread.
        addCallback(supplierFutureRef.get(), new FutureCallback<Supplier<T>>() {
            @Override
            public void onSuccess(Supplier<T> supplier) {
                future.set(supplier.get());
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
        return future;
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

    @Override
    public void supplierChanged(SupplierEvent supplierEvent) {
        SupplierEvent.Type type = supplierEvent.type();
        Supplier<T> supplier = (Supplier<T>) supplierEvent.supplier();
        switch (type) {
        case ADD:
            if (supplierReference.compareAndSet(null, supplier)) {
                supplierFutureRef.get().set(supplier);
            }
            break;
        case REMOVE:
            if (supplierReference.compareAndSet(supplier, null)) {
                supplierFutureRef.set(SettableFuture.<Supplier<T>> create());
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
