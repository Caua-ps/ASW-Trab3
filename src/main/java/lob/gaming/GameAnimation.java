package lob.gaming;

import lob.LotsOfBallsException;
import lob.physics.engine.PhysicsWorld;
import lob.physics.shapes.Appearance;
import lob.physics.shapes.FrameShower;

import java.util.function.Consumer;

/**
 * Abstract base class for every concrete game.
 *
 * <p>Implements the boilerplate every game needs:
 * <ul>
 *   <li>a {@link PhysicsWorld} sized to {@link #getWidth()} ×
 *       {@link #getHeight()};</li>
 *   <li>{@link Runnable}-based animation loop driven by a configurable
 *       FPS;</li>
 *   <li>plug-in {@link FrameShower} and {@link Consumer message shower}
 *       hooks so the GUI can subscribe;</li>
 *   <li>a static {@link AppearanceFactory} so visuals can be swapped
 *       without recompiling games.</li>
 * </ul>
 *
 * <p>Design pattern: <b>Template Method</b> &mdash; this class defines the
 * skeleton ({@link #loop()}) and delegates {@link #step()} and
 * {@link #resetGame()} to subclasses.
 */
public abstract class GameAnimation implements Gameplay, Runnable {

    // ---------------------------------------------------------------- static configuration

    /** Static appearance factory shared by every game. */
    private static AppearanceFactory appearanceFactory = null;
    /** Animation frame rate. */
    private static int fps = 60;

    // ---------------------------------------------------------------- per-instance state

    /** Whether the animation thread is currently running. */
    private volatile boolean running = false;

    /** No-op default so {@link #getMessageShower()} is never {@code null}. */
    private Consumer<String> messageShower = msg -> {};
    /** No-op default so {@link #getFrameShower()} is never {@code null}. */
    private FrameShower      frameShower   = world -> {};

    /** The simulation world. Available to subclasses. */
    protected PhysicsWorld world;

    // ---------------------------------------------------------------- leaderboard binding

    /**
     * Player whose results should be recorded in the
     * {@link LeaderboardManager}, or {@code null} when the game is not bound
     * to any player (the default — e.g. in unit tests).
     */
    private Player leaderboardPlayer = null;

    /**
     * Binds this game to a {@link Player}: from now on, calls to
     * {@link #reportScore(int)} are persisted in the {@link LeaderboardManager}
     * under that player's nick and this game's {@link #getName() name}.
     *
     * <p>This is the hook the Vaadin panels use (see
     * {@code lob.app.games.*Panel.onAttach}): when the player navigates to a
     * game, the panel binds the current player so scoring events land in the
     * leaderboard.
     *
     * @param player the player to credit, or {@code null} to unbind.
     */
    public void bindToLeaderboard(Player player) {
        this.leaderboardPlayer = player;
    }

    /** @return the player currently bound to the leaderboard, or {@code null}. */
    public Player getLeaderboardPlayer() { return leaderboardPlayer; }

    /**
     * Records a score for the bound player in the {@link LeaderboardManager}.
     * No-op when no player is bound (so the model and its tests run without
     * touching the leaderboard).
     *
     * @param points the score to record (higher is better).
     */
    protected void reportScore(int points) {
        Player player = leaderboardPlayer;
        if (player == null) return;
        try {
            LeaderboardManager.getInstance()
                    .logGameResult(player.getNick(), getName(), points);
        } catch (LotsOfBallsException e) {
            showMessage("Could not record score: " + e.getMessage());
        }
    }

    /** Read-only view of the current shapes &mdash; for UI / tests. */
    public Iterable<lob.physics.shapes.Shape> getShapes() { return world; }

    /** Builds the world eagerly using the subclass's declared dimensions. */
    public GameAnimation() {
        this.world = new PhysicsWorld(getWidth(), getHeight());
    }

    // ---------------------------------------------------------------- animation loop

    /** @return whether the animation thread is currently active. */
    public boolean isRunning() { return running; }

    /**
     * Starts the animation on a background daemon thread. Returns
     * immediately. Throws {@link IllegalStateException} if the game is
     * already running &mdash; required by {@code GameAnimationTest}.
     *
     * <p>When the loop ends, {@link #resetGame()} is called once so the
     * game returns to a clean state.
     */
    public synchronized void start() {
        if (running) throw new IllegalStateException("Game is already running");
        running = true;

        Thread thread = new Thread(() -> {
            try {
                loop();
            } finally {
                running = false;
                resetGame();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /** Signals the loop to exit at the next iteration. */
    public void stop() { running = false; }

    /** Frame loop: step the simulation, show the frame, sleep until the next tick. */
    private void loop() {
        long frameMicros = (long)(1_000_000.0 / fps);

        while (running) {
            long start = System.nanoTime();

            step();
            showFrame();

            long elapsed = (System.nanoTime() - start) / 1000;
            long sleep   = frameMicros - elapsed;
            if (sleep > 0) {
                try { Thread.sleep(sleep / 1000, (int)((sleep % 1000) * 1000)); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
        }
    }

    // ---------------------------------------------------------------- message shower

    /** @return the current message shower (never {@code null}). */
    public Consumer<String> getMessageShower() { return messageShower; }

    /** Replaces the message shower. */
    public void setMessageShower(Consumer<String> shower) {
        this.messageShower = shower;
    }

    /** Sends {@code message} to the registered shower. */
    public void showMessage(String message) {
        if (messageShower != null) messageShower.accept(message);
    }

    // ---------------------------------------------------------------- frame shower

    /** @return the current frame shower (never {@code null}). */
    public FrameShower getFrameShower() { return frameShower; }

    /** Replaces the frame shower (e.g. with a Swing GUI). */
    public void setFrameShower(FrameShower shower) {
        this.frameShower = shower;
    }

    /** Renders one frame via the registered shower. */
    public void showFrame() {
        if (frameShower != null) frameShower.show(world);
    }

    // ---------------------------------------------------------------- FPS / step

    /** @return the global frame rate. */
    public static int    getFPS()  { return fps; }
    /** Sets the global frame rate. */
    public static void   setFPS(int newFps) { fps = newFps; }
    /** @return the time step in seconds: {@code 1 / fps}. */
    public static double getStep() { return fps > 0 ? 1.0 / fps : 0.0; }

    // ---------------------------------------------------------------- appearance factory

    /** @return the global appearance factory, or {@code null} if none set. */
    public static AppearanceFactory getAppearanceFactory() { return appearanceFactory; }

    /** Replaces the global appearance factory. */
    public static void setAppearanceFactory(AppearanceFactory factory) {
        appearanceFactory = factory;
    }

    /** Looks up an appearance by name, delegating to the factory. */
    public static Appearance getAppearance(String name) {
        return appearanceFactory != null ? appearanceFactory.getAppearance(name) : null;
    }

    // ---------------------------------------------------------------- Runnable

    /** Default {@link Runnable} body &mdash; runs the loop synchronously. */
    @Override
    public void run() {
        loop();
    }

    // ---------------------------------------------------------------- abstract API

    /** {@inheritDoc} */
    @Override public abstract String getName();
    /** {@inheritDoc} */
    @Override public abstract String getInstructions();
    /** {@inheritDoc} */
    @Override public abstract double getWidth();
    /** {@inheritDoc} */
    @Override public abstract double getHeight();
    /** {@inheritDoc} */
    @Override public abstract void   resetGame();
    /** {@inheritDoc} */
    @Override public abstract void   step();
}
