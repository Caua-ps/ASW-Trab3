package lob.quadtree;

import lob.physics.Vector2D;
import java.util.Set;

/**
 * Abstract <i>trie</i> &mdash; the recursive building block of the quadtree.
 *
 * <p>This is the {@code component} role of the <b>Composite</b> design
 * pattern, as described in the project's FAQ on quadtrees:
 * <ul>
 *   <li>the leaf is {@link LeafTrie} &mdash; it stores up to
 *       {@link #capacity} elements directly;</li>
 *   <li>the composite is {@link NodeTrie} &mdash; it routes operations to
 *       four child tries, one per quadrant.</li>
 * </ul>
 * The client ({@link PointQuadtree}) only ever talks to a {@code Trie}
 * reference and is therefore oblivious to whether the current node is a
 * leaf or an internal node. When a leaf overflows, {@code insert} returns
 * a freshly built {@link NodeTrie} that the parent splices into its place.
 *
 * <p>The bucket {@link #capacity} is shared by all leaves and configurable
 * with {@link #setCapacity(int)} &mdash; the FAQ explicitly mentions that
 * the unit tests need to override this default with a {@code @BeforeClass}
 * setter.
 *
 * @param <T> the element type, which must expose {@link HasPoint#x()} and
 *            {@link HasPoint#y()}.
 */
public abstract class Trie<T extends HasPoint> {

    /** Default bucket capacity shared by every leaf. */
    protected static int capacity = 10;

    /**
     * Sets the bucket capacity used by future {@link LeafTrie} instances.
     * Called by tests inside an {@code @BeforeAll}/{@code @BeforeClass} hook
     * to control when leaves split.
     *
     * @param cap the new capacity (must be positive).
     */
    public static void setCapacity(int cap) { capacity = cap; }

    /** @return the current bucket capacity. */
    public static int getCapacity() { return capacity; }

    /**
     * Inserts an element. May return {@code this} (the leaf still has room),
     * a brand-new {@link NodeTrie} (the leaf overflowed and was split), or
     * one of the four sub-tries of an internal node.
     *
     * <p><b>Important:</b> the caller must always replace its reference with
     * the returned value, otherwise structural changes during a split will
     * be lost.
     *
     * @param element the element to insert.
     * @return the trie that should occupy this slot afterwards.
     */
    public abstract Trie<T> insert(T element);

    /**
     * Looks up an element with the same coordinates as {@code element}.
     *
     * @param element a probe carrying the coordinates to look up.
     * @return the stored element with matching coordinates, or {@code null}.
     */
    public abstract T find(T element);

    /**
     * Inserts {@code element}, replacing any element already stored at the
     * exact same {@code (x, y)} coordinates.
     *
     * @return the trie that should occupy this slot afterwards.
     */
    public abstract Trie<T> insertReplace(T element);

    /**
     * Removes the element with the same coordinates as {@code element}, if
     * any. The structure is not collapsed back &mdash; deleting from a
     * {@link NodeTrie} simply leaves an empty {@link LeafTrie} below.
     *
     * @return the trie that should occupy this slot afterwards.
     */
    public abstract Trie<T> delete(T element);

    /**
     * Adds to {@code found} every stored element within {@code radius} of
     * the point {@code (x, y)}. Implementations short-circuit using
     * bounding-box pruning before doing the per-element distance check.
     *
     * @param x      query centre x.
     * @param y      query centre y.
     * @param radius search radius.
     * @param found  set to which matching elements are added.
     */
    public abstract void collectNear(double x, double y, double radius, Set<T> found);

    /**
     * Helper computing the Euclidean distance between two points.
     *
     * @return {@code sqrt((x1-x2)² + (y1-y2)²)}.
     */
    public static double getDistance(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * {@link Vector2D}-flavoured overload of {@link #collectNear}. Kept for
     * compatibility with code that already manipulates positions as
     * vectors.
     *
     * @param center query centre as a vector.
     * @param radius search radius.
     * @param found  set to which matching elements are added.
     */
    public abstract void collectPoints(Vector2D center, double radius, Set<T> found);
}
