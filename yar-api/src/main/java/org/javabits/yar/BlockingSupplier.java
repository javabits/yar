package org.javabits.yar;

import javax.annotation.Nullable;
import java.lang.InterruptedException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Date: 2/28/13
 *
 * @author Romain Gilles
 */
public interface BlockingSupplier<T> extends Supplier<T> {
    /**
     * Retrieves the instance of {@code T} from the registry.
     * <p>
     * This is a non-blocking call.
     *
     * @return the instance of {@code T}, or <code>null</code> if no corresponding registration.
     */
    @Nullable
    @Override
    T get();

    /**
     * Retrieves the instance of {@code T} from the registry. If no {@code T} registered,
     * the call will block until the service is registered or the thread is interrupted.
     *
     * @return the instance of {@code T}.
     * @throws InterruptedException if the thread was interrupted while waiting.
     */
    T getSync() throws InterruptedException;

    /**
     * Retrieves the instance of {@code T} from the registry. If no {@code T} registered,
     * the call will block until the service is registered or the thread is interrupted, or the
     * timeout has expired.
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return the instance of {@code T}.
     * @throws InterruptedException if the thread was interrupted while waiting.
     * @throws TimeoutException     if the wait timed out.
     */
    T getSync(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;

    /**
     * Retrieves the instance of {@code T} from the registry asynchronously. The caller will use
     * the returned future to obtain the instance of the service once it gets registered available.
     *
     * @return an instance of future result.
     */
    CompletableFuture<T> getAsync();

    /**
     * Returns the default timeout used for blocking operations.
     * The associated time unit is provided by {@link #defaultTimeUnit()}.
     *
     * @return default timeout used for blocking operations.
     * @see #defaultTimeUnit()
     */
    long defaultTimeout();

    /**
     * Returns the default time unit used for blocking operations.
     * The associated timeout is provided by {@link #defaultTimeout()}.
     *
     * @return default timeout used for blocking operations.
     * @see #defaultTimeout()
     */
    TimeUnit defaultTimeUnit();

}
