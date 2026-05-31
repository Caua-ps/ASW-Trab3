package lob.app.games;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.router.Route;
import lob.app.GenericGamePanel;
import lob.app.LeaderboardPanel;
import lob.app.MainView;
import lob.app.WorldViewer;
import lob.gaming.Player;
import lob.gaming.games.Magnet;
import lob.physics.Vector2D;

import java.util.Map;

/**
 * A Vaadin panel for the Magnet game (project valorization), based on
 * {@link GenericGamePanel}.
 *
 * <p>Mouse movement moves the magnet (the balls follow the cursor) and a click
 * drops a burst of balls.
 */
@Route(layout = MainView.class)
public class MagnetPanel extends GenericGamePanel {

    /** Colour map consumed reflectively by {@link GenericGamePanel#init}. */
    public static Map<String,String> getAppearanceColors() {
        return Map.of(
                "ball",   "#48CAE4",
                "wall",   "#4F4F4F",
                "target", "#FFD700"
        );
    }

    private final Magnet game = new Magnet();

    /** Builds the panel and binds the magnet/spawn mouse handlers. */
    MagnetPanel() {
        super();
        init(game);
        viewer.getStyle().set("background-color", "#0D1B2A").set("display", "inline-block");
        viewer.addMouseMotionListener((x, y) -> game.setMagnet(new Vector2D(x, y)));
        viewer.addClickListener((x, y) -> game.spawn(new Vector2D(x, y)));
    }

    /** Shows instructions and binds the current player to the leaderboard. */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        WorldViewer.setShowVelocity(false);
        viewer.showMessage(game.getInstructions());

        Player player = MainView.getPlayer();
        if (player != null) game.bindToLeaderboard(player);

        LeaderboardPanel.setCurrentGameName(game.getName());
    }
}
