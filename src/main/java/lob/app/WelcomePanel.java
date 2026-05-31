package lob.app;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import lob.gaming.GameAnimation;
import lob.gaming.ReflectGameFactory;

/**
 * Landing panel shown at the application root.
 *
 * <p>Besides welcoming the player, it lists the games <b>discovered by
 * reflection</b> through {@link ReflectGameFactory}. This makes the
 * valorization visible: dropping a new {@code GameAnimation} subclass into
 * {@code lob.gaming.games} makes it appear here without changing this code.
 */
@Route(value = "", layout = MainView.class)
public class WelcomePanel extends VerticalLayout {

    /** Builds the static welcome content. */
    public WelcomePanel() {
        add(new H2("Lots of Balls — interface web"));
        add(new Paragraph(
                "Regista-te no botão \"Jogador\", escolhe um jogo no menu lateral, "
                        + "joga, e consulta a tabela classificativa em \"Leaderboard\"."));
    }

    /** Lists the reflection-discovered games each time the panel is shown. */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        add(new H2("Jogos disponíveis (descobertos por reflexão)"));
        UnorderedList list = new UnorderedList();
        try {
            ReflectGameFactory<GameAnimation> factory = new ReflectGameFactory<>();
            for (String name : factory.getAvailableGameNames()) {
                list.add(new ListItem(name));
            }
        } catch (Exception e) {
            list.add(new ListItem("(não foi possível enumerar os jogos: " + e.getMessage() + ")"));
        }
        add(list);
    }
}
