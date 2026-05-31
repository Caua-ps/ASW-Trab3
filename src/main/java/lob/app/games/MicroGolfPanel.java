package lob.app.games;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.router.Route;
import lob.app.GenericGamePanel;
import lob.app.LeaderboardPanel;
import lob.app.MainView;
import lob.app.WorldViewer;
import lob.gaming.Player;
import lob.gaming.games.MicroGolf;
import lob.physics.Vector2D;

import java.util.Map;

/**
 * A Vaadin panel for the Micro Golf game, based on {@link GenericGamePanel}.
 *
 * <p>Mouse clicks strike the ball towards the click point via
 * {@link MicroGolf#strikeBall(Vector2D)} (only when the ball is at rest).
 */
@Route(layout = MainView.class)
public class MicroGolfPanel extends GenericGamePanel {

    /** Colour map consumed reflectively by {@link GenericGamePanel#init}. */
    public static Map<String,String> getAppearanceColors() {
        return Map.of(
                "ball",   "#FFFFFF",
                "wall",   "#2F4F2F",
                "target", "#FFD700"
        );
    }

    private final MicroGolf game = new MicroGolf();

    /** Builds the panel and binds the click-to-strike handler. */
    MicroGolfPanel() {
        super();
        init(game);
        viewer.getStyle().set("background-color", "#3A8A3A").set("display", "inline-block");
        viewer.addClickListener((x, y) -> game.strikeBall(new Vector2D(x, y)));
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
