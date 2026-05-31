package lob.guis;

import lob.gaming.games.DribblingMaster;
import lob.physics.Vector2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

/**
 * Swing launcher for {@link DribblingMaster}. Binds a click handler that
 * applies an impulse to the basketball.
 */
public class DribblingMasterGUI extends GenericGameGUI {

    static {
        DribblingMaster.setAppearanceFactory(new WorldViewer.ColloredAppearanceFactory(
                Map.of(
                        "basketball", new Color(0xFF8C00),
                        "floor",      new Color(0x6B4F2A),
                        "target",     new Color(0xCC0000)
                )));
        WorldViewer.setShowVelocity(false);
    }

    /** Builds the window and binds the click-to-strike mouse handler. */
    DribblingMasterGUI() {
        super();
        DribblingMaster game = new DribblingMaster();
        init(game);
        viewer.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                game.strikeBall(new Vector2D(e.getX(), e.getY()));
            }
        });
    }

    /** Entry point. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(DribblingMasterGUI::new);
    }
}
