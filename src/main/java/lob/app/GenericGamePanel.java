package lob.app;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import lob.gaming.GameAnimation;

import java.util.Map;


/**
 * A class providing a method for game initializations.
 *
 * <p>This class is similar to {@code lob.guis.GenericGameGui} but adapted to
 * Vaadin and making use of {@link WorldViewer}.
 *
 * <p>Provided by the teaching team and <b>extended here</b>: {@link #init} now
 * also installs the {@link lob.gaming.AppearanceFactory} for the game, reading
 * the colour map from the concrete panel's {@code getAppearanceColors()} via
 * reflection (the same reflection style used by
 * {@link lob.gaming.ReflectGameFactory}). This keeps each {@code *Panel} clean:
 * a subclass only declares its colours by overriding {@code getAppearanceColors()}.
 *
 * @author Jos&eacute; Paulo Leal {@code jpleal@fc.up.pt} (original)
 */
public class GenericGamePanel extends Div {

    /**
     * Extensions should redefine this method to specify the colours of their
     * shapes.
     *
     * @return map of appearance names to CSS colours.
     */
    public static Map<String,String> getAppearanceColors() {
        return Map.of();
    }

    protected WorldViewer viewer;

    /** The game wired into this panel — started on attach, stopped on detach. */
    protected GameAnimation animation;

    /**
     * Perform game initializations using a game based on {@link GameAnimation}.
     *
     * <p>Installs the appearance factory from this panel's colour map (resolved
     * reflectively, so the concrete subclass's {@code getAppearanceColors()} is
     * used), wires the viewer as the game's frame/message shower, and rebuilds
     * the game's shapes so they pick up the freshly-installed colours.
     *
     * <p>The animation loop itself is started in {@link #onAttach} (and stopped
     * in {@link #onDetach}), so the simulation only runs while the panel is
     * actually on screen — and its background thread is released when the
     * player navigates away.
     *
     * @param animation providing basic configurations.
     */
    protected void init(GameAnimation animation) {

        this.animation = animation;

        viewer = new WorldViewer( (int) animation.getWidth(), (int) animation.getHeight() );

        add(viewer);

        animation.setMessageShower( viewer::showMessage );
        animation.setFrameShower( viewer::showFrame );

        // Install this game's colours. The concrete panel's static
        // getAppearanceColors() is resolved by reflection, mirroring the
        // factory-by-reflection approach used in lob.gaming.
        GameAnimation.setAppearanceFactory(
                new WorldViewer.ColloredAppearanceFactory(resolveColors()));

        // Rebuild shapes now that the colours are available.
        animation.resetGame();

        // Instructions should be shown when the panel is activated
        // viewer.showMessage( animation.getInstructions() );
    }

    /** Starts the animation loop when the panel becomes visible. */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (animation != null && !animation.isRunning()) {
            animation.start();
        }
    }

    /** Stops the animation loop (releasing its thread) when navigating away. */
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (animation != null) {
            animation.stop();
        }
        super.onDetach(detachEvent);
    }

    /**
     * Resolves the colour map declared by the concrete panel. Falls back to
     * {@link #getAppearanceColors()} (an empty map) if no override is found.
     *
     * @return the colour map for the concrete panel.
     */
    @SuppressWarnings("unchecked")
    private Map<String,String> resolveColors() {
        try {
            return (Map<String,String>) getClass()
                    .getMethod("getAppearanceColors")
                    .invoke(null);
        } catch (ReflectiveOperationException | ClassCastException e) {
            return getAppearanceColors();
        }
    }
}
