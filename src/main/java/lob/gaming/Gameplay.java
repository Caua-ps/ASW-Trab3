package lob.gaming;

/**
 * Minimal contract every game must satisfy: identification, dimensions
 * and the simulation hooks invoked once per frame ({@link #step()}) or
 * when the player restarts ({@link #resetGame()}).
 *
 * <p>The {@link GameAnimation} abstract class implements this interface
 * and adds the boilerplate (frame loop, world setup, listeners), so
 * concrete games extend {@code GameAnimation} rather than implement
 * {@code Gameplay} directly.
 */
public interface Gameplay {
    /** @return the human-readable name shown in menus and leaderboards. */
    String getName();
    /** @return a short description of the game's controls and goal. */
    String getInstructions();
    /** @return the world width in simulation units. */
    double getWidth();
    /** @return the world height in simulation units. */
    double getHeight();
    /** Restarts the game from its initial state. */
    void   resetGame();
    /** Advances the simulation by one frame. */
    void   step();
}
