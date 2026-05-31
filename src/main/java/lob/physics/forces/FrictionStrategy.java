package lob.physics.forces;

import lob.physics.Vector2D;
import lob.physics.shapes.Circle;
import lob.physics.shapes.Shape;

/**
 * {@link ForceStrategy} that produces a deceleration in the direction
 * opposite to the shape's velocity, modelling kinetic friction.
 *
 * <p>Per the slides (<i>Forças e acelerações</i>): on a horizontal plane,
 * friction has constant intensity and the same direction as the velocity
 * but the opposite sense. So the acceleration returned is
 * {@code -friction · v̂}.
 *
 * <p>Only {@link Circle} bodies feel friction (rectangles are static).
 * Shapes whose velocity is exactly zero see no force &mdash; otherwise
 * {@code v.normalize()} would flip-flop direction once friction nudged
 * the velocity past zero.
 */
public class FrictionStrategy implements ForceStrategy {

    /** Magnitude of the friction acceleration (constant). */
    private final double friction;

    /**
     * @param friction the magnitude of the friction deceleration. Must be
     *                 non-negative; positive values oppose motion.
     */
    public FrictionStrategy(double friction) {
        this.friction = friction;
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D getAcceleration(Shape s) {
        if (s instanceof Circle) {
            Circle c = (Circle) s;
            Vector2D v = c.getVelocity();
            // Stationary shapes feel no friction; otherwise we'd produce a
            // NaN from normalising a zero vector.
            if (v.x() == 0 && v.y() == 0) return Vector2D.NULL_VECTOR;
            return v.normalize().multiply(-friction);
        }
        return Vector2D.NULL_VECTOR;
    }
}
