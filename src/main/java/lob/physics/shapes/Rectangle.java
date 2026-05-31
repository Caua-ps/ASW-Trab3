package lob.physics.shapes;

import lob.physics.Vector2D;

/**
 * A static rectangular body, axis-aligned with the simulation's reference
 * frame.
 *
 * <p>Per the slides (<i>Retângulo</i>), rectangles do <i>not</i> move
 * under the influence of forces or velocities &mdash; their {@link #move}
 * method is a no-op. They <i>can</i> however be repositioned externally
 * (e.g. the player's paddle in Arkanoid) by replacing the instance.
 *
 * <p>The rectangle is described by its four edges
 * ({@code xMin}, {@code yMin}, {@code xMax}, {@code yMax}) where:
 * <ul>
 *   <li>{@code xMin} = left edge, {@code xMax} = right edge;</li>
 *   <li>{@code yMin} = top edge, {@code yMax} = bottom edge
 *       (remember y grows downwards in this project's coordinate system).</li>
 * </ul>
 *
 * <p>Class is {@code final} because it is one of the two permitted
 * implementors of the sealed {@link Shape} interface.
 */
public final class Rectangle implements Shape {

    /** Bounding edges of this rectangle. */
    private final double xMin, yMin, xMax, yMax;

    /** Pre-computed centre, returned by {@link #getPosition()}. */
    private final Vector2D position;

    /** Visual aspect, opaque to the engine. */
    private final Appearance appearance;

    /**
     * Creates a rectangle from its four edges and an appearance.
     *
     * @throws IllegalArgumentException if {@code xMax < xMin} or
     *         {@code yMax < yMin}, since that would describe a degenerate
     *         (negative-area) rectangle.
     */
    public Rectangle(double xMin, double yMin, double xMax, double yMax, Appearance appearance) {
        if (xMax < xMin)
            throw new IllegalArgumentException("xMax (" + xMax + ") < xMin (" + xMin + ")");
        if (yMax < yMin)
            throw new IllegalArgumentException("yMax (" + yMax + ") < yMin (" + yMin + ")");
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
        this.position = new Vector2D((xMin + xMax) / 2.0, (yMin + yMax) / 2.0);
        this.appearance = appearance;
    }

    /** @return the left edge x. */
    public double xMin() { return xMin; }
    /** @return the top edge y. */
    public double yMin() { return yMin; }
    /** @return the right edge x. */
    public double xMax() { return xMax; }
    /** @return the bottom edge y. */
    public double yMax() { return yMax; }

    /** {@inheritDoc} &mdash; returns the rectangle's centre. */
    @Override public Vector2D   getPosition()   { return position; }
    /** {@inheritDoc} */
    @Override public Appearance getAppearance() { return appearance; }

    /** @return the upper-left corner (xMin, yMin). */
    public Vector2D upperLeft()  { return new Vector2D(xMin, yMin); }
    /** @return the upper-right corner (xMax, yMin). */
    public Vector2D upperRight() { return new Vector2D(xMax, yMin); }
    /** @return the lower-left corner (xMin, yMax). */
    public Vector2D lowerLeft()  { return new Vector2D(xMin, yMax); }
    /** @return the lower-right corner (xMax, yMax). */
    public Vector2D lowerRight() { return new Vector2D(xMax, yMax); }

    /**
     * No-op. Rectangles are static bodies in this engine; the method exists
     * only to keep the symmetry with {@link Circle#move(Vector2D, double)}.
     *
     * @return {@code this}.
     */
    public Rectangle move(Vector2D velocity, double dt) { return this; }

    /**
     * Closest point on the rectangle to {@code p}, computed by clamping
     * each coordinate to the rectangle's range.
     *
     * @param p any point.
     * @return the closest point on the rectangle (which equals {@code p}
     *         itself if {@code p} is inside).
     */
    public Vector2D closestPoint(Vector2D p) {
        double cx = Math.max(xMin, Math.min(p.x(), xMax));
        double cy = Math.max(yMin, Math.min(p.y(), yMax));
        return new Vector2D(cx, cy);
    }

    /**
     * @return {@code true} iff {@code p} lies inside or on the rectangle.
     */
    public boolean inside(Vector2D p) {
        return p.x() >= xMin && p.x() <= xMax && p.y() >= yMin && p.y() <= yMax;
    }

    /**
     * Outward unit normal of the face closest to {@code p}.
     *
     * <p>The unit test {@code GetNormalTest} pins a deterministic
     * tie-breaking order for ambiguous cases (corners and the centre):
     * <b>top &gt; right &gt; bottom &gt; left</b>. So:
     * <ul>
     *   <li>top-left corner &rarr; top wins,</li>
     *   <li>top-right corner &rarr; top wins,</li>
     *   <li>bottom-right corner &rarr; right wins,</li>
     *   <li>bottom-left corner &rarr; bottom wins,</li>
     *   <li>centre of the rectangle &rarr; top wins.</li>
     * </ul>
     * The implementation respects this order by checking {@code dTop}
     * first, then {@code dRight}, etc.
     *
     * @param p a point near the rectangle.
     * @return one of the four unit vectors {@code (0,-1)}, {@code (1,0)},
     *         {@code (0,1)}, {@code (-1,0)}.
     */
    public Vector2D getNormal(Vector2D p) {
        double dTop    = Math.abs(p.y() - yMin);
        double dRight  = Math.abs(p.x() - xMax);
        double dBottom = Math.abs(p.y() - yMax);
        double dLeft   = Math.abs(p.x() - xMin);

        double min = Math.min(Math.min(dTop, dRight), Math.min(dBottom, dLeft));

        if (dTop    == min) return new Vector2D( 0, -1);
        if (dRight  == min) return new Vector2D( 1,  0);
        if (dBottom == min) return new Vector2D( 0,  1);
        return                     new Vector2D(-1,  0);
    }
}
