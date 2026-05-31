package lob.app;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import lob.gaming.Player;
import lob.gaming.Players;

import java.util.function.Consumer;

/**
 * Player management dialog — the form for "gerir dados do jogador".
 *
 * <p>Uses a {@link FormLayout} with data-entry fields (the Vaadin form
 * component the assignment points to) and talks to the {@link Players}
 * singleton: <i>Registar</i> creates a new player; <i>Entrar</i> selects an
 * existing one. The chosen player is handed back through the {@code onChosen}
 * callback so {@link MainView} can store it for the session.
 */
public class PlayerDialog extends Dialog {

    /**
     * Builds the dialog.
     *
     * @param onChosen invoked with the registered/selected {@link Player}.
     */
    PlayerDialog(Consumer<Player> onChosen) {
        setHeaderTitle("Gerir jogador");

        TextField nick = new TextField("Nick (sem espaços)");
        TextField name = new TextField("Nome");
        FormLayout form = new FormLayout(nick, name);

        Button register = new Button("Registar", e -> {
            Player p = Players.getInstance()
                    .register(nick.getValue().trim(), name.getValue().trim());
            if (p == null) {
                Notification.show("Nick inválido (vazio/com espaços) ou já em uso.");
                return;
            }
            onChosen.accept(p);
            close();
        });
        register.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button enter = new Button("Entrar", e -> {
            Player p = Players.getInstance().getPlayer(nick.getValue().trim());
            if (p == null) {
                Notification.show("Jogador não encontrado. Regista-te primeiro.");
                return;
            }
            onChosen.accept(p);
            close();
        });

        Button cancel = new Button("Cancelar", e -> close());

        add(form);
        getFooter().add(new HorizontalLayout(cancel, enter, register));
    }
}
