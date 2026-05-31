package lob.physics.actions;

/**
 * Functional interface for a deferred mutation on the physics world.
 *
 * <p>This is the {@code Command} role of the <b>Command</b> design pattern
 * applied to the {@link lob.physics.engine.PhysicsWorld}: instead of
 * mutating the world (adding shapes, removing shapes, resetting it) in
 * the middle of an iteration &mdash; which would corrupt iterators and
 * trigger {@link java.util.ConcurrentModificationException} &mdash; the
 * mutation is wrapped in an {@code ActionOnShapes} and queued in a
 * {@link PendingActions} buffer. The world flushes the queue at the end
 * of each {@code update()} cycle, replaying every command in order.
 *
 * <p>Concrete commands: {@link AddShapeAction}, {@link RemoveShapeAction},
 * {@link ResetAction}.
 */
public interface ActionOnShapes {

    /** Apply this command to the world. Called by
     *  {@link PendingActions#executePendingActions()}. */
    void execute();
}
