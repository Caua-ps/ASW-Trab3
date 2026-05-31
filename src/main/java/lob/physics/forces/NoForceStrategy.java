package lob.physics.forces;

import lob.physics.Vector2D;
import lob.physics.shapes.Shape;

/**
 * {@link ForceStrategy} that applies no force at all: every shape's
 * velocity is preserved (modulo collisions) for the entire simulation.
 *
 * <p>This is the appropriate strategy for inertial games such as
 * Arkanoid Knockoff, where the ball travels in a straight line until it
 * hits a wall or a brick.
 *
 * <p>Design pattern note: this is the <i>null object</i> variant of the
 * Strategy pattern &mdash; the engine never has to special-case "no
 * force"; it just gets a {@link Vector2D#NULL_VECTOR} from the strategy.
 */
public class NoForceStrategy implements ForceStrategy {

    /** {@inheritDoc} &mdash; always returns {@link Vector2D#NULL_VECTOR}. */
    @Override
    public Vector2D getAcceleration(Shape shape) {
        return Vector2D.NULL_VECTOR;
    }
}
