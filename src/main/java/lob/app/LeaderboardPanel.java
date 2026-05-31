package lob.app;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import lob.gaming.LeaderboardManager;
import lob.gaming.LeaderboardManager.GameResult;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Leaderboard panel — the "grelha" the assignment describes.
 *
 * <p>It uses a {@link Grid} typed on {@link GameResult} (the record the
 * assignment says to feed the grid) and simply injects the result list from
 * the {@link LeaderboardManager}.
 *
 * <p>The game currently being played is tracked through the {@code static}
 * {@link #setCurrentGameName(String)} that each game panel calls in its
 * {@code onAttach}, so this panel always shows the right game's top scores.
 */
@Route(value = "leaderboard", layout = MainView.class)
public class LeaderboardPanel extends VerticalLayout {

    /** Name of the game whose leaderboard should be shown. */
    private static String currentGameName = null;

    /** How many rows to show. */
    private static final int TOP = 10;

    private final H2 title = new H2();
    private final Grid<GameResult> grid = new Grid<>();

    /** Builds the grid columns (nick, game, points, date). */
    public LeaderboardPanel() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        grid.addColumn(GameResult::nick).setHeader("Jogador").setAutoWidth(true);
        grid.addColumn(GameResult::game).setHeader("Jogo").setAutoWidth(true);
        grid.addColumn(GameResult::points).setHeader("Pontos").setAutoWidth(true);
        grid.addColumn(r -> fmt.format(r.date())).setHeader("Data").setAutoWidth(true);

        setSizeFull();
        add(title, grid);
    }

    /**
     * Selects which game's leaderboard is displayed.
     *
     * @param name the game name (matches {@code GameAnimation.getName()}).
     */
    public static void setCurrentGameName(String name) {
        currentGameName = name;
    }

    /** Reloads the rows for the current game whenever the panel is shown. */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        String game = currentGameName;
        title.setText(game == null ? "Leaderboard" : "Leaderboard — " + game);

        List<GameResult> rows = (game == null)
                ? List.of()
                : LeaderboardManager.getInstance().getLeaderboard(game, TOP);
        grid.setItems(rows);
    }
}
