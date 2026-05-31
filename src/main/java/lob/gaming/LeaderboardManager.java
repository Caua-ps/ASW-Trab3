package lob.gaming;

import lob.LotsOfBallsException;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Singleton that records and ranks game results.
 *
 * <p>Design pattern: <b>Singleton</b>, with optional disk persistence via
 * Java serialization &mdash; mirrors the structure of {@link Players}.
 * Each result is a {@link GameResult} record carrying the player's nick,
 * the game name, the date and the score.
 */
public class LeaderboardManager implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Single instance, lazily created. */
    private static LeaderboardManager instance;
    /** Optional backing file. */
    private static File                leaderboardFile;

    /** All recorded results, in insertion order. */
    private final List<GameResult> results = new ArrayList<>();

    /** Private constructor &mdash; Singleton pattern. */
    private LeaderboardManager() {}

    /** @return the lazily-created singleton instance. */
    public static synchronized LeaderboardManager getInstance() {
        if (instance == null) {
            instance = new LeaderboardManager();
            instance.load();
        }
        return instance;
    }

    /** Sets the backing file and reloads. */
    public static void setLeaderboardFile(File file) {
        leaderboardFile = file;
        if (instance != null) instance.load();
    }

    /** {@code String}-path overload of {@link #setLeaderboardFile(File)}. */
    public static void setLeaderboardFile(String path) {
        setLeaderboardFile(new File(path));
    }

    /** @return the current backing file, or {@code null}. */
    public static File getLeaderboardFile() { return leaderboardFile; }

    /** Replaces the in-memory list with whatever is on disk (silently
     *  ignores I/O errors). */
    @SuppressWarnings("unchecked")
    private void load() {
        results.clear();
        if (leaderboardFile == null || !leaderboardFile.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(leaderboardFile))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                for (Object o : (List<?>) obj) {
                    if (o instanceof GameResult) results.add((GameResult) o);
                }
            }
        } catch (Exception ignored) {}
    }

    /** Persists the current list to disk (no-op if no backing file). */
    private void save() {
        if (leaderboardFile == null) return;
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(leaderboardFile))) {
            oos.writeObject(new ArrayList<>(results));
        } catch (IOException ignored) {}
    }

    /**
     * Clears the in-memory list and persists the empty list.
     * Package-private &mdash; for test use only (see FAQ).
     */
    void reset() {
        results.clear();
        save();
    }

    /**
     * Records one game outcome.
     *
     * @throws LotsOfBallsException reserved for future validation errors;
     *         currently never thrown but kept on the signature to satisfy
     *         the API contract.
     */
    public void logGameResult(String nick, String game, int points) throws LotsOfBallsException {
        results.add(new GameResult(nick, game, new Date(), points));
        save();
    }

    /**
     * Top-N results for a given game, sorted by descending score.
     *
     * @param game the game name to filter on.
     * @param size maximum number of entries to return.
     */
    public List<GameResult> getLeaderboard(String game, int size) {
        return results.stream()
                .filter(r -> r.game().equals(game))
                .sorted((a, b) -> b.points() - a.points())
                .limit(size)
                .collect(Collectors.toList());
    }

    /**
     * One row of the leaderboard.
     *
     * @param nick   the player's nickname.
     * @param game   the game name.
     * @param date   when the result was recorded.
     * @param points score achieved (higher is better).
     */
    public record GameResult(String nick, String game, Date date, int points)
            implements Serializable {
        private static final long serialVersionUID = 1L;
    }
}
