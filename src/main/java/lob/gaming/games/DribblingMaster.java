package lob.gaming.games;

import lob.gaming.GameAnimation;
import lob.physics.Vector2D;
import lob.physics.forces.GravityStrategy;
import lob.physics.shapes.Circle;
import lob.physics.shapes.Rectangle;
import lob.physics.shapes.Shape;

/**
 * Dribbling Master — keep a basketball bouncing and steer it onto the target.
 *
 * The field is wide and short and only has a floor (no top, left or right walls).
 * The ball starts hanging in the air at the upper-left.  The player clicks
 * <i>near</i> the ball to alter its velocity (impulse pointing away from the
 * click).  When the ball hits the floor it bounces but loses some energy.
 *
 * The target is a small narrow strip sitting on top of the floor on the right.
 * Game is lost if the ball leaves the window.
 *
 * Design patterns:
 *   - Strategy:        {@link GravityStrategy}.
 *   - Template Method: extends {@link GameAnimation}.
 */
public class DribblingMaster extends GameAnimation {

    public static final String GAME_NAME         = "Dribbling Master";
    public static final String GAME_INSTRUCTIONS =
            "Click near the ball to alter its velocity. Bounce it onto the target on the right.";

    private static final double WORLD_WIDTH  = 500;
    private static final double WORLD_HEIGHT = 400;

    private static final double GRAVITY        = 800;
    private static final double FLOOR_THICK    = 6;
    private static final double BALL_RADIUS    = 20;
    private static final double STRIKE_IMPULSE = 320;
    /** Click only counts as a strike if it is at most this far from the ball. */
    private static final double STRIKE_RADIUS  = 80;

    private static final double TARGET_W = 22, TARGET_H = 4;

    /** The current ball Circle (replaced on every physics step). */
    private Circle    ball;
    /** The target rectangle, fixed for the whole game. */
    private Rectangle target;
    /** Win and loss flags. */
    private boolean   won, lost;

    /** Builds a new game with downward gravity and a 0.7 restitution. */
    public DribblingMaster() {
        super();
        world.setForceStrategy(new GravityStrategy(new Vector2D(0, GRAVITY)));
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

    /** @return {@code true} once the ball has settled on the target. */
    public boolean isWon()  { return won; }
    /** @return {@code true} once the ball has left the window. */
    public boolean isLost() { return lost; }

    /** Resets the world: rebuild floor, target and ball; clear flags. */
    @Override
    public void resetGame() {
        world.reset();
        won = false;
        lost = false;

        // Floor — the only wall in the field
        world.addShape(new Rectangle(0, WORLD_HEIGHT - FLOOR_THICK,
                WORLD_WIDTH, WORLD_HEIGHT, getAppearance("floor")));

        // Target — a small narrow strip on top of the floor, on the right side
        double tx = WORLD_WIDTH - 30 - TARGET_W;
        double ty = WORLD_HEIGHT - FLOOR_THICK - TARGET_H;
        target = new Rectangle(tx, ty, tx + TARGET_W, ty + TARGET_H,
                getAppearance("target"));
        world.addShape(target);

        // Ball — placed in the upper-left, hovering in the air at mid-height
        ball = new Circle(new Vector2D(120, WORLD_HEIGHT * 0.45),
                          Vector2D.NULL_VECTOR,
                          BALL_RADIUS,
                          getAppearance("basketball"));
        world.addShape(ball);
    }

    /**
     * Player clicks near the ball: a velocity impulse is added to it pointing
     * <i>away</i> from the click (so a click on the left of the ball pushes
     * it to the right).  Clicks farther than {@link #STRIKE_RADIUS} are ignored.
     */
    public void strikeBall(Vector2D clickPoint) {
        if (won || lost) return;

        Shape s = world.findShape(x -> x instanceof Circle);
        if (!(s instanceof Circle current)) return;

        Vector2D ballPos = current.getPosition();
        Vector2D diff    = ballPos.minus(clickPoint);
        double   dist    = diff.length();
        if (dist < 0.01 || dist > STRIKE_RADIUS) return;

        // Stronger impulse when clicked closer to the ball.
        double power = STRIKE_IMPULSE * (1 - 0.5 * (dist / STRIKE_RADIUS));
        Vector2D impulse = diff.multiply(power / dist);

        Circle next = new Circle(ballPos,
                current.getVelocity().add(impulse),
                current.radius(),
                current.appearance());
        world.removeShape(current);
        world.addShape(next);
        ball = next;
    }

    /** Steps the simulation, then checks for win or out-of-bounds. */
    @Override
    public void step() {
        if (won || lost) return;

        if (ballOnTarget()) { won = true; showMessage("Nice shot! You win."); reportScore(100); return; }

        world.update(getStep());

        Shape s = world.findShape(x -> x instanceof Circle);
        if (!(s instanceof Circle c)) return;
        ball = c;

        if (ballOnTarget()) { won = true; showMessage("Nice shot! You win."); reportScore(100); return; }

        // Lose — ball left the window through any side (no walls on top/sides).
        Vector2D p = c.getPosition();
        if (p.x() < -BALL_RADIUS || p.x() > WORLD_WIDTH + BALL_RADIUS
                || p.y() < -BALL_RADIUS * 4
                || p.y() > WORLD_HEIGHT + BALL_RADIUS) {
            lost = true;
            showMessage("Out of bounds — game over.");
        }
    }

    /** @return {@code true} when the ball overlaps the target rectangle. */
    private boolean ballOnTarget() {
        Shape s = world.findShape(x -> x instanceof Circle);
        if (!(s instanceof Circle c)) return false;
        Vector2D bp = c.getPosition();
        // The ball is "on" the target when its bottom touches the target's top edge,
        // and its centre is horizontally within the target span (with a small grace).
        double cx = Math.max(target.xMin(), Math.min(bp.x(), target.xMax()));
        double cy = Math.max(target.yMin(), Math.min(bp.y(), target.yMax()));
        double dx = bp.x() - cx, dy = bp.y() - cy;
        return dx * dx + dy * dy <= BALL_RADIUS * BALL_RADIUS;
    }
}
