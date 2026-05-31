package lob.app;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;
import lob.app.games.ArkanoidKnockoffPanel;
import lob.app.games.BallStormPanel;
import lob.app.games.CannonPracticePanel;
import lob.app.games.DribblingMasterPanel;
import lob.app.games.MicroGolfPanel;
import lob.gaming.Player;

/**
 * Main application shell — the "classic application layout" suggested in the
 * assignment ({@link AppLayout}: a top navbar plus a side drawer).
 *
 * <p>Every game panel and the leaderboard declare {@code @Route(layout =
 * MainView.class)}, so they render inside this shell while the navbar/drawer
 * stay put — this is the navigation between panels the assignment asks for.
 *
 * <p>The current {@link Player} is held in the {@link VaadinSession} (one per
 * browser session) but exposed through the {@code static} {@link #getPlayer()}
 * the game panels call in their {@code onAttach}. Player management is offered
 * through a {@link PlayerDialog} launched from a navbar button (a dialog
 * launched by a button, as the assignment suggests).
 */
public class MainView extends AppLayout {

    /** Shows the current player in the navbar. */
    private final Span playerLabel = new Span();

    /** Builds the navbar (title + player controls) and the navigation drawer. */
    public MainView() {
        H1 title = new H1("Lots of Balls");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0");

        Button playerButton = new Button("Jogador", e -> openPlayerDialog());

        addToNavbar(new DrawerToggle(), title, playerButton, playerLabel);

        VerticalLayout menu = new VerticalLayout(
                new RouterLink("Início",           WelcomePanel.class),
                new RouterLink("Cannon Practice",  CannonPracticePanel.class),
                new RouterLink("Dribbling Master", DribblingMasterPanel.class),
                new RouterLink("Micro Golf",       MicroGolfPanel.class),
                new RouterLink("Arkanoid",         ArkanoidKnockoffPanel.class),
                new RouterLink("Ball Storm",       BallStormPanel.class),
                new RouterLink("Leaderboard",      LeaderboardPanel.class)
        );
        addToDrawer(menu);

        refreshPlayerLabel();
    }

    // ---------------------------------------------------------------- player dialog

    /** Opens the player management dialog and refreshes the navbar on success. */
    private void openPlayerDialog() {
        new PlayerDialog(player -> {
            setPlayer(player);
            refreshPlayerLabel();
        }).open();
    }

    /** Updates the navbar label with the current player's name. */
    private void refreshPlayerLabel() {
        Player p = getPlayer();
        playerLabel.setText(p == null ? "(sem jogador)" : "Jogador: " + p.getName());
    }

    // ---------------------------------------------------------------- current player

    /**
     * @return the player chosen for this browser session, or {@code null} if
     *         none has been set yet.
     */
    public static Player getPlayer() {
        VaadinSession session = VaadinSession.getCurrent();
        return session == null ? null : session.getAttribute(Player.class);
    }

    /**
     * Sets the player for this browser session.
     *
     * @param player the player to remember (may be {@code null} to clear).
     */
    public static void setPlayer(Player player) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) session.setAttribute(Player.class, player);
    }
}
