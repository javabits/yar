package org.javabits.yar;

/**
 * Root class for all yar registry exceptions.
 * This class is an unchecked exception.
 * Date: 5/17/13
 *
 * @author Romain Gilles
 */
public class RegistryException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RegistryException() {
    }

    public RegistryException(String message) {
        super(message);
    }

    public RegistryException(String message, Throwable cause) {
        super(message, cause);
    }

    public RegistryException(Throwable cause) {
        super(cause);
    }

    public RegistryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
