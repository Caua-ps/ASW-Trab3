package lob.physics.events;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete {@code Subject} of the <b>Observer</b> pattern, parameterised
 * over a {@link PhysicsEvent} subtype. Maintains the list of registered
 * observers and broadcasts events to them via
 * {@link #notifyObservers(PhysicsEvent)}.
 *
 * <p>Used as a building block by {@link lob.physics.engine.PhysicsWorld},
 * which keeps one {@code PhysicsSubject} per event type (one for collisions,
 * one for escapes, &hellip;).
 *
 * <p>This class is concrete (rather than abstract) because the
 * {@code PhysicsSubjectTest} unit test instantiates it directly to verify
 * registration and notification behaviour.
 *
 * @param <T> the concrete event type this subject broadcasts.
 */
public class PhysicsSubject<T extends PhysicsEvent> {

    /** Currently registered observers. Order is preserved. */
    private final List<PhysicsObserver<T>> observers = new ArrayList<>();

    /**
     * Adds {@code observer} to the notification list, unless it is
     * {@code null} or already present (duplicate registration is a no-op,
     * not a failure).
     *
     * @param observer the listener to register.
     */
    public void addObserver(PhysicsObserver<T> observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Removes {@code observer} from the notification list. No-op if the
     * observer was not registered.
     */
    public void removeObserver(PhysicsObserver<T> observer) {
        observers.remove(observer);
    }

    /**
     * Broadcasts {@code event} to every registered observer.
     *
     * <p>The observer list is copied before iteration so observers are
     * free to register or unregister listeners while reacting to the
     * event &mdash; otherwise that would throw a
     * {@link java.util.ConcurrentModificationException}.
     *
     * @param event the event to deliver. {@code null} events are ignored.
     */
    public void notifyObservers(T event) {
        if (event == null) return;
        for (PhysicsObserver<T> observer : new ArrayList<>(observers)) {
            observer.notified(event);
        }
    }
}
