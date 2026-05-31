package lob.physics.events;

import lob.physics.shapes.Shape;

/**
 * Event raised by the {@link lob.physics.engine.PhysicsWorld} when a
 * dynamic shape leaves the world's bounding rectangle.
 *
 * <p>This is the situation the slides describe as "a bola sai fora da
 * janela": the GUI typically interprets it as game-over (Dribbling
 * Master) or as a successful exit (Cannon Practice's target).
 *
 * @param boundary the static rectangle representing the side the shape
 *                 escaped through (for instance, the ground).
 * @param escaped  the shape that left the world.
 */
public record EscapeEvent(Shape boundary, Shape escaped) implements PhysicsEvent {
    // Record components generate the boundary() and escaped() accessors
    // required by the EscapeEventTest unit test.
}
