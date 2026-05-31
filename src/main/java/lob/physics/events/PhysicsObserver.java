package lob.physics.events;

/**
 * Functional listener interface for {@link PhysicsEvent}s of a specific
 * subtype. This is the {@code Observer} role of the <b>Observer</b>
 * design pattern.
 *
 * <p>Registered with a {@link PhysicsSubject} via
 * {@link PhysicsSubject#addObserver(PhysicsObserver)}. When the subject
 * fires {@link PhysicsSubject#notifyObservers(PhysicsEvent)}, every
 * registered observer's {@link #notified(PhysicsEvent)} is invoked once.
 *
 * <p>Because the interface has a single abstract method, listeners can
 * be written as lambdas or method references &mdash; this is what the
 * games rely on when they register collision and escape handlers on a
 * {@link lob.physics.engine.PhysicsWorld}.
 *
 * @param <T> the concrete event type this observer reacts to.
 */
public interface PhysicsObserver<T extends PhysicsEvent> {

    /**
     * Called by the subject every time a matching event is fired.
     *
     * @param event the event that just occurred.
     */
    void notified(T event);
}
