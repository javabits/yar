package org.javabits.yar.guice;

import org.javabits.yar.Id;
import org.javabits.yar.Supplier;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * TODO comment
 * Date: 5/3/13
 * Time: 6:08 AM
 *
 * @author Romain Gilles
 */
class JavaSupplierAdapter<T> implements Supplier<T>, SupplierWrapper<T> {
    private final java.util.function.Supplier<T> delegate;
    private final Id<T> id;

    @SuppressWarnings("unchecked")
    JavaSupplierAdapter(Id<T> id, java.util.function.Supplier<? extends T> delegate) {
        this.id = requireNonNull(id, "id");
        this.delegate = (java.util.function.Supplier<T>) requireNonNull(delegate, "delegate");
    }

    @Override
    public Id<T> id() {
        return id;
    }

    @Nullable
    @Override
    public T get() {
        return delegate.get();
    }

    @Override
    public String toString() {
        return "JavaSupplierAdapter{" +
                "delegate=" + delegate +
                ", id=" + id +
                '}';
    }

    @Nullable
    @Override
    public java.util.function.Supplier<T> getNativeSupplier() {
        return delegate;
    }

    @Override
    public java.util.function.Supplier<T> getWrapped() {
        return getNativeSupplier();
    }
}
