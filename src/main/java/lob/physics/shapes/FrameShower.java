package lob.physics.shapes;

/**
 * Functional interface representing "render one frame of the simulation".
 *
 * <p>The physics engine ({@link lob.physics.engine.PhysicsWorld}) calls
 * {@link #show(Iterable)} once per simulation step with the iterable of
 * shapes that need to be drawn. The actual drawing happens elsewhere
 * (Swing GUI, headless test mock, etc.) &mdash; this interface keeps the
 * engine decoupled from any concrete rendering technology.
 *
 * <p>Because {@link lob.physics.engine.PhysicsWorld} itself implements
 * {@code Iterable<Shape>}, callers can pass {@code world::iterator} or
 * simply hand the world over wherever a frame source is required. This is
 * what {@code GameAnimationTest} relies on: it sets a frame shower with
 * {@code game.setFrameShower(this::myFrameShower)} where the test method
 * has signature {@code void myFrameShower(Iterable<Shape> shapes)}.
 */
@FunctionalInterface
public interface FrameShower {

    /**
     * Renders a single frame.
     *
     * @param shapes iterable of every shape currently in the world.
     */
    void show(Iterable<Shape> shapes);
}
