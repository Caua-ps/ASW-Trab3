package lob.guis;

import lob.gaming.AppearanceFactory;
import lob.physics.Vector2D;
import lob.physics.shapes.Appearance;
import lob.physics.shapes.Circle;
import lob.physics.shapes.Rectangle;
import lob.physics.shapes.Shape;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A widget to display a succession of frames to produce an animation,
 * where each frame is a collection of shapes - circles or rectangles,
 * colored using appearance.
 *
 * @author Jos&eacute; Paulo Leal {@code jpleal@fc.up.pt}
 */
public class WorldViewer extends Canvas {
    // keep a copy to avoid concurrent modification exceptions
    private final ArrayList<Shape> shapes = new ArrayList<>();

    /**
     * Record to bind an appearance name to {@link Color}.
     * @param name of appearance
     * @param color for that name
     */
    record ColoredAppearance(String name, Color color) implements Appearance {
        @Override
        public String toString() {
            return "Show as "+name;
        }
    }

    /**
     * Creates an appearance factory from a map from strings to colors.
     */
    static class ColloredAppearanceFactory implements AppearanceFactory {
        Map<String, Color> colors;
        Map<String, ColoredAppearance> appearances = new HashMap<>();

        ColloredAppearanceFactory(Map<String, Color> colors) {
            this.colors = colors;
        }

        /**
         * Gets a colored appearance bound to the given name.
         * If the name is unknown, then the color is black and a warning is
         * reported on the standard error.
         *
         * @param name used to look up or generate the corresponding appearance object.
         * @return appearance mapped to the given name.
         */
        @Override
        public Appearance getAppearance(String name) {
            if (appearances.containsKey(name)) {
                return appearances.get(name);
            } else  {
                Color color = colors.getOrDefault(name, Color.BLACK);
                ColoredAppearance appearance = new ColoredAppearance(name,color);

                if(! colors.containsKey(name)) {
                    System.err.println("name not mapped to a color: "+name);
                }

                appearances.put(name, appearance);
                return appearance;
            }
        }
    }

    private static boolean showVelocity = false;

    /**
     * Sets the visibility of velocity vectors in the world viewer.
     *
     * This method controls whether velocity vectors associated with shapes are
     * displayed on the graphical canvas. If set to {@code true}, velocity
     * vectors will be shown; otherwise, they will be hidden.
     *
     * @param showVelocity a boolean that determines whether velocity visualization
     *                     should be enabled or disabled
     */
    public static void setShowVelocity(boolean showVelocity) {
        WorldViewer.showVelocity = showVelocity;
    }

    /**
     * Determines whether velocity visualization is enabled.
     *
     * This method returns the current state of the `showVelocity` flag, which
     * indicates if velocity vectors of shapes should be displayed in the viewer.
     *
     * @return {@code true} if velocity visualization is enabled, {@code false} otherwise
     */
    public static boolean isShowVelocity() {
        return showVelocity;
    }

    /**
     * Displays a message in a dialog box.
     *
     * This method uses a dialog box to show the provided message to the user.
     * It integrates with the graphical user interface and is suitable for
     * conveying alerts or information.
     *
     * @param message the text to be displayed in the dialog box
     */
    void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    /**
     * Shows a single frame by displaying the given shapes
     * @param shapes to show
     */
     synchronized void showFrame(Iterable<Shape> shapes) {
        this.shapes.clear();
        shapes.forEach(this.shapes::add);
        repaint();
    }

    private static final Toolkit TOOLKIT = Toolkit.getDefaultToolkit();
    /**
     * Paints the shapes on the canvas
     * @param g   the specified Graphics context
     */
     synchronized public void paint(Graphics g) {
        if(shapes == null)
            return;

        for(Shape shape: shapes) {
             switch(shape) {
                 case Circle circle -> showCircle(g, circle);

                 case Rectangle rectangle -> showRectangle(g, rectangle);
             }
        }
        TOOLKIT.sync();
    }

    /**
     * Shows a rectangle on the canvas
     * @param g  the specified Graphics context
     * @param rectangle to show
     */
    private static void showRectangle(Graphics g, Rectangle rectangle) {
        Vector2D upperLeft = rectangle.upperLeft();
        Vector2D lowerRight = rectangle.lowerRight();
        int x = (int) upperLeft.x();
        int y = (int) upperLeft.y();
        int width = (int) (lowerRight.x() - upperLeft.x());
        int height = (int) (lowerRight.y() - upperLeft.y());
        g.setColor(getColor(rectangle));
        g.fillRect(x, y, width, height);
    }

    /**
     * Shows a circle on the canvas
     * @param g  the specified Graphics context
     * @param circle to show
     */
    private static void showCircle(Graphics g, Circle circle) {
        Vector2D position = circle.position();
        int x = (int) position.x();
        int y = (int) position.y();
        int radius = (int) circle.radius();
        g.setColor(getColor(circle));
        g.fillOval(x - radius,y - radius, 2*radius, 2*radius);

        if(showVelocity) {
            Vector2D velocity = circle.velocity();
            int vx = x + (int) velocity.x();
            int vy = y + (int) velocity.y();
            g.setColor(Color.RED);
            g.drawLine(x, y, vx, vy);
        }
    }

    /**
     * Retrieves the color associated with the specified shape.
     *
     * This method examines the appearance of the given shape and determines its
     * color. If the appearance is an instance of {@code ColoredAppearance} and
     * the color is not null, the specified color is returned. Otherwise, the
     * default color {@code Color.BLACK} is returned.
     *
     * @param shape the shape whose color is to be determined
     * @return the color of the shape if specified, or {@code Color.BLACK} if no
     *         color is defined or the appearance is not of type {@code ColoredAppearance}
     */
    private static Color getColor(Shape shape) {
        Appearance appearance  = shape.appearance();

        if(appearance instanceof ColoredAppearance colored) {
            if(colored != null)
                return colored.color();
            else
                return Color.BLACK;
        } else
            return Color.BLACK;
    }

}
