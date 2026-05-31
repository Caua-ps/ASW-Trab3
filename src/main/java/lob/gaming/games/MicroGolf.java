package lob.gaming.games;

import lob.gaming.GameAnimation;
import lob.physics.Vector2D;
import lob.physics.forces.FrictionStrategy;
import lob.physics.shapes.Circle;
import lob.physics.shapes.Rectangle;
import lob.physics.shapes.Shape;

/**
 * Micro Golf — top-down mini-golf in a portrait field.
 *
 * The field has four border walls plus an L-shaped obstacle hugging the bottom-
 * right corner: a vertical bar coming down from mid-height, with a horizontal
 * arm running along the bottom, all the way to the right wall.  The hole sits
 * on top of the horizontal arm of the L (i.e. just above its top edge).  The
 * ball starts in the bottom-left corner.
 *
 * To win the player has to bounce the ball around the L: typically up the left
 * channel, across the top, and down to the small target on top of the L's arm.
 *
 * Design patterns:
 *   - Strategy:        {@link FrictionStrategy}.
 *   - Template Method: extends {@link GameAnimation}.
 */
public class MicroGolf extends GameAnimation {

    public static final String GAME_NAME         = "Micro Golf";
    public static final String GAME_INSTRUCTIONS =
            "Click to strike the ball. Get it onto the small target — fewest strokes wins.";

    private static final double WORLD_WIDTH  = 380;
    private static final double WORLD_HEIGHT = 480;

    private static final double WALL_THICKNESS = 8;
    private static final double BALL_RADIUS    = 7;
    private static final double STRIKE_SPEED   = 480;
    private static final double FRICTION       = 130;
    /** Below this speed the ball is considered stationary and may be struck again. */
    private static final double REST_THRESHOLD = 8;

    /** L-shape geometry (bottom-right corner). */
    private static final double L_TOP        = WORLD_HEIGHT * 0.55;       // top of the L
    private static final double L_VERT_X     = WORLD_WIDTH  * 0.55;       // x of vertical bar
    private static final double L_BAR_TOP    = WORLD_HEIGHT - 60;          // top of horizontal arm
    private static final double L_THICKNESS  = 10;

    /** Target sits as a small square on top of the L's horizontal arm. */
    private static final double TARGET_SIZE  = 8;
    private static final double TARGET_X     = WORLD_WIDTH - 90;           // centre x
    private static final double TARGET_Y     = L_BAR_TOP - TARGET_SIZE / 2;

    /** The current ball Circle (replaced on every physics step). */
    private Circle    ball;
    /** The hole target. */
    private Rectangle target;
    /** Number of strokes used so far. */
    private int       strokes;
    /** Win flag. */
    private boolean   won;

