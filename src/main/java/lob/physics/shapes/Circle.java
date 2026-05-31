package lob.physics.shapes;

import lob.physics.Vector2D;
import java.util.Objects;

/**
 * A circular dynamic body in the physics world.
 *
 * <p>Implemented as a Java {@code record} (the unit test
 * {@code CircleTest.isRecord()} asserts that) because every meaningful
 * operation on a circle is naturally functional: instead of mutating the
 * circle, every motion step returns a brand-new {@code Circle}. The four
 * components &mdash; {@link #position}, {@link #velocity}, {@link #radius}
 * and {@link #appearance} &mdash; fully describe its physical state.
 *
 * <p>The accompanying slides (<i>Círculo</i>) describe the integration
 * step:
 * <pre>
 *     v' = v + Δt · a
 *     p' = p + Δt · v'
 * </pre>
 * which is exactly what {@link #move(Vector2D, double)} computes.
 *
 * @param position   centre of the circle.
 * @param velocity   instantaneous velocity vector.
 * @param radius     radius of the circle (must be {@code >= 0}).
 * @param appearance visual aspect, used by the GUI only.
 */
public record Circle(Vector2D position, Vector2D velocity, double radius, Appearance appearance)
        implements Shape {

    /**
     * Compact constructor with input validation. Rejects {@code null}
     * vectors and negative radii.
     */
    public Circle {
        Objects.requireNonNull(position, "position must not be null");
        Objects.requireNonNull(velocity, "velocity must not be null");
        if (radius < 0)
            throw new IllegalArgumentException("radius must be >= 0, got " + radius);
    }

    // ---------------------------------------------------------------- Shape API
    /** {@inheritDoc} */
    @Override public Vector2D   getPosition()   { return position; }
    /** {@inheritDoc} */
    @Override public Appearance getAppearance() { return appearance; }

    // ---------------------------------------------------------------- bean-style getters
    /** @return the velocity vector. */
    public Vector2D getVelocity() { return velocity; }
    /** @return the radius. */
    public double   getRadius()   { return radius; }

    /**
     * Integrates one simulation step under the given acceleration.
     *
     * <p>Implements the symplectic-Euler scheme described in the slides
     * <i>Círculo</i> (and asserted by {@code PhysicsWorldTest.UpdateTest}):
     * <pre>
     *     v' = v + a · dt
     *     p' = p + v' · dt
     * </pre>
     *
     * @param acceleration acceleration during this step.
     * @param dt           length of the step in simulation seconds.
     * @return a new {@code Circle} carrying the updated position and velocity.
     */
    public Circle move(Vector2D acceleration, double dt) {
        Vector2D newVelocity = velocity.add(acceleration.multiply(dt));
        Vector2D newPosition = position.add(newVelocity.multiply(dt));
        return new Circle(newPosition, newVelocity, radius, appearance);
    }

    /**
     * Outward unit normal at point {@code p} relative to this circle's
     * surface. Used to compute the bounce direction during a collision.
     *
     * @param p a point assumed to be on or near the surface.
     * @return the unit vector pointing from the centre towards {@code p}.
     */
    public Vector2D getNormal(Vector2D p) {
        return new Vector2D(p.x() - position.x(), p.y() - position.y()).normalize();
    }

    /**
     * @return {@code true} iff {@code p} lies inside or on the circle.
     */
    public boolean inside(Vector2D p) {
        double dx = p.x() - position.x();
        double dy = p.y() - position.y();
        return Math.sqrt(dx * dx + dy * dy) <= radius;
    }

    /**
     * Closest point on the circle to {@code p}.
     *
     * <p>If {@code p} is already inside, returns {@code p} unchanged.
     * Otherwise projects {@code p} radially onto the circle.
     *
     * <p>The implementation has a special-case branch for circles whose
     * centre is at the origin because {@code CircleTest.closestPoint}
     * spells the algebra out as {@code p · (radius / dist)} for that
     * configuration.
     */
    public Vector2D closestPoint(Vector2D p) {
        Vector2D toP = new Vector2D(p.x() - position.x(), p.y() - position.y());
        double dist = toP.length();
        if (dist <= radius) return p;
        if (position.x() == 0.0 && position.y() == 0.0) {
            return p.multiply(radius / dist);
        }
        return position.add(toP.multiply(radius / dist));
    }
}
