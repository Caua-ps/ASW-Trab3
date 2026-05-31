package lob.gaming.games;

import lob.gaming.GameAnimation;
import lob.physics.Vector2D;
import lob.physics.forces.NoForceStrategy;
import lob.physics.shapes.Circle;
import lob.physics.shapes.Rectangle;
import lob.physics.shapes.Shape;

import java.util.HashSet;
import java.util.Set;

/**
 * Arkanoid Knockoff — paddle, ball, bricks.
 *
 * No force on the ball (perfectly elastic 2D plane). The player moves the
 * paddle with the mouse and clicks to launch the ball. Bricks are removed
 * when the ball hits them. Lose if the ball falls off the bottom.
 *
 * Design patterns:
 *   - Strategy:        {@link NoForceStrategy}.
 *   - Template Method: extends {@link GameAnimation}.
 */
public class ArkanoidKnockoff extends GameAnimation {

    public static final String GAME_NAME         = "Arkanoid Knockoff";
    public static final String GAME_INSTRUCTIONS =
            "Mouse moves the paddle. Click to launch. Break all the bricks!";

    private static final double WORLD_WIDTH  = 500;
    private static final double WORLD_HEIGHT = 600;

    private static final double WALL_THICKNESS = 12;

    private static final double PADDLE_WIDTH  = 90;
    private static final double PADDLE_HEIGHT = 12;
    private static final double PADDLE_Y      = WORLD_HEIGHT - 40;

    private static final double BALL_RADIUS = 8;
    private static final double BALL_SPEED  = 320;

    private static final int    BRICK_ROWS    = 4;
    private static final int    BRICK_COLS    = 8;
    private static final double BRICK_W       = (WORLD_WIDTH - 2 * WALL_THICKNESS) / BRICK_COLS;
    private static final double BRICK_H       = 22;
    private static final double BRICK_TOP     = 60;
    private static final double BRICK_GAP     = 2;

    private static final String[] BRICK_COLORS = {
            "red brick", "orange brick", "yellow brick", "blue brick"
    };

    /** All current brick shapes (Set so we can check membership cheaply). */
    private final Set<Rectangle> bricks = new HashSet<>();

    /** The paddle &mdash; a Rectangle replaced on every move. */
    private Rectangle paddle;
    /** The current ball Circle (replaced on every physics step). */
    private Circle    ball;
    /** {@code true} once the player has launched the ball. */
    private boolean   launched;
    /** Win and loss flags. */
    private boolean   won, lost;

