package lob.physics.forces;

import lob.physics.Vector2D;
import lob.physics.shapes.Shape;

/**
 * Strategy that produces the acceleration to apply to a {@link Shape}
 * during one simulation step.
 *
 * <p>Design pattern: <b>Strategy</b>. The {@link lob.physics.engine.PhysicsWorld}
 * holds a {@code ForceStrategy} reference and asks it for accelerations
 * without knowing whether they originate from gravity, friction, magnetism,
 * or no force at all. Swapping strategies (or composing them) is what gives
 * each game its distinct physical feel:
 * <ul>
 *   <li>{@link GravityStrategy} &mdash; constant downward acceleration
 *       (vertical games like Cannon Practice, Dribbling Master);</li>
 *   <li>{@link FrictionStrategy} &mdash; opposes velocity (top-down games
 *       like Micro Golf);</li>
 *   <li>{@link NoForceStrategy} &mdash; returns zero, used when the
 *       simulation is meant to be inertial (Arkanoid Knockoff).</li>
 * </ul>
 */
public interface ForceStrategy {

    /**
     * Acceleration to apply to {@code shape} during the upcoming simulation
     * step. Implementations may inspect the shape's velocity, position or
     * radius to decide.
     *
     * @param shape the shape being integrated.
     * @return the acceleration vector (may be {@link Vector2D#NULL_VECTOR}).
     */
    Vector2D getAcceleration(Shape shape);
}
