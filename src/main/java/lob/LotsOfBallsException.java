package lob;

/**
 * Checked exception thrown by every operation in the LotsOfBalls project that
 * can fail at runtime for a domain reason &mdash; class loading errors,
 * invalid game configurations, out-of-bounds quadtree insertions, missing
 * resources, and so on.
 *
 * <p>It extends {@link Exception} (not {@link RuntimeException}) on purpose:
 * the project's FAQ (section <i>Codificação</i>) states that recoverable error
 * conditions detected by the API should be wrapped in
 * {@code LotsOfBallsException} so callers are forced to acknowledge them with
 * {@code throws} or {@code try/catch}. Unchecked exceptions raised by the JDK
 * (such as {@link NullPointerException} or {@link ArrayIndexOutOfBoundsException})
 * are <i>not</i> wrapped &mdash; they indicate programmer errors and bubble up
 * untouched.
 */
public class LotsOfBallsException extends Exception {

    /** Creates an empty exception with no message and no cause. */
    public LotsOfBallsException() {
        super();
    }

    /**
     * Creates an exception carrying a human-readable message.
     *
     * @param message description of what went wrong.
     */
    public LotsOfBallsException(String message) {
        super(message);
    }

    /**
     * Creates an exception that wraps another throwable while adding context.
     *
     * @param message description of what went wrong, in the project's domain.
     * @param cause   the underlying low-level exception (e.g.
     *                {@link ClassNotFoundException}, {@link java.io.IOException}).
     */
    public LotsOfBallsException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates an exception that simply re-throws another throwable as a
     * domain-level error. Prefer {@link #LotsOfBallsException(String, Throwable)}
     * whenever a meaningful message can be supplied.
     *
     * @param cause the underlying exception.
     */
    public LotsOfBallsException(Throwable cause) {
        super(cause);
    }

    /**
     * Full form &mdash; lets callers (or subclasses) opt out of suppression
     * or of writable stack traces, matching the standard
     * {@link Exception#Exception(String, Throwable, boolean, boolean)} contract.
     *
     * @param message            description of what went wrong.
     * @param cause              the underlying exception, or {@code null}.
     * @param enableSuppression  whether suppression is enabled.
     * @param writableStackTrace whether the stack trace should be writable.
     */
    public LotsOfBallsException(String message, Throwable cause,
                                boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
