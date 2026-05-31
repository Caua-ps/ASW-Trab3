package lob.quadtree;

import lob.physics.Vector2D;
import java.util.Set;

/**
 * Internal node of the quadtree composite &mdash; routes operations to one
 * of four sub-tries, one per quadrant.
 *
 * <p>Quadrant layout, given the screen-style axes used by the project
 * (y grows downwards):
 * <pre>
 *           xMin --- xMid --- xMax
 *      yMin |  topLeft   |   topRight   |
 *      yMid +------------+--------------+
 *      yMax | bottomLeft |  bottomRight |
 * </pre>
 *
 * <p>Each child is initially a {@link LeafTrie} covering one quarter of this
 * node's rectangle. A child can later be replaced by another
 * {@link NodeTrie} when its bucket overflows &mdash; that is why each
 * mutation method reassigns the child to the result of the recursive call.
 *
 * @param <T> the element type, see {@link HasPoint}.
 */
public class NodeTrie<T extends HasPoint> extends Trie<T> {

    /** The four sub-tries, one per quadrant. */
    private Trie<T> topLeft, topRight, bottomLeft, bottomRight;

    /** Inclusive bounding rectangle of this node. */
    private final double xMin, xMax, yMin, yMax;

    /** Mid coordinates &mdash; precomputed because every routing decision uses them. */
    private final double xMid, yMid;

    /**
     * Creates a new internal node spanning the given rectangle, with four
     * empty leaves as children.
     */
    public NodeTrie(double xMin, double xMax, double yMin, double yMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;

        this.xMid = (xMin + xMax) / 2;
        this.yMid = (yMin + yMax) / 2;

        this.topLeft     = new LeafTrie<>(xMin, xMid, yMin, yMid);
        this.topRight    = new LeafTrie<>(xMid, xMax, yMin, yMid);
        this.bottomLeft  = new LeafTrie<>(xMin, xMid, yMid, yMax);
        this.bottomRight = new LeafTrie<>(xMid, xMax, yMid, yMax);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Selects the quadrant containing {@code element.x(), element.y()}
     * and delegates the insertion. The returned reference is reassigned so
     * that, if the child split, the parent keeps the new {@link NodeTrie}.
     * The node always returns {@code this} &mdash; internal nodes never
     * "promote" themselves.
     */
    @Override
    public Trie<T> insert(T element) {
        if (element.x() < xMid) {
            if (element.y() < yMid) topLeft     = topLeft.insert(element);
            else                    bottomLeft  = bottomLeft.insert(element);
        } else {
            if (element.y() < yMid) topRight    = topRight.insert(element);
            else                    bottomRight = bottomRight.insert(element);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Routes the lookup to the single quadrant that could possibly
     * contain {@code element.x(), element.y()} &mdash; this is the
     * logarithmic-time path the slides describe.
     */
    @Override
    public T find(T element) {
        if (element.x() < xMid) {
            if (element.y() < yMid) return topLeft.find(element);
            else                    return bottomLeft.find(element);
        } else {
            if (element.y() < yMid) return topRight.find(element);
            else                    return bottomRight.find(element);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Trie<T> insertReplace(T element) {
        if (element.x() < xMid) {
            if (element.y() < yMid) topLeft     = topLeft.insertReplace(element);
            else                    bottomLeft  = bottomLeft.insertReplace(element);
        } else {
            if (element.y() < yMid) topRight    = topRight.insertReplace(element);
            else                    bottomRight = bottomRight.insertReplace(element);
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Trie<T> delete(T element) {
        if (element.x() < xMid) {
            if (element.y() < yMid) topLeft     = topLeft.delete(element);
            else                    bottomLeft  = bottomLeft.delete(element);
        } else {
            if (element.y() < yMid) topRight    = topRight.delete(element);
            else                    bottomRight = bottomRight.delete(element);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Bounding-box prune first: if the search disk does not overlap this
     * node's rectangle, none of the four children can contribute and we
     * return immediately. Otherwise descend into all four &mdash; a search
     * disk can straddle quadrant boundaries.
     */
    @Override
    public void collectNear(double x, double y, double radius, Set<T> found) {
        if (x + radius < xMin || x - radius > xMax ||
            y + radius < yMin || y - radius > yMax) {
            return;
        }
        topLeft    .collectNear(x, y, radius, found);
        topRight   .collectNear(x, y, radius, found);
        bottomLeft .collectNear(x, y, radius, found);
        bottomRight.collectNear(x, y, radius, found);
    }

    /** {@link Vector2D}-flavoured overload of {@link #collectNear}. */
    @Override
    public void collectPoints(Vector2D center, double radius, Set<T> found) {
        if (center.getX() + radius < xMin || center.getX() - radius > xMax ||
            center.getY() + radius < yMin || center.getY() - radius > yMax) {
            return;
        }
        topLeft    .collectPoints(center, radius, found);
        topRight   .collectPoints(center, radius, found);
        bottomLeft .collectPoints(center, radius, found);
        bottomRight.collectPoints(center, radius, found);
    }
}
