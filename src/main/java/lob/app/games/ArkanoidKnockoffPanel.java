package lob.app.games;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.router.Route;
import lob.app.GenericGamePanel;
import lob.app.LeaderboardPanel;
import lob.app.MainView;
import lob.app.WorldViewer;
import lob.gaming.Player;
import lob.gaming.games.ArkanoidKnockoff;

import java.util.Map;

/**
 * A Vaadin panel for the Arkanoid Knockoff game, based on {@link GenericGamePanel}.
 *
 * <p>Mouse motion moves the paddle via {@link ArkanoidKnockoff#movePaddle(int)};
 * a click launches the ball (or restarts) via {@link ArkanoidKnockoff#startGame()}.
 */
@Route(layout = MainView.class)
public class ArkanoidKnockoffPanel extends GenericGamePanel {

    /** Colour map consumed reflectively by {@link GenericGamePanel#init}. */
    public static Map<String,String> getAppearanceColors() {
        return Map.of(
                "wall",         "#4F4F4F",
                "paddle",       "#CCCCCC",
                "ball",         "#FFFFFF",
                "red brick",    "#E53935",
                "orange brick", "#FB8C00",
                "yellow brick", "#FDD835",
                "blue brick",   "#1E88E5"
        );
    }

    private final ArkanoidKnockoff game = new ArkanoidKnockoff();

    /** Builds the panel and binds the paddle/launch mouse handlers. */
    ArkanoidKnockoffPanel() {
        super();
        init(game);
        viewer.getStyle().set("background-color", "#111111").set("display", "inline-block");
        viewer.addMouseMotionListener((x, y) -> game.movePaddle(x));
        viewer.addClickListener((x, y) -> game.startGame());
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
