package lob.guis;

import lob.gaming.games.ArkanoidKnockoff;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

/**
 * Swing launcher for {@link ArkanoidKnockoff}. Wires the appearance
 * factory (brick / paddle / ball colours), instantiates the game, and
 * binds mouse input: motion moves the paddle, click launches the ball.
 */
public class ArkanoidKnockoffGUI extends GenericGameGUI {

    static {
        ArkanoidKnockoff.setAppearanceFactory(new WorldViewer.ColloredAppearanceFactory(
                Map.of(
                        "wall",         new Color(0x4F4F4F),
                        "paddle",       new Color(0xCCCCCC),
                        "ball",         new Color(0xFFFFFF),
                        "red brick",    new Color(0xE53935),
                        "orange brick", new Color(0xFB8C00),
                        "yellow brick", new Color(0xFDD835),
                        "blue brick",   new Color(0x1E88E5)
                )));
        WorldViewer.setShowVelocity(false);
    }

    /** Builds the window and binds mouse handlers. */
    ArkanoidKnockoffGUI() {
        super();
        ArkanoidKnockoff game = new ArkanoidKnockoff();
        viewer.setBackground(new Color(0x111111));
        init(game);

        viewer.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { game.startGame(); }
        });
        viewer.addMouseMotionListener(new MouseAdapter() {
            public void mouseMoved(MouseEvent e)   { game.movePaddle(e.getX()); }
            public void mouseDragged(MouseEvent e) { game.movePaddle(e.getX()); }
        });
    }

    /** Entry point &mdash; runs the GUI on the Swing event-dispatch thread. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ArkanoidKnockoffGUI::new);
    }
}
