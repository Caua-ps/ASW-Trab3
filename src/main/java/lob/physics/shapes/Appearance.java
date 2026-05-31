package lob.physics.shapes;

/**
 * Marker interface for the visual aspect of a {@link Shape}.
 *
 * <p>The physics engine treats this opaquely &mdash; it never inspects the
 * appearance, only carries it around so the GUI layer can render shapes
 * appropriately. Concrete implementations (colour, sprite, texture) live
 * in the GUI package; here we just need a common <i>type</i> to put in
 * shape signatures.
 *
 * <p>This is the {@code Appearance} type discussed in the FAQ
 * (<i>"a interface {@code Appearance} é marcadora"</i>).
 */
public interface Appearance {
    // Intentionally empty: this is a marker interface.
}