    /** Builds the world with a friction force and a 0.7 restitution. */
    public MicroGolf() {
        super();
        world.setForceStrategy(new FrictionStrategy(FRICTION));
        world.setRestitution(0.7);
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

    /** @return number of strokes used. */
    public int     getStrokes() { return strokes; }
    /** @return {@code true} once the ball is on the target. */
    public boolean isWon()      { return won; }

    /** Resets the world: rebuild walls, L-obstacle, target and ball. */
    @Override
    public void resetGame() {
        world.reset();
        strokes = 0;
        won = false;

        // Border walls (top, bottom, left, right)
        world.addShape(new Rectangle(0, 0,
                WORLD_WIDTH, WALL_THICKNESS, getAppearance("wall")));
        world.addShape(new Rectangle(0, WORLD_HEIGHT - WALL_THICKNESS,
                WORLD_WIDTH, WORLD_HEIGHT, getAppearance("wall")));
        world.addShape(new Rectangle(0, 0,
                WALL_THICKNESS, WORLD_HEIGHT, getAppearance("wall")));
        world.addShape(new Rectangle(WORLD_WIDTH - WALL_THICKNESS, 0,
                WORLD_WIDTH, WORLD_HEIGHT, getAppearance("wall")));

        // L-shape (vertical part)
        world.addShape(new Rectangle(L_VERT_X - L_THICKNESS / 2, L_TOP,
                L_VERT_X + L_THICKNESS / 2, L_BAR_TOP, getAppearance("wall")));
        // L-shape (horizontal arm running to the right wall)
        world.addShape(new Rectangle(L_VERT_X - L_THICKNESS / 2, L_BAR_TOP,
                WORLD_WIDTH - WALL_THICKNESS, L_BAR_TOP + L_THICKNESS,
                getAppearance("wall")));

        // Target — small square on top of the horizontal arm
        target = new Rectangle(TARGET_X - TARGET_SIZE / 2,
                               TARGET_Y - TARGET_SIZE / 2,
                               TARGET_X + TARGET_SIZE / 2,
                               TARGET_Y + TARGET_SIZE / 2,
                               getAppearance("target"));
        world.addShape(target);

        // Ball — bottom-left corner
        ball = new Circle(new Vector2D(40, WORLD_HEIGHT - 40),
                          Vector2D.NULL_VECTOR,
                          BALL_RADIUS,
                          getAppearance("ball"));
        world.addShape(ball);
    }

    /** Strike the ball towards the click point (only if it's at rest). */
    public void strikeBall(Vector2D clickPoint) {
        if (won) return;

        Shape s = world.findShape(x -> x instanceof Circle);
        if (!(s instanceof Circle current)) return;
        if (current.getVelocity().length() > REST_THRESHOLD) return;

        Vector2D pos = current.getPosition();
        Vector2D diff = clickPoint.minus(pos);
        if (diff.length() < 0.01) return;

        Vector2D vel = diff.normalize().multiply(STRIKE_SPEED);
        Circle next = new Circle(pos, vel, current.radius(), current.appearance());
        world.removeShape(current);
        world.addShape(next);
        ball = next;
        strokes++;
    }

    /** Steps the simulation; checks for win and clamps tiny velocities to rest. */
    @Override
    public void step() {
        if (won) return;

        // Win check BEFORE update — generous threshold so the ball is "in"
        // the moment it overlaps the target rectangle.
        if (ballOverlapsTarget()) { winNow(); return; }

        world.update(getStep());

        Shape s = world.findShape(x -> x instanceof Circle);
        if (!(s instanceof Circle c)) return;
        ball = c;

        if (ballOverlapsTarget()) { winNow(); return; }

        // Friction can leave a residual sub-pixel velocity — clamp small
        // velocities to 0 so subsequent strikes are accepted.
        Vector2D v = c.getVelocity();
        if (v.length() > 0 && v.length() < REST_THRESHOLD) {
            Circle stopped = new Circle(c.getPosition(), Vector2D.NULL_VECTOR,
                    c.radius(), c.appearance());
            world.removeShape(c);
            world.addShape(stopped);
            ball = stopped;
        }
    }

    /** @return whether the ball circle overlaps the target rectangle. */
    private boolean ballOverlapsTarget() {
        Shape s = world.findShape(x -> x instanceof Circle);
        if (!(s instanceof Circle c)) return false;
        Vector2D bp = c.getPosition();
        // closest point on target rectangle to ball centre
        double cx = Math.max(target.xMin(), Math.min(bp.x(), target.xMax()));
        double cy = Math.max(target.yMin(), Math.min(bp.y(), target.yMax()));
        double dx = bp.x() - cx, dy = bp.y() - cy;
        return dx * dx + dy * dy <= BALL_RADIUS * BALL_RADIUS;
    }

    /** Marks the game as won and shows the final stroke count. */
    private void winNow() {
        won = true;
        showMessage("On target! Strokes: " + strokes);
        reportScore(Math.max(1, 100 - (strokes - 1) * 10));  // leaderboard: fewer strokes = more points
    }
}
