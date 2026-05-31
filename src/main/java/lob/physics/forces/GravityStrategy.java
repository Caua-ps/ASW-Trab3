package lob.physics.forces;

import lob.physics.Vector2D;
import lob.physics.shapes.Shape;

/**
 * {@link ForceStrategy} that returns a constant downward acceleration,
 * modelling gravity in a 2D vertical world.
 *
 * <p>Per the slides (<i>Forças e acelerações</i>), gravity has the same
 * intensity and direction regardless of the shape's state &mdash; the
 * implementation just returns its stored vector verbatim, ignoring the
 * argument.
 *
 * <p>Two constructors are provided:
 * <ul>
 *   <li>the no-arg constructor uses Earth's gravity at sea level
 *       ({@code 9.8 m/s²} pointing down, i.e. {@code +y} in this project's
 *       screen-style coordinates) &mdash; required by
 *       {@code GravityStrategyTest} which calls {@code new GravityStrategy()};</li>
 *   <li>the parameterised constructor lets callers pick any other
 *       gravitational vector, useful for fantasy physics or for testing.</li>
 * </ul>
 */
public class GravityStrategy implements ForceStrategy {

    /** Stored gravitational acceleration, applied to every shape. */
    private final Vector2D gravity;

    /** Default gravity: {@code (0, 9.8)} &mdash; standard Earth gravity,
     *  pointing down because y grows downwards. */
    public GravityStrategy() {
        this.gravity = new Vector2D(0, 9.8);
    }

    /**
     * Custom gravity. Useful for fantasy physics or tests.
     *
     * @param gravity any 2D acceleration vector.
     */
    public GravityStrategy(Vector2D gravity) {
        this.gravity = gravity;
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D getAcceleration(Shape shape) {
        return gravity;
    }
}
