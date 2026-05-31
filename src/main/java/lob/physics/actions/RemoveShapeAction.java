package lob.physics.actions;

import lob.physics.engine.PhysicsWorld;
import lob.physics.shapes.Shape;

/**
 * Deferred command that, when executed, removes a {@link Shape} from a
 * {@link PhysicsWorld}. Counterpart of {@link AddShapeAction}.
 *
 * <p>Used, for example, by Arkanoid Knockoff to delete a brick after the
 * ball collides with it: removing the brick during collision iteration
 * would invalidate the iterator, so the action is queued and applied
 * after the iteration completes.
 */
public class RemoveShapeAction implements ActionOnShapes {

    /** The world the shape will be removed from. */
    private final PhysicsWorld world;

    /** The shape to remove. */
    private final Shape shape;

    /**
     * @param world the target world (non-null).
     * @param shape the shape to remove (non-null).
     */
    public RemoveShapeAction(PhysicsWorld world, Shape shape) {
        this.world = world;
        this.shape = shape;
    }

    /** {@inheritDoc} &mdash; calls
     *  {@link PhysicsWorld#removeShapeNow(Shape)} which performs the
     *  actual mutation. */
    @Override
    public void execute() {
        world.removeShapeNow(shape);
    }
}
