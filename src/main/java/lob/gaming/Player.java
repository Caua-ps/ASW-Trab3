package lob.gaming;

import java.io.Serializable;
import java.util.UUID;

/**
 * A registered player &mdash; identified by a unique {@link #nick} (no spaces)
 * and a display {@link #name}, plus an authentication {@link #key} generated
 * once at construction.
 *
 * <p>Implements {@link Serializable} so {@link Players} can persist its
 * roster to disk via Java serialization (see project FAQ).
 *
 * <p>Equality is based on {@link #nick} alone &mdash; this is what lets
 * {@link Players#getOrCreatePlayer(String, String)} return the same logical
 * player across calls.
 */
public class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String nick;
    private final String name;
    private final String key;

    /** @param nick unique identifier, must not contain spaces.
     *  @param name human-readable display name. */
    public Player(String nick, String name) {
        this.nick = nick;
        this.name = name;
        // UUID is long enough that any truncation will fail authenticate().
        this.key  = UUID.randomUUID().toString();
    }

    /** @return the unique nickname. */
    public String getNick() { return nick; }
    /** @return the display name. */
    public String getName() { return name; }

    /** @return this player's authentication key. */
    public String generateKey() { return key; }

    /** Equal iff the {@link #nick} matches. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        return nick.equals(((Player) o).nick);
    }

    /** Hash consistent with {@link #equals(Object)}. */
    @Override
    public int hashCode() { return nick.hashCode(); }
}
