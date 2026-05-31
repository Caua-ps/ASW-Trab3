package lob.physics.actions;

import lob.physics.engine.PhysicsWorld;
import lob.physics.shapes.Shape;

/**
 * Deferred command that, when executed, adds a {@link Shape} to a
 * {@link PhysicsWorld}.
 *
 * <p>Created by callers via
 * {@code world.addShape(shape)}, which under the hood wraps the request
 * in this command and queues it via
 * {@link PendingActions#defer(ActionOnShapes)} so the actual mutation
 * happens after the current iteration finishes (see {@link PendingActions}
 * for the rationale).
 */
public class AddShapeAction implements ActionOnShapes {

    /** The world the shape will be added to. */
    private final PhysicsWorld world;

    /** The shape to add. */
    private final Shape shape;

    /**
     * @param world the target world (non-null).
     * @param shape the shape to add (non-null).
     */
    public AddShapeAction(PhysicsWorld world, Shape shape) {
        this.world = world;
        this.shape = shape;
    }

    /** {@inheritDoc} &mdash; calls
     *  {@link PhysicsWorld#addShapeNow(Shape)} which performs the actual
     *  mutation. */
    @Override
    public void execute() {
        world.addShapeNow(shape);
    }
}
