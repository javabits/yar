package org.javabits.yar.guice;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.util.concurrent.Futures.addCallback;
import static com.google.common.util.concurrent.Futures.getUnchecked;

import java.lang.InterruptedException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;

import org.javabits.yar.*;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.javabits.yar.Supplier;

class BlockingSupplierImpl<T> implements BlockingSupplier<T>, SupplierListener, SupplierWrapper<T> {
    private final AtomicReference<SettableFuture<Supplier<T>>> supplierRef;
    private final Id<T> id;
    private final InternalRegistry registry;

    // preserve a reference to the registration to avoid garbage collection.
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private Registration<T> selfRegistration;

    BlockingSupplierImpl(Id<T> id, InternalRegistry registry) {
        this.id = checkNotNull(id, "id");
        this.registry = registry;
        this.supplierRef = new AtomicReference<>();
        initSupplierRef();
    }

    @Override
    public long defaultTimeout() {
        return registry.defaultTimeout();
    }

    @Override
    public TimeUnit defaultTimeUnit() {
        return registry.defaultTimeUnit();
    }


    @Override
    public Id<T> id() {
        return id;
    }

    @Nullable
    @Override
    public T get() {
        Future<Supplier<T>> future = supplierRef.get();
        // Do not block on Future.get() here. Just check if the future is done.
        if (future.isDone())
            return future.isCancelled() ? null : getUnchecked(future).get();
        return null;
    }

    @Override
    public T getSync() throws InterruptedException {
        try {
            return getAsync().get();
        } catch (ExecutionException e) {
            // getAsync() future can't fail (by design). If it did then there is something
            // horribly wrong with this code.
            throw new AssertionError(e);
        }
    }

    @Nullable
    @Override
    public T getSync(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        try {
            return getAsync().get(timeout, unit);
        } catch (ExecutionException e) {
            // getAsync() future can't fail (by design). If it did then there is something
            // horribly wrong with this code.
            throw new AssertionError(e);
        }
    }

    @Override
    public ListenableFuture<T> getAsync() {
        final SettableFuture<T> future = SettableFuture.create();

        // The future callback will be executed either on the current thread (if the future is
        // already completed) or on the registry's action handler thread.
        addCallback(supplierRef.get(), new FutureCallback<Supplier<T>>() {
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

    @SuppressWarnings("unchecked")
    @Override
    public void supplierChanged(SupplierEvent supplierEvent) {
        SupplierEvent.Type type = supplierEvent.type();
        Supplier<T> supplier = (Supplier<T>) supplierEvent.supplier();
        switch (type) {
        case ADD:
            supplierRef.get().set(supplier);
            break;
        case REMOVE:
            Future<Supplier<T>> future = supplierRef.get();
            // Do not block on Future.get() here. Just check if the future is done.
            if (future.isDone() && !future.isCancelled()) {
                Supplier<T> currentSupplier = getUnchecked(future);
                if (supplier.equals(currentSupplier)) {
                    initSupplierRef();
                }
            } else if (future.isCancelled()) {
                supplierRef.set(SettableFuture.<Supplier<T>>create());
            }
            // else nothing to do we preserve the previous one
            break;
        default:
            throw new IllegalStateException("Unknown supplier event: " + supplierEvent);
        }
    }

    @Nullable
    @Override
    public com.google.common.base.Supplier<T> getNativeSupplier() {
        Future<Supplier<T>> future = supplierRef.get();
        // Do not block on Future.get() here. Just check if the future is done.
        if (future.isDone())
            return future.isCancelled() ? null : getUnchecked(future);
        return null;

    }

    @Override
    public com.google.common.base.Supplier<T> getWrapped() {
        return getNativeSupplier();
    }

    //preserve a strong reference on the registration listener registration
    void setSelfRegistration(Registration<T> selfRegistration) {
        this.selfRegistration = selfRegistration;
    }

    @Override
    public String toString() {
        SettableFuture<Supplier<T>> delegateSupplier = supplierRef.get();
        return "BlockingSupplierImpl{" +
                "id=" + id +
                ", delegate=" + (delegateSupplier != null? delegateSupplier.getClass().getName():"null") +
                '}';
    }

    private void initSupplierRef() {
        SettableFuture<Supplier<T>> settableFuture = SettableFuture.create();
        supplierRef.set(settableFuture);
        Supplier<T> supplier = registry.getDirectly(id);
        if (supplier != null) {
            settableFuture.set(supplier);
        }
    }
}
