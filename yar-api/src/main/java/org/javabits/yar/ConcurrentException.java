package org.javabits.yar;

/**
 * Root class for all concurrency related exceptions
 * Date: 5/17/13
 *
 * @author Romain Gilles
 */
public class ConcurrentException extends RegistryException {
    public ConcurrentException() {
    }

    public ConcurrentException(String message) {
        super(message);
    }

    public ConcurrentException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConcurrentException(Throwable cause) {
        super(cause);
    }

    public ConcurrentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
