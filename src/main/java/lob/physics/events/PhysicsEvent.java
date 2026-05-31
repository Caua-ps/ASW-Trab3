package lob.physics.events;

/**
 * Marker interface for any event raised by the physics engine.
 *
 * <p>This is the abstract event type of the <b>Observer</b> design pattern
 * implemented by {@link PhysicsSubject}/{@link PhysicsObserver}: every
 * concrete event ({@link CollisionEvent}, {@link EscapeEvent}, &hellip;)
 * implements it so that subjects, observers and listener registrations
 * can be parameterised over a common upper bound.
 *
 * <p>The interface is intentionally empty &mdash; events carry their
 * payload through their concrete type, and the engine type-checks
 * listener registrations against that type rather than reading data via
 * this base interface.
 */
public interface PhysicsEvent {
    // Marker interface: empty by design.
}
