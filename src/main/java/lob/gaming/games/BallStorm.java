package lob.gaming.games;

import lob.gaming.GameAnimation;
import lob.physics.Vector2D;
import lob.physics.forces.GravityStrategy;
import lob.physics.shapes.Circle;
import lob.physics.shapes.Rectangle;
import lob.physics.shapes.Shape;

/**
 * Ball Storm — the "lots of balls" game (project valorization).
 *
 * <p>A closed box under gravity. Every click spawns a <i>burst</i> of balls at
 * the cursor, each thrown in a random direction; balls bounce off the walls and
 * off each other and pile up at the bottom. The world quickly fills with a large
 * number of balls, which is exactly the stress scenario the assignment asks for
 * ("novos jogos, preferencialmente com um grande número de bolas").
 *
 * <p>It is a plain {@link GameAnimation} subclass with a public {@code GAME_NAME}
 * field and a no-arg constructor, so the {@link lob.gaming.ReflectGameFactory}
 * discovers and instantiates it automatically — no change to the discovery code
 * is needed to add this game.
 *
 * <p>Design patterns: <b>Strategy</b> ({@link GravityStrategy}) and
 * <b>Template Method</b> via {@link GameAnimation}.
 */
public class BallStorm extends GameAnimation {

    /** Game name registered in the {@link lob.gaming.ReflectGameFactory}. */
    public static final String GAME_NAME         = "Ball Storm";
    /** Short human-readable instructions. */
    public static final String GAME_INSTRUCTIONS =
            "Click anywhere to launch a burst of balls. Fill the box!";

    private static final double WORLD_WIDTH    = 600;
    private static final double WORLD_HEIGHT   = 460;
    private static final double WALL_THICKNESS = 10;

    private static final double GRAVITY      = 500;
    private static final double BALL_RADIUS  = 9;
    private static final int    BURST_SIZE   = 10;
    private static final double BURST_SPEED  = 260;

    /** Hard cap on live balls so the simulation stays responsive. */
    private static final int    MAX_BALLS    = 140;

    /** Total balls launched this session — used as the leaderboard score. */
    private int spawned;

    /** Builds a closed box with downward gravity and bouncy restitution. */
    public BallStorm() {
        super();
        world.setForceStrategy(new GravityStrategy(new Vector2D(0, GRAVITY)));
        world.setRestitution(0.85);
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

    /** @return total number of balls launched this session. */
    public int getSpawned() { return spawned; }

    /** Resets the world: rebuild the four border walls, clear all balls. */
    @Override
    public void resetGame() {
        world.reset();
        spawned = 0;

        // Four border walls (top, bottom, left, right) — a closed box.
        world.addShape(new Rectangle(0, 0,
                WORLD_WIDTH, WALL_THICKNESS, getAppearance("wall")));
        world.addShape(new Rectangle(0, WORLD_HEIGHT - WALL_THICKNESS,
                WORLD_WIDTH, WORLD_HEIGHT, getAppearance("wall")));
        world.addShape(new Rectangle(0, 0,
                WALL_THICKNESS, WORLD_HEIGHT, getAppearance("wall")));
        world.addShape(new Rectangle(WORLD_WIDTH - WALL_THICKNESS, 0,
                WORLD_WIDTH, WORLD_HEIGHT, getAppearance("wall")));
    }

    /**
     * Launches a burst of {@link #BURST_SIZE} balls at the click point, each in
     * a random direction. Older balls beyond {@link #MAX_BALLS} are recycled.
     *
     * @param at the click point, in world coordinates.
     */
    public void burst(Vector2D at) {
        double cx = at.x(), cy = at.y();

        for (int i = 0; i < BURST_SIZE; i++) {
            // Spread the burst around the click on a jittered ring. This keeps
            // every spawn position distinct: the PointQuadtree that indexes the
            // world by position cannot separate two points at the *same* float
            // coordinates, so stacking balls on one spot would recurse forever.
            double angle  = (2 * Math.PI * i) / BURST_SIZE + Math.random() * 0.4;
            double radius = BALL_RADIUS * 1.6 + Math.random() * BALL_RADIUS;
            double sx = clampX(cx + Math.cos(angle) * radius);
            double sy = clampY(cy + Math.sin(angle) * radius);

            Vector2D velocity = new Vector2D(
                    Math.cos(angle) * BURST_SPEED,
                    Math.sin(angle) * BURST_SPEED);
            world.addShape(new Circle(new Vector2D(sx, sy), velocity,
                    BALL_RADIUS, getAppearance("ball")));
            spawned++;
        }
        reportScore(spawned);   // leaderboard: total balls launched
    }

    /** Clamps an x coordinate inside the box (keeping the ball off the walls). */
    private static double clampX(double x) {
        return Math.max(WALL_THICKNESS + BALL_RADIUS,
                Math.min(x, WORLD_WIDTH - WALL_THICKNESS - BALL_RADIUS));
    }

    /** Clamps a y coordinate inside the box (keeping the ball off the walls). */
    private static double clampY(double y) {
        return Math.max(WALL_THICKNESS + BALL_RADIUS,
                Math.min(y, WORLD_HEIGHT - WALL_THICKNESS - BALL_RADIUS));
    }

    /** Steps the simulation and recycles balls once the cap is exceeded. */
    @Override
    public void step() {
        world.update(getStep());

        // Keep the live-ball count bounded: drop one ball per frame while over
        // the cap (cheap, and visually unnoticeable at the cap).
        while (countCircles() > MAX_BALLS) {
            Shape oldest = world.findShape(s -> s instanceof Circle);
            if (oldest == null) break;
            world.removeShape(oldest);
        }
    }

    /** @return how many circles (balls) currently live in the world. */
    private int countCircles() {
        int n = 0;
        for (Shape s : world) if (s instanceof Circle) n++;
        return n;
    }
}
