package lob.physics.engine;

import lob.physics.events.CollisionEvent;
import lob.physics.events.PhysicsObserver;
import lob.physics.shapes.Shape;

/**
 * Strategy interface that the {@link PhysicsWorld} delegates collision
 * detection and resolution to.
 *
 * <p>Two responsibilities:
 * <ol>
 *   <li><b>Detection</b> &mdash; given two shapes, decide whether they
 *       overlap and, if so, return a {@link CollisionManifold} describing
 *       the contact (penetration depth and surface normal). The slides
 *       define this in <i>Colisões</i>.</li>
 *   <li><b>Resolution</b> &mdash; given a manifold, push the colliding
 *       shape out of the other and reflect its velocity according to the
 *       <i>Repor a posição</i> and <i>Rebater</i> formulae from the slides
 *       (with optional damping via {@link #getRestitution()}).</li>
 * </ol>
 *
 * <p>Per-shape collision listeners are also registered through this
 * interface, so the manager can fire observer notifications using the
 * <b>Observer</b> pattern (see {@link CollisionEvent}).
 *
 * <p>Concrete implementation: {@link SimpleCollisionManager}.
 */
public interface CollisionManager {

    /**
     * Detects a collision between two shapes.
     *
     * @return a manifold describing the contact, or
     *         {@link CollisionManifold#NO_COLLISION} if the shapes do not
     *         overlap.
     */
    CollisionManifold checkCollision(Shape s1, Shape s2);

    /**
     * Repositions and reflects {@code colliding} away from {@code collided}
     * using the manifold geometry.
     *
     * @return the {@code Shape} after collision resolution (a brand-new
     *         instance for circles, since they are records).
     */
    Shape resolveCollision(Shape colliding, Shape collided, CollisionManifold collision);

    /** Registers an observer for collisions involving {@code shape}. */
    void addCollisionListener(Shape shape, PhysicsObserver<CollisionEvent> observer);

    /** Unregisters a previously registered observer. */
    void removeCollisionListener(Shape shape, PhysicsObserver<CollisionEvent> observer);

    /** Removes <i>every</i> listener registered against {@code shape}. */
    void removeAllCollisionListeners(Shape shape);

    /**
     * Default no-op so test mocks don't have to override; a real manager
     * fires {@link CollisionEvent}s to the per-shape observers.
     */
    default void notifyCollision(Shape s1, Shape s2, CollisionManifold manifold) {
        // No-op; concrete implementations override.
    }

    /**
     * @return the coefficient of restitution {@code τ} used during bounce
     *         (slides: <i>Amortecer</i>). 0 = perfectly plastic, 1 =
     *         perfectly elastic.
     */
    double getRestitution();

    /** Updates the coefficient of restitution. */
    void   setRestitution(double restitution);
}
