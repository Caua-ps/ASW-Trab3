package lob.gaming;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Singleton registry of every {@link Player} known to the system.
 *
 * <p>Design pattern: <b>Singleton</b> &mdash; the FAQ explicitly requires
 * {@link #getInstance()} as part of the pattern. Persistence uses Java
 * serialization with a configurable backing file
 * ({@link #setPlayersFile(File)}), so a freshly started JVM that points
 * at an existing file recovers the roster.
 */
public class Players implements Serializable {
    private static final long serialVersionUID = 1L;

    /** The single instance, lazily created. */
    private static Players   instance;
    /** Optional backing file used for serialization. */
    private static File      playersFile;

    /** In-memory list of registered players. */
    private final List<Player> playersList = new ArrayList<>();

    /** Private constructor &mdash; part of the Singleton pattern. */
    private Players() {}

    /** @return the (lazily created) single instance. */
    public static synchronized Players getInstance() {
        if (instance == null) {
            instance = new Players();
            instance.load();
        }
        return instance;
    }

    // ---------------------------------------------------------------- backing file

    /** Sets the backing file used for serialization and reloads it. */
    public static void setPlayersFile(File file) {
        playersFile = file;
        if (instance != null) instance.load();
    }

    /** {@code String}-path overload of {@link #setPlayersFile(File)}. */
    public static void setPlayersFile(String pathname) {
        setPlayersFile(new File(pathname));
    }

    /** @return the current backing file, or {@code null}. */
    public static File getPlayersFile() { return playersFile; }

    // ---------------------------------------------------------------- (de)serialization

    /** Replaces the in-memory list with whatever is on disk (silently
     *  ignores I/O or class-cast errors). */
    @SuppressWarnings("unchecked")
    private void load() {
        playersList.clear();
        if (playersFile == null || !playersFile.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(playersFile))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                for (Object o : (List<?>) obj) {
                    if (o instanceof Player) playersList.add((Player) o);
                }
            }
        } catch (Exception ignored) {}
    }

    /** Writes the in-memory list to the backing file (no-op if none). */
    private void save() {
        if (playersFile == null) return;
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(playersFile))) {
            oos.writeObject(new ArrayList<>(playersList));
        } catch (IOException ignored) {}
    }

    // ---------------------------------------------------------------- registry

    /**
     * Clears the in-memory list and persists the empty list. Package-private
     * by design &mdash; used only by tests to reset the singleton between
     * runs (see FAQ section <i>Codificação</i>).
     */
    void reset() {
        playersList.clear();
        save();
    }

    /**
     * Registers a brand-new player.
     *
     * @return the new {@link Player}, or {@code null} if the nickname is
     *         invalid (null, contains a space) or already in use.
     */
    public Player register(String nick, String name) {
        if (nick == null || nick.contains(" ")) return null;
        if (getPlayer(nick) != null)             return null;

        Player p = new Player(nick, name);
        playersList.add(p);
        save();
        return p;
    }

    /** @return the player with this nickname, or {@code null}. */
    public Player getPlayer(String nick) {
        for (Player p : playersList)
            if (Objects.equals(p.getNick(), nick)) return p;
        return null;
    }

    /** Returns the existing player with this nick, or creates a new one. */
    public Player getOrCreatePlayer(String nick, String name) {
        Player p = getPlayer(nick);
        if (p == null) {
            p = new Player(nick, name);
            playersList.add(p);
            save();
        }
        return p;
    }

    /**
     * Authenticates a player by full key match.
     *
     * @return {@code true} iff a player with this {@code nick} exists and
     *         their key equals {@code key} verbatim (substrings fail).
     */
    public boolean authenticate(String nick, String key) {
        Player p = getPlayer(nick);
        if (p == null) return false;
        return p.generateKey().equals(key);
    }
}
