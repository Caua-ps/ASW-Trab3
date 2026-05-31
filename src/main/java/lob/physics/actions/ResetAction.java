package lob.physics.actions;

import lob.physics.engine.PhysicsWorld;

/**
 * Deferred command that, when executed, clears every shape from a
 * {@link PhysicsWorld}. Used by games when the player loses or restarts:
 * issuing the reset during collision handling would corrupt iterators,
 * so the operation is queued like every other mutation.
 */
public class ResetAction implements ActionOnShapes {

    /** The world to clear. */
    private final PhysicsWorld world;

    /** @param world the world that will be cleared on {@link #execute()}. */
    public ResetAction(PhysicsWorld world) {
        this.world = world;
    }

    /** {@inheritDoc} &mdash; calls {@link PhysicsWorld#clear()}. */
    @Override
    public void execute() {
        world.clear();
    }
}
