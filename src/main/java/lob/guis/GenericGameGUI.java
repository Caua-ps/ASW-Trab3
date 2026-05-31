package lob.guis;

import lob.gaming.GameAnimation;

import javax.swing.*;
import java.awt.*;

/**
 * Common Swing scaffold reused by every game launcher.
 *
 * <p>Subclasses build the concrete game and invoke {@link #init(GameAnimation)}
 * in their constructor. {@code init} hooks the game's frame-shower to a
 * {@link WorldViewer}, routes message-shower calls to a {@link JOptionPane},
 * resets the game and starts the animation thread.
 */
public class GenericGameGUI extends JFrame {

    /** The canvas onto which frames are painted. */
    WorldViewer viewer = new WorldViewer();

    /** Wires the {@code animation} into this window and starts it. */
    protected void init(GameAnimation animation) {
        setTitle(animation.getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        viewer.setPreferredSize(new Dimension(
                (int) animation.getWidth(), (int) animation.getHeight()));
        add(viewer, BorderLayout.CENTER);

        animation.setMessageShower(msg ->
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, msg, animation.getName(), JOptionPane.INFORMATION_MESSAGE)
            )
        );
        animation.setFrameShower(viewer::showFrame);

        animation.resetGame();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        animation.start();
    }
}
