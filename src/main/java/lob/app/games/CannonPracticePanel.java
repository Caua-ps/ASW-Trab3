package lob.app.games;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.router.Route;
import lob.app.GenericGamePanel;
import lob.app.LeaderboardPanel;
import lob.app.MainView;
import lob.app.WorldViewer;
import lob.gaming.Player;
import lob.gaming.games.CannonPractice;
import lob.physics.Vector2D;

import java.util.Map;

/**
 * A Vaadin panel for Cannon Practice game based on {@link GenericGamePanel}
 *
 * @author Jos&eacute; Paulo Leal {@code jpleal@fc.up.pt}
 */
@Route(layout = MainView.class)
public class CannonPracticePanel extends GenericGamePanel {

    private static final String DARK_SLATE_GREY = "#2f4f4f";
    private static final String ORANGE = "#FFA500";
    private static final String BRICK_COLOR = "#CC8E69";


    //    GameAnimation.setAppearanceFactory(new WorldViewer.ColloredAppearanceFactory( ))


    static public Map<String,String> getAppearanceColors() {
        return  Map.of(
                        "cannonball",DARK_SLATE_GREY,
                        "target"    ,ORANGE,
                        "wall"      ,BRICK_COLOR
        );
    }

    CannonPractice cannonPractice = new CannonPractice();

    /**
     * Create a panel by performing standard initialization,
     * and bind mouse events to {@link CannonPractice#fire(Vector2D)}.
     */
    CannonPracticePanel() {
        super();

        init(cannonPractice);

        viewer.addClickListener( (x,y) -> cannonPractice.fire( new Vector2D(x, y)) );
    }

    /**
     * Called when the component is attached to the UI. This method performs
     * various setup actions specific to the Cannon Practice game, such as
     * displaying instructions, binding the current player to the leaderboard,
     * and updating the current game name in the Leaderboard panel.
     *
     * @param attachEvent the event containing details about the component being attached
     */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        WorldViewer.setShowVelocity(true);

        viewer.showMessage( cannonPractice.getInstructions() );

        Player player = MainView.getPlayer();
        if(player != null)
            cannonPractice.bindToLeaderboard(player);

        LeaderboardPanel.setCurrentGameName( cannonPractice.getName() );
    }
}
