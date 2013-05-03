package org.javabits.yar.guice;

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
class GuavaSupplierAdapter<T> implements Supplier<T> {
    private final com.google.common.base.Supplier<T> delegate;

    GuavaSupplierAdapter(com.google.common.base.Supplier<T> delegate) {
        this.delegate = requireNonNull(delegate, "delegate");
    }

    @Nullable
    @Override
    public T get() {
        return delegate.get();
    }
}