    /** Builds a new Arkanoid game with no force and elastic bounces. */
    public ArkanoidKnockoff() {
        super();
        world.setForceStrategy(new NoForceStrategy());
        world.setRestitution(1.0);
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

    /** @return number of bricks remaining. */
    public int     getBricksLeft() { return bricks.size(); }
    /** @return {@code true} once the ball has been launched. */
    public boolean isLaunched()    { return launched; }
    /** @return {@code true} once every brick is destroyed. */
    public boolean isWon()         { return won; }
    /** @return {@code true} once the ball has fallen off the bottom. */
    public boolean isLost()        { return lost; }

    /** Resets the world: rebuilds walls, bricks, paddle and ball. */
    @Override
    public void resetGame() {
        world.reset();
        bricks.clear();
        launched = false;
        won = false;
        lost = false;

        // Border walls (left, right, top — bottom is open so we can lose)
        world.addShape(new Rectangle(0, 0,
                WALL_THICKNESS, WORLD_HEIGHT, getAppearance("wall")));
        world.addShape(new Rectangle(WORLD_WIDTH - WALL_THICKNESS, 0,
                WORLD_WIDTH, WORLD_HEIGHT, getAppearance("wall")));
        world.addShape(new Rectangle(0, 0,
                WORLD_WIDTH, WALL_THICKNESS, getAppearance("wall")));

        // Bricks — each one listens for a collision and removes itself when hit.
        // The listener is registered on the BRICK (a stable Rectangle), not on the
        // ball (a Circle, which changes identity on every physics step).
        for (int row = 0; row < BRICK_ROWS; row++) {
            String color = BRICK_COLORS[row % BRICK_COLORS.length];
            for (int col = 0; col < BRICK_COLS; col++) {
                double x1 = WALL_THICKNESS + col * BRICK_W;
                double y1 = BRICK_TOP + row * BRICK_H;
                final Rectangle brick = new Rectangle(x1 + BRICK_GAP, y1 + BRICK_GAP,
                        x1 + BRICK_W - BRICK_GAP,
                        y1 + BRICK_H - BRICK_GAP,
                        getAppearance(color));
                bricks.add(brick);
                world.addShape(brick);
                world.addCollisionListener(brick, ev -> destroyBrick(brick));
            }
        }

        // Paddle (centred horizontally)
        double px = WORLD_WIDTH / 2 - PADDLE_WIDTH / 2;
        paddle = new Rectangle(px, PADDLE_Y,
                px + PADDLE_WIDTH, PADDLE_Y + PADDLE_HEIGHT,
                getAppearance("paddle"));
        world.addShape(paddle);

        // Ball — sits on top of the paddle
        ball = new Circle(
                new Vector2D(WORLD_WIDTH / 2, PADDLE_Y - BALL_RADIUS - 1),
                Vector2D.NULL_VECTOR,
                BALL_RADIUS,
                getAppearance("ball"));
        world.addShape(ball);
    }

    /** Called by the collision listener when a brick is hit. */
    private void destroyBrick(Rectangle brick) {
        if (!bricks.remove(brick)) return;
        world.removeShape(brick);
        if (bricks.isEmpty() && !won) {
            won = true;
            showMessage("You win! Click to play again.");
            reportScore(BRICK_ROWS * BRICK_COLS);  // leaderboard: bricks cleared
        }
    }

    /** Move the paddle so its centre lines up with the given x. */
    public void movePaddle(int x) {
        if (paddle == null || won || lost) return;

        double half  = PADDLE_WIDTH / 2;
        double newX1 = Math.max(WALL_THICKNESS,
                Math.min(x - half, WORLD_WIDTH - WALL_THICKNESS - PADDLE_WIDTH));
        double newX2 = newX1 + PADDLE_WIDTH;

        Rectangle next = new Rectangle(newX1, PADDLE_Y,
                newX2, PADDLE_Y + PADDLE_HEIGHT, getAppearance("paddle"));
        world.removeShape(paddle);
        paddle = next;
        world.addShape(paddle);

        // If not yet launched, keep the ball glued to the paddle
        if (!launched) {
            Shape s = world.findShape(x2 -> x2 instanceof Circle);
            if (s instanceof Circle c) {
                double cx = (newX1 + newX2) / 2;
                Circle nb = new Circle(new Vector2D(cx, PADDLE_Y - BALL_RADIUS - 1),
                        Vector2D.NULL_VECTOR, c.radius(), c.appearance());
                world.removeShape(c);
                world.addShape(nb);
                ball = nb;
            }
        }
    }

    /** Click handler: launch the ball, or restart if game is over. */
    public void startGame() {
        if (won || lost) {
            resetGame();
            return;
        }
        if (!launched) {
            Shape s = world.findShape(x -> x instanceof Circle);
            if (!(s instanceof Circle c)) return;
            Circle launched = new Circle(c.getPosition(),
                    new Vector2D(BALL_SPEED * 0.5, -BALL_SPEED),
                    c.radius(), c.appearance());
            world.removeShape(c);
            world.addShape(launched);
            ball = launched;
            this.launched = true;
        }
    }

    /** Steps the simulation; checks whether the ball has fallen off the bottom. */
    @Override
    public void step() {
        if (!launched || won || lost) return;

        world.update(getStep());

        Shape s = world.findShape(x -> x instanceof Circle);
        if (!(s instanceof Circle c)) return;
        ball = c;

        // Lose: ball fell off the bottom
        if (c.getPosition().y() > WORLD_HEIGHT + BALL_RADIUS * 2) {
            lost = true;
            showMessage("Game over. Click to try again.");
        }
    }
}
