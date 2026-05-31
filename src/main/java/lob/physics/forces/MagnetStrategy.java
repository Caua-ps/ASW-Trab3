package lob.physics.forces;

import lob.physics.Vector2D;
import lob.physics.shapes.Circle;
import lob.physics.shapes.Shape;

/**
 * {@link ForceStrategy} modelling a magnet: every {@link Circle} is pulled
 * towards (or pushed away from) a single point — the magnet — which can be
 * moved at runtime (e.g. to follow the mouse cursor).
 *
 * <p>Unlike {@link GravityStrategy} (a constant vector), this strategy inspects
 * each shape's <b>position</b> to aim the force at the magnet, and its
 * <b>velocity</b> to add a little damping so the balls converge on the magnet
 * instead of orbiting it forever. Non-circular shapes (walls, target) are left
 * untouched.
 *
 * <p>Design pattern: <b>Strategy</b> — swapped into the
 * {@link lob.physics.engine.PhysicsWorld} exactly like gravity or friction.
 */
public class MagnetStrategy implements ForceStrategy {

    /** Point the balls are attracted to (updated as the cursor moves). */
    private volatile Vector2D magnet = Vector2D.NULL_VECTOR;

    /** {@code true} to attract, {@code false} to repel. */
    private volatile boolean attract = true;

    /** Magnitude of the magnetic acceleration. */
    private final double strength;

    /** Velocity damping factor, so balls settle on the magnet. */
    private final double damping;

    /**
     * @param strength magnitude of the pull (acceleration units).
     */
    public MagnetStrategy(double strength) {
        this(strength, 1.8);
    }

    /**
     * @param strength magnitude of the pull (acceleration units).
     * @param damping  velocity damping factor (higher = settles faster).
     */
    public MagnetStrategy(double strength, double damping) {
        this.strength = strength;
        this.damping = damping;
    }

    /**
     * Moves the magnet (e.g. to the cursor position).
     *
     * @param point the new magnet location, in world coordinates.
     */
    public void setMagnet(Vector2D point) {
        this.magnet = point;
    }

    /**
     * Switches between attraction and repulsion.
     *
     * @param attract {@code true} to attract, {@code false} to repel.
     */
    public void setAttract(boolean attract) {
        this.attract = attract;
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D getAcceleration(Shape shape) {
        if (!(shape instanceof Circle ball)) {
            return Vector2D.NULL_VECTOR;   // walls and the target are not magnetic
        }

        Vector2D toMagnet = magnet.minus(ball.position());
        double distance = toMagnet.length();

        Vector2D pull = (distance < 1e-3)
                ? Vector2D.NULL_VECTOR
                : toMagnet.normalize().multiply(attract ? strength : -strength);

        // Damping opposes the current velocity so the swarm converges.
        Vector2D drag = ball.velocity().multiply(-damping);

        return pull.add(drag);
    }
}
