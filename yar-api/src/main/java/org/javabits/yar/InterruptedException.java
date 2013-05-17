package org.javabits.yar;

/**
 * This class is a unchecked wrapper of the {@link java.lang.InterruptedException}.
 * <p><b>From {@link java.lang.InterruptedException} documentation:</b>
 * Thrown when a thread is waiting, sleeping, or otherwise occupied,
 * and the thread is interrupted, either before or during the activity.
 * Occasionally a method may wish to test whether the current
 * thread has been interrupted, and if so, to immediately throw
 * this exception.  The following code can be used to achieve
 * this effect:
 * <pre>
 *  if (Thread.interrupted())  // Clears interrupted status!
 *      throw new InterruptedException();
 * </pre>
 * </p>
 * Date: 5/17/13
 *
 * @author Romain Gilles
 * @see java.lang.InterruptedException
 */
public class InterruptedException extends ConcurrentException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an <code>InterruptedException</code> with no detail  message.
     *
     * @param cause original {@code InterruptedException
     */
    public InterruptedException(java.lang.InterruptedException cause) {
        super(cause);
    }

    /**
     * Constructs an <code>InterruptedException</code> with the
     * specified detail message.
     *
     * @param message the detail message.
     * @param cause   original {@code InterruptedException}
     */
    public InterruptedException(String message, java.lang.InterruptedException cause) {
        super(message, cause);
    }

    /**
     * Re-interrupt the current thread and constructs an <code>InterruptedException</code>
     * with the specified detail message.
     *
     * @param message the detail message.
     * @param cause   original {@code InterruptedException}
     */
    public static InterruptedException newInterruptedException(String message, java.lang.InterruptedException cause) {
        Thread.currentThread().interrupt();
        return new InterruptedException(message, cause);
    }

    /**
     * Re-interrupt the current thread and constructs an <code>InterruptedException</code>
     * with no detail  message.
     *
     * @param cause original {@code InterruptedException
     */
    public static InterruptedException newInterruptedException(java.lang.InterruptedException cause) {
        Thread.currentThread().interrupt();
        return new InterruptedException(cause);
    }
}
