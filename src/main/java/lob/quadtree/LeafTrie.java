package lob.quadtree;

import lob.physics.Vector2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Leaf node of the quadtree composite &mdash; stores up to
 * {@link Trie#getCapacity()} elements directly in an {@link ArrayList}.
 *
 * <p>When the bucket is full, the next {@link #insert(HasPoint)} replaces
 * this leaf with a freshly built {@link NodeTrie} that spans the same
 * rectangle. This is the <i>split</i> step described in the slides
 * (<i>Quadtrees / Estrutura de dados</i>): every existing element is
 * re-inserted into the new node, then the new element follows.
 *
 * <p>The leaf is bounded by an axis-aligned rectangle
 * {@code [xMin, xMax] × [yMin, yMax]}. The rectangle is used for
 * bounding-box pruning during {@link #collectNear} queries: if the search
 * disk does not intersect the rectangle, the leaf is skipped entirely.
 *
 * @param <T> the element type, see {@link HasPoint}.
 */
public class LeafTrie<T extends HasPoint> extends Trie<T> {

    /** Elements currently stored in this leaf. */
    private final List<T> elements = new ArrayList<>();

    /** Inclusive bounding rectangle of this leaf. */
    private final double xMin, xMax, yMin, yMax;

    /**
     * Creates a new (empty) leaf covering the given rectangle.
     *
     * @param xMin minimum x of this leaf's rectangle.
     * @param xMax maximum x of this leaf's rectangle.
     * @param yMin minimum y of this leaf's rectangle.
     * @param yMax maximum y of this leaf's rectangle.
     */
    public LeafTrie(double xMin, double xMax, double yMin, double yMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }

    /**
     * {@inheritDoc}
     *
     * <p>If the bucket has room, appends to it and returns {@code this}.
     * Otherwise builds a {@link NodeTrie} over the same rectangle, copies
     * every existing element into it, and finally inserts {@code element}
     * &mdash; the returned trie is the new {@link NodeTrie}, which the
     * parent must splice in.
     */
    @Override
    public Trie<T> insert(T element) {
        if (elements.size() < getCapacity()) {
            elements.add(element);
            return this;
        }

        // Bucket overflow ⇒ split. Build a NodeTrie spanning the same
        // rectangle and re-insert the existing elements before the new one.
        NodeTrie<T> newNode = new NodeTrie<>(xMin, xMax, yMin, yMax);
        for (T p : elements) {
            newNode.insert(p);
        }
        return newNode.insert(element);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Linear scan of the bucket comparing exact coordinates with
     * {@link Double#compare}. Returns the first element whose {@code (x, y)}
     * matches.
     */
    @Override
    public T find(T element) {
        for (T p : elements) {
            if (Double.compare(p.x(), element.x()) == 0 &&
                Double.compare(p.y(), element.y()) == 0) {
                return p;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>If an element with the same coordinates already exists, replaces it
     * in place (no split needed). Otherwise delegates to
     * {@link #insert(HasPoint)}.
     */
    @Override
    public Trie<T> insertReplace(T element) {
        for (int i = 0; i < elements.size(); i++) {
            T p = elements.get(i);
            if (Double.compare(p.x(), element.x()) == 0 &&
                Double.compare(p.y(), element.y()) == 0) {
                elements.set(i, element);
                return this;
            }
        }
        return insert(element);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Removes every element whose coordinates match. The leaf itself is
     * never collapsed away.
     */
    @Override
    public Trie<T> delete(T element) {
        elements.removeIf(p -> Double.compare(p.x(), element.x()) == 0 &&
                               Double.compare(p.y(), element.y()) == 0);
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * <p>First step is a bounding-box test: if the search disk lies fully
     * outside the leaf rectangle, return immediately without inspecting any
     * element. Otherwise perform a linear scan.
     */
    @Override
    public void collectNear(double x, double y, double radius, Set<T> found) {
        if (x + radius < xMin || x - radius > xMax ||
            y + radius < yMin || y - radius > yMax) {
            return;
        }
        for (T p : elements) {
            double dist = Trie.getDistance(x, y, p.x(), p.y());
            if (dist <= radius) {
                found.add(p);
            }
        }
    }

    /**
     * {@link Vector2D}-flavoured overload of {@link #collectNear}.
     * Implementation mirrors {@link #collectNear} exactly.
     */
    @Override
    public void collectPoints(Vector2D center, double radius, Set<T> found) {
        if (center.getX() + radius < xMin || center.getX() - radius > xMax ||
            center.getY() + radius < yMin || center.getY() - radius > yMax) {
            return;
        }
        for (T p : elements) {
            double dist = Trie.getDistance(center.getX(), center.getY(), p.x(), p.y());
            if (dist <= radius) {
                found.add(p);
            }
        }
    }
}
