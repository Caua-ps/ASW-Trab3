package lob.gaming.games;

import lob.gaming.GameAnimation;
import lob.physics.Vector2D;
import lob.physics.forces.MagnetStrategy;
import lob.physics.shapes.Circle;
import lob.physics.shapes.Rectangle;
import lob.physics.shapes.Shape;

import java.util.ArrayList;
import java.util.List;

/**
 * Magnet — a magnetism game (project valorization).
 *
 * <p>The balls are attracted to a magnet that follows the mouse cursor. The
 * player drops balls with a click and drags the swarm around by moving the
 * mouse, with the goal of guiding the balls into the gold <b>target</b> in the
 * corner. Each ball that reaches the target scores a point and disappears.
 *
 * <p>Uses {@link MagnetStrategy} (the <b>Strategy</b> pattern, like gravity)
 * and is auto-discovered by {@link lob.gaming.ReflectGameFactory} thanks to its
 * {@code GAME_NAME} field and no-arg constructor.
 */
public class Magnet extends GameAnimation {

    /** Game name registered in the {@link lob.gaming.ReflectGameFactory}. */
    public static final String GAME_NAME         = "Magnet";
    /** Short human-readable instructions. */
    public static final String GAME_INSTRUCTIONS =
            "Move the mouse: the balls follow the magnet. Click to drop more. "
            + "Guide them into the gold target!";

    private static final double WORLD_WIDTH  = 600;
    private static final double WORLD_HEIGHT = 460;
    private static final double WALL         = 10;

    private static final double BALL_RADIUS  = 9;
    private static final int    BURST        = 6;
    private static final int    START_BALLS  = 12;
    private static final int    MAX_BALLS    = 120;
    private static final double STRENGTH     = 900;

    // Target: a square zone in the bottom-right corner.
    private static final double TARGET_SIZE = 80;
    private static final double TARGET_X    = WORLD_WIDTH  - WALL - TARGET_SIZE;
    private static final double TARGET_Y    = WORLD_HEIGHT - WALL - TARGET_SIZE;

    /** The movable magnet the balls are attracted to. */
    private final MagnetStrategy magnet = new MagnetStrategy(STRENGTH);

    /** Balls delivered to the target this session (the leaderboard score). */
    private int caught;

    /** Builds the box, the target, and a handful of starting balls. */
    public Magnet() {
        super();
        world.setForceStrategy(magnet);
        world.setRestitution(0.6);
        magnet.setMagnet(new Vector2D(WORLD_WIDTH / 2, WORLD_HEIGHT / 2));
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

    /** @return balls delivered to the target this session. */
    public int getCaught() { return caught; }

    /**
     * Moves the magnet — typically bound to mouse movement so the balls follow
     * the cursor.
     *
     * @param point the magnet location, in world coordinates.
     */
    public void setMagnet(Vector2D point) {
        magnet.setMagnet(point);
    }

    /** Rebuilds the walls, the target, and the starting balls. */
    @Override
    public void resetGame() {
        world.reset();
        caught = 0;

        // Four border walls.
        world.addShape(new Rectangle(0, 0, WORLD_WIDTH, WALL, getAppearance("wall")));
        world.addShape(new Rectangle(0, WORLD_HEIGHT - WALL, WORLD_WIDTH, WORLD_HEIGHT, getAppearance("wall")));
        world.addShape(new Rectangle(0, 0, WALL, WORLD_HEIGHT, getAppearance("wall")));
        world.addShape(new Rectangle(WORLD_WIDTH - WALL, 0, WORLD_WIDTH, WORLD_HEIGHT, getAppearance("wall")));

        // The target zone (a coloured square; not magnetic).
        world.addShape(new Rectangle(TARGET_X, TARGET_Y,
                TARGET_X + TARGET_SIZE, TARGET_Y + TARGET_SIZE, getAppearance("target")));

        // A few starting balls in the top-left area.
        for (int i = 0; i < START_BALLS; i++) {
            spawnOne(WALL + BALL_RADIUS + Math.random() * 140,
                     WALL + BALL_RADIUS + Math.random() * 140);
        }
    }

    /**
     * Drops a burst of balls at the click point (positions jittered so no two
     * share the exact same coordinates).
     *
     * @param at the click point, in world coordinates.
     */
    public void spawn(Vector2D at) {
        for (int i = 0; i < BURST; i++) {
            double angle  = (2 * Math.PI * i) / BURST + Math.random() * 0.4;
            double radius = BALL_RADIUS * 1.6 + Math.random() * BALL_RADIUS;
            spawnOne(at.x() + Math.cos(angle) * radius,
                     at.y() + Math.sin(angle) * radius);
        }
    }

    /** Adds one ball at a clamped position, at rest. */
    private void spawnOne(double x, double y) {
        world.addShape(new Circle(new Vector2D(clampX(x), clampY(y)),
                Vector2D.NULL_VECTOR, BALL_RADIUS, getAppearance("ball")));
    }

    /** Steps the simulation, catches balls on the target, and caps the count. */
    @Override
    public void step() {
        world.update(getStep());

        double targetCx = TARGET_X + TARGET_SIZE / 2;
        double targetCy = TARGET_Y + TARGET_SIZE / 2;
        double reach    = TARGET_SIZE / 2 + BALL_RADIUS;

        // Collect balls that have reached the target.
        List<Shape> reached = new ArrayList<>();
        for (Shape s : world) {
            if (s instanceof Circle ball) {
                double dx = Math.abs(ball.position().x() - targetCx);
                double dy = Math.abs(ball.position().y() - targetCy);
                if (dx <= reach && dy <= reach) reached.add(ball);
            }
        }
        for (Shape s : reached) {
            world.removeShape(s);
            caught++;
        }
        if (!reached.isEmpty()) {
            reportScore(caught);                 // leaderboard: balls delivered
            showMessage("Caught: " + caught);
        }

        // Keep the live-ball count bounded.
        while (countCircles() > MAX_BALLS) {
            Shape oldest = world.findShape(s -> s instanceof Circle);
            if (oldest == null) break;
            world.removeShape(oldest);
        }
    }

    /** @return how many balls currently live in the world. */
    private int countCircles() {
        int n = 0;
        for (Shape s : world) if (s instanceof Circle) n++;
        return n;
    }

    private static double clampX(double x) {
        return Math.max(WALL + BALL_RADIUS, Math.min(x, WORLD_WIDTH - WALL - BALL_RADIUS));
    }

    private static double clampY(double y) {
        return Math.max(WALL + BALL_RADIUS, Math.min(y, WORLD_HEIGHT - WALL - BALL_RADIUS));
    }
}
