package lob.app.games;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.router.Route;
import lob.app.GenericGamePanel;
import lob.app.LeaderboardPanel;
import lob.app.MainView;
import lob.app.WorldViewer;
import lob.gaming.Player;
import lob.gaming.games.BallStorm;
import lob.physics.Vector2D;

import java.util.Map;

/**
 * A Vaadin panel for the Ball Storm game (project valorization — "lots of
 * balls"), based on {@link GenericGamePanel}.
 *
 * <p>Each click launches a burst of balls at the cursor via
 * {@link BallStorm#burst(Vector2D)}.
 */
@Route(layout = MainView.class)
public class BallStormPanel extends GenericGamePanel {

    /** Colour map consumed reflectively by {@link GenericGamePanel#init}. */
    public static Map<String,String> getAppearanceColors() {
        return Map.of(
                "ball", "#1E88E5",
                "wall", "#4F4F4F"
        );
    }

    private final BallStorm game = new BallStorm();

    /** Builds the panel and binds the click-to-burst handler. */
    BallStormPanel() {
        super();
        init(game);
        viewer.addClickListener((x, y) -> game.burst(new Vector2D(x, y)));
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
