package lob.gaming.games;

import lob.gaming.GameAnimation;
import lob.physics.Vector2D;
import lob.physics.forces.GravityStrategy;
import lob.physics.shapes.Circle;
import lob.physics.shapes.Rectangle;
import lob.physics.shapes.Shape;

/**
 * Cannon Practice &mdash; fire a cannonball at a target while avoiding a wall.
 *
 * <p>Vertical plane, gravity pulls the ball down. The player clicks and the
 * ball is fired from the cannon (bottom-left) towards the click point.
 *
 * <p>Design patterns: <b>Strategy</b> ({@link GravityStrategy}) and
 * <b>Template Method</b> via {@link GameAnimation}.
 */
public class CannonPractice extends GameAnimation {

    /** Game name registered in the {@link lob.gaming.ReflectGameFactory}. */
    public static final String GAME_NAME         = "Cannon Practice";
    /** Short human-readable instructions. */
    public static final String GAME_INSTRUCTIONS =
            "Click to fire towards that point. Hit the target on the right!";

    private static final double WORLD_WIDTH  = 600;
    private static final double WORLD_HEIGHT = 400;

    /** Pixels/s² downward — much more visible than the textbook 9.8. */
    private static final double GRAVITY = 600;

    private static final double CANNON_X = 25;
    private static final double CANNON_Y = WORLD_HEIGHT - 25;

    private static final double BALL_RADIUS = 8;
    private static final double FIRE_SPEED  = 450;

    /** The current in-flight cannonball, or {@code null} if none. */
    private Circle cannonball;
    /** Number of successful target hits this session. */
    private int hits;
    /** Total number of shots fired this session. */
    private int shots;

    /** Builds the world with downward gravity and a damped restitution. */
    public CannonPractice() {
        super();
        world.setForceStrategy(new GravityStrategy(new Vector2D(0, GRAVITY)));
        world.setRestitution(0.4);
        resetGame();
    }

    /** {@inheritDoc} */
    @Override public String getName()         { return GAME_NAME; }
    /** {@inheritDoc} */
    @Override public String getInstructions() { return GAME_INSTRUCTIONS; }
    /** {@inheritDoc} */
    @Override public double getWidth()        { return WORLD_WIDTH; }
    /** {@inheritDoc} */
    @Override public double getHeight()       { return WORLD_HEIGHT; }

    /** @return number of successful target hits. */
    public int getHits()  { return hits; }
    /** @return total number of shots fired. */
    public int getShots() { return shots; }

    /** Resets the world: rebuild cannon, wall, target, ground; zero counters. */
    @Override
    public void resetGame() {
        world.reset();
        cannonball = null;
        hits = 0;
        shots = 0;

        // Cannon body — small dark square at the bottom-left.
        world.addShape(new Rectangle(CANNON_X - 8, CANNON_Y - 4,
                CANNON_X + 8, CANNON_Y + 8, getAppearance("cannon")));

        // Wall obstacle in the middle — low enough to be cleared by a good shot.
        world.addShape(new Rectangle(WORLD_WIDTH / 2 - 6, WORLD_HEIGHT - 60,
                WORLD_WIDTH / 2 + 6, WORLD_HEIGHT - 4, getAppearance("wall")));

        // Target on the right.
        world.addShape(new Rectangle(WORLD_WIDTH - 60, WORLD_HEIGHT - 30,
                WORLD_WIDTH - 20, WORLD_HEIGHT - 4, getAppearance("target")));

        // Ground (so the ball doesn't fall forever).
        world.addShape(new Rectangle(0, WORLD_HEIGHT - 4,
                WORLD_WIDTH, WORLD_HEIGHT, getAppearance("ground")));
    }

    /** Fires the cannon towards an aim point. Replaces any in-flight ball. */
    public void fire(Vector2D aim) {
        Shape existing = world.findShape(s -> s instanceof Circle);
        if (existing != null) world.removeShape(existing);

        Vector2D origin    = new Vector2D(CANNON_X, CANNON_Y - BALL_RADIUS);
        Vector2D direction = aim.minus(origin).normalize();
        Vector2D velocity  = direction.multiply(FIRE_SPEED);

        cannonball = new Circle(origin, velocity, BALL_RADIUS, getAppearance("cannonball"));
        world.addShape(cannonball);
        shots++;
    }

    /**
     * Steps the simulation, then checks for win conditions or out-of-bounds.
     */
    @Override
    public void step() {
        world.update(getStep());

        Shape s = world.findShape(x -> x instanceof Circle);
        if (!(s instanceof Circle c)) {
            cannonball = null;
            return;
        }
        cannonball = c;

        Vector2D p = c.getPosition();

        // Win check: ball overlaps the target.
        if (p.x() >= WORLD_WIDTH - 60 - BALL_RADIUS
                && p.y() >= WORLD_HEIGHT - 30 - BALL_RADIUS
                && p.x() <= WORLD_WIDTH - 20 + BALL_RADIUS) {
            hits++;
            showMessage("Hit! (" + hits + "/" + shots + ")");
            reportScore(hits);   // leaderboard: cumulative hits this session
            world.removeShape(c);
            cannonball = null;
            return;
        }

        // Out-of-bounds: kill the ball quietly.
        if (p.x() < -BALL_RADIUS || p.x() > WORLD_WIDTH + BALL_RADIUS
                || p.y() < -BALL_RADIUS) {
            world.removeShape(c);
            cannonball = null;
            return;
        }

        // Settled on the ground without hitting the target — clean it up.
        if (p.y() >= WORLD_HEIGHT - 4 - BALL_RADIUS - 1
                && Math.abs(c.getVelocity().y()) < 5) {
            world.removeShape(c);
            cannonball = null;
        }
    }
}
