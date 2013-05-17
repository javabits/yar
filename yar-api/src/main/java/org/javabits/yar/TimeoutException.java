package org.javabits.yar;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

/**
 * This class is a unchecked wrapper of the {@link java.util.concurrent.TimeoutException}.
 * <p><b>From {@link java.util.concurrent.TimeoutException} documentation:</b>
 * Exception thrown when a blocking operation times out.  Blocking
 * operations for which a timeout is specified need a means to
 * indicate that the timeout has occurred. For many such operations it
 * is possible to return a value that indicates timeout; when that is
 * not possible or desirable then <tt>TimeoutException</tt> should be
 * declared and thrown.
 * </p>
 * Date: 5/17/13
 * @author Romain Gilles
 * @see java.util.concurrent.TimeoutException
 */
public class TimeoutException extends ConcurrentException {
    private static final long serialVersionUID = 1L;

    @Nullable
    private final Long timeout;
    @Nullable
    private final TimeUnit unit;

    /**
     * Constructs a <tt>TimeoutException</tt> with no specified detail
     * message.
     *
     * @param cause the original {@code TimeoutException}
     */
    public TimeoutException(java.util.concurrent.TimeoutException cause) {
        super(cause);
        this.timeout = null;
        this.unit = null;
    }

    /**
     * Constructs a <tt>TimeoutException</tt> with the specified detail
     * message.
     *
     * @param message the detail message
     * @param cause   the original {@code TimeoutException}
     */
    public TimeoutException(String message, java.util.concurrent.TimeoutException cause) {
        super(message, cause);
        this.timeout = null;
        this.unit = null;
    }

    /**
     * Constructs a <tt>TimeoutException</tt> with the specified detail
     * timeout value.
     *
     * @param timeout the maximum time to wait.
     * @param unit    the time unit of the timeout argument
     * @param cause   the original {@code TimeoutException}
     */
    public TimeoutException(long timeout, TimeUnit unit, java.util.concurrent.TimeoutException cause) {
        super(getTimeoutMessage(timeout, unit), cause);
        this.timeout = timeout;
        this.unit = unit;
    }

    /**
     * Get the maximum time to wait
     * @return wait time
     */
    @Nullable
    public Long getTimeout() {
        return timeout;
    }

    /**
     * Get the time unit of the timeout.
     * @return time unit
     */
    @Nullable
    public TimeUnit getUnit() {
        return unit;
    }

    /**
     * Utility method that produce the message of the timeout.
     * @param timeout the maximum time to wait.
     * @param unit    the time unit of the timeout argument
     * @return formatted string that contains the timeout information.
     */
    public static String getTimeoutMessage(long timeout, TimeUnit unit) {
        return String.format("Timeout of %d %s reached", timeout, requireNonNull(unit, "unit"));
    }

    /**
     * Constructs a <tt>TimeoutException</tt> with the specified detail
     * timeout value.
     *
     * @param timeout the maximum time to wait.
     * @param unit    the time unit of the timeout argument
     * @param cause   the original {@code TimeoutException}
     */
    public static TimeoutException newTimeoutException(long timeout, TimeUnit unit, java.util.concurrent.TimeoutException cause) {
        return new TimeoutException(timeout, unit, cause);
    }

    /**
     * Constructs a <tt>TimeoutException</tt> with the specified detail
     * message.
     *
     * @param message the detail message
     * @param cause   the original {@code TimeoutException}
     */
    public static TimeoutException newTimeoutException(String message, java.util.concurrent.TimeoutException cause) {
        return new TimeoutException(message, cause);
    }

    /**
     * Constructs a <tt>TimeoutException</tt> with no specified detail
     * message.
     *
     * @param cause the original {@code TimeoutException}
     */
    public TimeoutException newTimeoutException(java.util.concurrent.TimeoutException cause) {
        return new TimeoutException(cause);
    }

}
