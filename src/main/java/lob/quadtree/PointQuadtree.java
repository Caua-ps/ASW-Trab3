package lob.quadtree;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Façade for the quadtree data structure described in the project's slides
 * (<i>Quadtrees</i>). Wraps a {@link Trie} root and exposes the public API
 * the rest of the codebase ever needs: insert, lookup, delete, range query,
 * and iteration.
 *
 * <p>Two important practical points called out in the FAQ
 * (<i>FAQ: Pacote &laquo;quadtree&raquo;</i>):
 * <ul>
 *   <li>this class is intentionally non-abstract and accepts <i>any</i>
 *       rectangle orientation: the tests construct it as
 *       {@code new PointQuadtree(0, 0, width, height)} or as
 *       {@code new PointQuadtree(10, 20, 20, 10)}, depending on whether
 *       graphics-style (y down) or maths-style (y up) coordinates are
 *       being used. The constructor normalises the four numbers into
 *       {@code (xLow, yLow, xHigh, yHigh)};</li>
 *   <li>operations on the underlying trie return a (possibly different)
 *       trie reference because of split/replace, so the {@link #root} field
 *       is reassigned on every mutation.</li>
 * </ul>
 *
 * <p>Implements {@link Iterable} so a quadtree can be used directly in a
 * for-each loop &mdash; iteration order is unspecified.
 *
 * @param <T> the element type, must implement {@link HasPoint}.
 */
public class PointQuadtree<T extends HasPoint> implements Iterable<T> {

    /** Current root &mdash; replaced on every mutation that splits or
     *  rebuilds the underlying trie. */
    private Trie<T> root;

    /** Normalised bounding rectangle of the indexed area. */
    private final double xLow, yLow, xHigh, yHigh;

    /**
     * Creates an empty quadtree spanning the rectangle whose corners are
     * any two of the four supplied numbers. The constructor accepts any
     * orientation and stores the actual {@code min}/{@code max}.
     *
     * @param a one corner's x.
     * @param b one corner's y.
     * @param c the opposite corner's x.
     * @param d the opposite corner's y.
     */
    public PointQuadtree(double a, double b, double c, double d) {
        this.xLow  = Math.min(a, c);
        this.xHigh = Math.max(a, c);
        this.yLow  = Math.min(b, d);
        this.yHigh = Math.max(b, d);
        this.root  = new LeafTrie<>(xLow, xHigh, yLow, yHigh);
    }

    /**
     * Iterator over every element currently stored in the tree. Convenience
     * delegate to {@link #getAll()}.
     */
    @Override
    public Iterator<T> iterator() { return getAll().iterator(); }

    /**
     * Inserts an element.
     *
     * @throws PointOutOfBoundException if the element falls outside the
     *         tree's bounding rectangle.
     */
    public void insert(T element) {
        checkBoundaries(element);
        this.root = this.root.insert(element);
    }

    /**
     * Inserts an element, replacing any existing element at the same exact
     * coordinates.
     *
     * @throws PointOutOfBoundException if the element falls outside the
     *         tree's bounding rectangle.
     */
    public void insertReplace(T element) {
        checkBoundaries(element);
        this.root = this.root.insertReplace(element);
    }

    /**
     * Throws {@link PointOutOfBoundException} if {@code e} sits outside the
     * indexed rectangle.
     */
    private void checkBoundaries(T e) {
        if (e.x() < xLow || e.x() > xHigh || e.y() < yLow || e.y() > yHigh)
            throw new PointOutOfBoundException();
    }

    /**
     * Looks up an element with the same coordinates as {@code element}.
     *
     * @return the stored element, or {@code null}.
     */
    public T find(T element) { return root.find(element); }

    /** Removes the element with the same coordinates as {@code element}. */
    public void delete(T element) { this.root = this.root.delete(element); }

    /**
     * Range query &mdash; returns every stored element within {@code radius}
     * of the point {@code (x, y)}.
     */
    public Set<T> findNear(double x, double y, double radius) {
        Set<T> found = new HashSet<>();
        root.collectNear(x, y, radius, found);
        return found;
    }

    /** {@link lob.physics.Vector2D}-flavoured overload of {@link #findNear}. */
    public Set<T> find(lob.physics.Vector2D center, double radius) {
        Set<T> found = new HashSet<>();
        root.collectPoints(center, radius, found);
        return found;
    }

    /**
     * Returns every element currently stored in the tree.
     *
     * <p>Implementation trick: a range query centred on the rectangle's
     * centre and with radius equal to its longest side covers the whole
     * indexed area, so it lets us reuse {@link Trie#collectNear} instead
     * of writing a dedicated traversal.
     */
    public java.util.Collection<T> getAll() {
        Set<T> all = new HashSet<>();
        double cx     = (xLow  + xHigh) / 2;
        double cy     = (yLow  + yHigh) / 2;
        double radius = Math.max(xHigh - xLow, yHigh - yLow);
        root.collectNear(cx, cy, radius, all);
        return all;
    }
}
