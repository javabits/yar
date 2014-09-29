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
class GuavaSupplierAdapter<T> implements Supplier<T>, SupplierWrapper<T> {
    private final com.google.common.base.Supplier<T> delegate;
    private final Id<T> id;

    GuavaSupplierAdapter(Id<T> id, com.google.common.base.Supplier<? extends T> delegate) {
        this.id = requireNonNull(id, "id");
        this.delegate = (com.google.common.base.Supplier<T>) requireNonNull(delegate, "delegate");
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
        return "GuavaSupplierAdapter{" +
                "delegate=" + delegate +
                '}';
    }

    @Nullable
    @Override
    public com.google.common.base.Supplier<T> getNativeSupplier() {
        return delegate;
    }

    @Override
    public com.google.common.base.Supplier<T> getWrapped() {
        return getNativeSupplier();
    }
}
