package lob.physics.shapes;

import lob.physics.Vector2D;
import lob.quadtree.HasPoint;

/**
 * Common interface for every body that lives in the physics world.
 *
 * <p>This is a <b>sealed</b> interface restricted to {@link Circle} and
 * {@link Rectangle}. We use an interface (rather than an abstract class)
 * because Java records cannot extend classes &mdash; and we want
 * {@link Circle} to be a record (the unit test
 * {@code CircleTest.isRecord()} asserts that). Sealing keeps the type
 * hierarchy closed: the engine knows it only ever needs to handle these
 * two cases when computing collisions.
 *
 * <p>{@code Shape} also extends {@link HasPoint}, which means any shape
 * can be inserted into a {@link lob.quadtree.PointQuadtree} keyed by its
 * position &mdash; the spatial index used to speed up collision queries.
 *
 * <p>Both record-style accessors ({@code position()}, {@code appearance()})
 * and bean-style getters ({@code getPosition()}, {@code getAppearance()})
 * are exposed because the test suite uses both.
 */
public sealed interface Shape extends HasPoint permits Circle, Rectangle {

    /** @return the position of this shape (centre for circles, centre of
     *  the bounding box for rectangles). */
    Vector2D    getPosition();

    /** @return the visual aspect of this shape. */
    Appearance  getAppearance();

    /** Record-style alias of {@link #getPosition()}. */
    default Vector2D position()    { return getPosition(); }

    /** Record-style alias of {@link #getAppearance()}. */
    default Appearance appearance(){ return getAppearance(); }

    /** {@inheritDoc} &mdash; delegates to {@link #getPosition()}. */
    @Override default double x() { return getPosition().x(); }

    /** {@inheritDoc} &mdash; delegates to {@link #getPosition()}. */
    @Override default double y() { return getPosition().y(); }
}
