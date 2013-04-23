package org.yaor;

import com.google.common.util.concurrent.ListenableFuture;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

/**
 * Date: 2/28/13
 * Time: 10:51 AM
 *
 * @author Romain Gilles
 */
public interface BlockingSupplier<T> extends Supplier<T> {
    @Nullable
    @Override
    T get();

    @Nullable
    T get(long timeout, TimeUnit unit);

    ListenableFuture<T> getAsynch();
}
