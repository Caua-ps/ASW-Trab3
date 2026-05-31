package lob.quadtree;

/**
 * Unchecked exception thrown by {@link PointQuadtree#insert} (and its
 * variants) when an element falls outside the bounding box the tree was
 * created with.
 *
 * <p>It extends {@link RuntimeException} on purpose: per the FAQ, exceptions
 * that signal a programmer error (an out-of-bounds insertion is one) do not
 * need to be acknowledged with a {@code throws} clause.
 */
public class PointOutOfBoundException extends RuntimeException {

    /** Creates the exception with the default Portuguese error message. */
    public PointOutOfBoundException() {
        super("Ponto fora dos limites!");
    }
}
