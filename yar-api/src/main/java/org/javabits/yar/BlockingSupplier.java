package org.javabits.yar;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nullable;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Date: 2/28/13 Time: 10:51 AM
 *
 * @author Romain Gilles
 */
public interface BlockingSupplier<T> extends Supplier<T> {
    /**
     * Retrieves the instance of the service from the registry.
     * <p>
     * This is a non-blocking call.
     *
     * @return the instance of the service, or <code>null</code> if the services is not registered.
     */
    @Nullable
    @Override
    T get();

    /**
     * Retrieves the instance of the service from the registry. If the service is not registered,
     * the call will block until the service is registered or the thread is interrupted.
     *
     * @return the instance of the service.
     * @throws InterruptedException
     *             if the thread was interrupted while waiting.
     */
    T getSync() throws InterruptedException;

    /**
     * Retrieves the instance of the service from the registry. If the service is not registered,
     * the call will block until the service is registered or the thread is interrupted, or the
     * timeout has expired.
     *
     * @param timeout
     *            the maximum time to wait
     * @param unit
     *            the time unit of the timeout argument
     * @return the instance of the service.
     * @throws InterruptedException
     *             if the thread was interrupted while waiting.
     * @throws TimeoutException
     *             if the wait timed out.
     */
    T getSync(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;

    /**
     * Retrieves the instance of the service from the registry asynchronously. The caller will use
     * the returned future to obtain the instance of the service once it gets registered available.
     *
     * @return an instance of future result.
     */
    ListenableFuture<T> getAsync();
}
