package org.javabits.yar.guice;

import org.javabits.yar.*;

import javax.annotation.Nullable;
import java.lang.InterruptedException;
import java.util.concurrent.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.util.concurrent.Futures.getUnchecked;
import static java.util.concurrent.CompletableFuture.completedFuture;

class BlockingSupplierImpl<T> implements BlockingSupplier<T>, SupplierListener, SupplierWrapper<T> {
    private final AtomicReference<CompletableFuture<Supplier<T>>> supplierRef;
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
    public CompletableFuture<T> getAsync() {
        return supplierRef.get().thenApply(Supplier::get);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void supplierChanged(SupplierEvent supplierEvent) {
        SupplierEvent.Type type = supplierEvent.type();
        Supplier<T> supplier = (Supplier<T>) supplierEvent.supplier();
        switch (type) {
            case ADD:
                CompletableFuture<Supplier<T>> completableFuture = supplierRef.get();
                if (!completableFuture.isDone()) {
                    completableFuture.complete(supplier);
                }
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
                    supplierRef.set(new CompletableFuture<>());
                }
                // else nothing to do we preserve the previous one
                break;
            default:
                throw new IllegalStateException("Unknown supplier event: " + supplierEvent);
        }
    }

    @Nullable
    @Override
    public java.util.function.Supplier<T> getNativeSupplier() {
        Future<Supplier<T>> future = supplierRef.get();
        // Do not block on Future.get() here. Just check if the future is done.
        if (future.isDone())
            return future.isCancelled() ? null : getUnchecked(future);
        return null;

    }

    @Override
    public java.util.function.Supplier<T> getWrapped() {
        return getNativeSupplier();
    }

    //preserve a strong reference on the registration listener registration
    void setSelfRegistration(Registration<T> selfRegistration) {
        this.selfRegistration = selfRegistration;
    }

    @Override
    public String toString() {
        CompletableFuture<Supplier<T>> delegateSupplier = supplierRef.get();
        return "BlockingSupplierImpl{" +
                "id=" + id +
                ", delegate=" + (delegateSupplier != null ? delegateSupplier.getClass().getName() : "null") +
                '}';
    }

    private void initSupplierRef() {
        Supplier<T> supplier = registry.getDirectly(id);
        initSupplierRef(supplier);
    }

    private void initSupplierRef(@Nullable Supplier<T> supplier) {
        if (supplier != null) {
            supplierRef.set(completedFuture(supplier));
        } else {
            supplierRef.set(new CompletableFuture<>());
        }
    }
}
