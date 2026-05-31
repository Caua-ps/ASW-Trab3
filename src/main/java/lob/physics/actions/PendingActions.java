package lob.physics.actions;

import java.util.ArrayList;
import java.util.List;

/**
 * Queue of deferred {@link ActionOnShapes} commands &mdash; the
 * {@code Invoker} role of the <b>Command</b> design pattern as used by
 * {@link lob.physics.engine.PhysicsWorld}.
 *
 * <p>While the world is iterating its shapes (during {@code update()},
 * collision detection, or observer notifications), no listener is allowed
 * to mutate the shape collection directly. Instead it calls
 * {@link #defer(ActionOnShapes)} to schedule the change. Once the
 * iteration is over the world calls
 * {@link #executePendingActions()} to flush the queue, mutating the
 * world safely.
 */
public class PendingActions {

    /** Queued commands, in insertion order. */
    private final List<ActionOnShapes> actions = new ArrayList<>();

    /**
     * Adds {@code action} to the back of the queue.
     *
     * @param action a command produced by an event handler.
     */
    public void defer(ActionOnShapes action) {
        actions.add(action);
    }

    /**
     * Executes every queued command and clears the queue.
     *
     * <p>The list is iterated through a private snapshot so a command is
     * free to {@link #defer(ActionOnShapes)} new commands while running
     * &mdash; those new commands will be processed on the next flush, not
     * during the current one.
     */
    public void executePendingActions() {
        List<ActionOnShapes> toRun = new ArrayList<>(actions);
        actions.clear();
        for (ActionOnShapes a : toRun) a.execute();
    }
}
