package lob.physics.events;

import lob.physics.shapes.Shape;
import lob.physics.engine.CollisionManifold;

/**
 * Event raised by the {@link lob.physics.engine.PhysicsWorld} when two
 * shapes touch or overlap during a simulation step.
 *
 * <p>Implemented as a record so the three accessors
 * ({@link #colliding()}, {@link #collided()}, {@link #collision()}) are
 * derived for free &mdash; the unit test {@code CollisionEventTest}
 * relies on those exact names.
 *
 * @param colliding the dynamic shape (typically a circle) that initiated
 *                  the contact.
 * @param collided  the other shape involved in the contact (a circle or a
 *                  static rectangle).
 * @param collision the {@link CollisionManifold} describing the geometry
 *                  of the contact (normal and penetration depth).
 */
public record CollisionEvent(Shape colliding, Shape collided, CollisionManifold collision)
        implements PhysicsEvent {
    // Record components above generate the accessors required by tests.
}
