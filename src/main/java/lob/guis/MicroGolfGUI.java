package lob.guis;

import lob.gaming.games.MicroGolf;
import lob.physics.Vector2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

/**
 *  Sets up a green-felt background
 * and binds a click handler that strikes the ball towards the click point.
 */
public class MicroGolfGUI extends GenericGameGUI {

    static {
        MicroGolf.setAppearanceFactory(new WorldViewer.ColloredAppearanceFactory(
                Map.of(
                        "ball", new Color(0xFFFFFF),
                        "wall", new Color(0x2F4F2F),
                        "hole", new Color(0x000000)
                )));
        WorldViewer.setShowVelocity(false);
    }

    /** Builds the window and binds the click-to-strike mouse handler. */
    MicroGolfGUI() {
        super();
        MicroGolf game = new MicroGolf();
        viewer.setBackground(new Color(0x3A8A3A));
        init(game);
        viewer.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                game.strikeBall(new Vector2D(e.getX(), e.getY()));
            }
        });
    }

    /** Entry point. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MicroGolfGUI::new);
    }
}
