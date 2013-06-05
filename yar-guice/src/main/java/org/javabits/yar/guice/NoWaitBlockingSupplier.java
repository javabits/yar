package org.javabits.yar.guice;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.javabits.yar.*;

import javax.annotation.Nullable;
import java.lang.InterruptedException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.util.concurrent.Futures.addCallback;
import static com.google.common.util.concurrent.Futures.getUnchecked;

/**
 * @author Romain Gilles
 *         Date: 5/31/13
 *         Time: 2:08 PM
 */
public class NoWaitBlockingSupplier<T> extends BlockingSupplierImpl<T> {

    public NoWaitBlockingSupplier(Id<T> id, Supplier<T> supplier) {
        super(id, supplier);
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
    public String toString() {
        return "NoWaitBlockingSupplier{" +
                "super=" + super.toString() +
                '}';
    }
}
