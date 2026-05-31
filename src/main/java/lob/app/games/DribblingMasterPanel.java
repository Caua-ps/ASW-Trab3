package lob.app.games;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.router.Route;
import lob.app.GenericGamePanel;
import lob.app.LeaderboardPanel;
import lob.app.MainView;
import lob.app.WorldViewer;
import lob.gaming.Player;
import lob.gaming.games.DribblingMaster;
import lob.physics.Vector2D;

import java.util.Map;

/**
 * A Vaadin panel for the Dribbling Master game, based on {@link GenericGamePanel}.
 *
 * <p>Mouse clicks apply an impulse to the ball via
 * {@link DribblingMaster#strikeBall(Vector2D)}.
 */
@Route(layout = MainView.class)
public class DribblingMasterPanel extends GenericGamePanel {

    /** Colour map consumed reflectively by {@link GenericGamePanel#init}. */
    public static Map<String,String> getAppearanceColors() {
        return Map.of(
                "basketball", "#FF8C00",
                "floor",      "#6B4F2A",
                "target",     "#CC0000"
        );
    }

    private final DribblingMaster game = new DribblingMaster();

    /** Builds the panel and binds the click-to-strike handler. */
    DribblingMasterPanel() {
        super();
        init(game);
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
