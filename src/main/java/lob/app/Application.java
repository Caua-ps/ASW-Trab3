package lob.app;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import lob.gaming.GameAnimation;
import lob.gaming.LeaderboardManager;
import lob.gaming.Players;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 *
 * <p>Carries {@link Push @Push} (required by {@link WorldViewer}: the viewer
 * pushes simulation frames from the server thread to the browser, so server
 * push must be enabled on the application shell — this is the class with
 * {@code main()}, as the assignment requires).
 *
 * <p>{@code main()} also points the {@link Players} and {@link LeaderboardManager}
 * singletons at backing files so registrations and scores survive a restart.
 *
 * <p>This class lives in {@code lob.app} so Spring component scanning and
 * Vaadin route scanning cover {@code lob.app} and {@code lob.app.games}.
 */
@Push
@SpringBootApplication
public class Application implements AppShellConfigurator {

    /**
     * Boots the Spring/Vaadin application after configuring persistence.
     *
     * @param args standard program arguments (forwarded to Spring).
     */
    public static void main(String[] args) {
        // Persist the player roster and the leaderboard between runs.
        Players.setPlayersFile("players.dat");
        LeaderboardManager.setLeaderboardFile("leaderboard.dat");

        // A smooth animation frame rate for the web viewer.
        GameAnimation.setFPS(60);

        SpringApplication.run(Application.class, args);
    }
}
