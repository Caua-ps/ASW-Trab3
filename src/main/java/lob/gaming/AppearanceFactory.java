package lob.gaming;

import lob.physics.shapes.Appearance;

/**
 * Factory that maps a logical shape name (e.g. {@code "ball"}, {@code "wall"},
 * {@code "target"}) to a concrete {@link Appearance}.
 *
 * <p>Design pattern: <b>Factory Method</b>. Keeps the physics engine
 * appearance-agnostic: it only knows shape <i>names</i>; the presentation
 * layer (Swing or Vaadin) plugs in a real factory that produces the actual
 * visual.
 *
 * <p>Relocated to {@code lob.gaming} (from {@code lob.physics.shapes}) so the
 * Vaadin presentation classes in {@code lob.app} bind to the same type that
 * {@link GameAnimation#setAppearanceFactory(AppearanceFactory)} accepts.
 */
public interface AppearanceFactory {

    /**
     * Returns the appearance associated with a logical name.
     *
     * @param shapeName logical name of the shape (e.g. {@code "ball"}).
     * @return the matching {@link Appearance} — never {@code null}.
     */
    Appearance getAppearance(String shapeName);
}
