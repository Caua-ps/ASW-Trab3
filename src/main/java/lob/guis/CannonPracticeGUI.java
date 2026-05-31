package lob.guis;

import lob.gaming.games.CannonPractice;
import lob.physics.Vector2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

/**
 * Swing launcher for {@link CannonPractice}. Maps shape names to colours
 * and binds a click-to-fire mouse handler.
 */
public class CannonPracticeGUI extends GenericGameGUI {

    static {
        CannonPractice.setAppearanceFactory(new WorldViewer.ColloredAppearanceFactory(
                Map.of(
                        "cannonball", new Color(0x222222),
                        "cannon",     new Color(0x444444),
                        "wall",       new Color(0xCC8E69),
                        "target",     new Color(0xFFA500),
                        "ground",     new Color(0x999999)
                )));
        WorldViewer.setShowVelocity(false);
    }

    /** Builds the window and binds the click-to-fire mouse handler. */
    CannonPracticeGUI() {
        super();
        CannonPractice game = new CannonPractice();
        init(game);
        viewer.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                game.fire(new Vector2D(e.getX(), e.getY()));
            }
        });
    }

    /** Entry point. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(CannonPracticeGUI::new);
    }
}
