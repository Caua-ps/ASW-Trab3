package lob.app;

import com.vaadin.flow.component.notification.Notification;
import lob.gaming.AppearanceFactory;
import lob.physics.Vector2D;
import lob.physics.shapes.Appearance;
import lob.physics.shapes.Circle;
import lob.physics.shapes.Rectangle;
import lob.physics.shapes.Shape;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A widget to display a succession of frames to produce an animation,
 * where each frame is a collection of shapes - circles or rectangles,
 * colored using appearance.
 *
 * This class is similar to {@code lob.guis.WorldViewer} but adapted to
 * Vaadin and making use {@link Canvas}.
 *
 * @author Jos&eacute; Paulo Leal {@code jpleal@fc.up.pt}
 */
public class WorldViewer extends Canvas {
    // keep a copy to avoid concurrent modification exceptions
    private final ArrayList<Shape> shapes = new ArrayList<>();
    private GraphicContext graphicContext;

    WorldViewer(int width, int height) {
        super(width,height);

        graphicContext = getContext("2d");
    }

    /**
     * Record to bind an appearance name to a color.
     * @param name of appearance
     * @param color for that name
     */
    record ColoredAppearance(String name, String color) implements Appearance {
        @Override
        public String toString() {
            return "Show as "+name;
        }
    }

    /**
     * Creates an appearance factory from a map from names to colors.
     */
    static class ColloredAppearanceFactory implements AppearanceFactory {
        Map<String, String> colors;
        Map<String, ColoredAppearance> appearances = new HashMap<>();

        ColloredAppearanceFactory(Map<String, String> colors) {
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
                String color = colors.getOrDefault(name, "black");
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
     * Displays a message as a notification.
     *
     * It integrates with the graphical user interface and is suitable for
     * conveying alerts or information.
     *
     * @param message the text to be displayed in the dialog box
     */
    public void showMessage(String message) {
        runOnUi(() -> Notification.show(message, 5000, Notification.Position.TOP_CENTER) );
    }

    /**
     * Shows a single frame by displaying the given shapes
     * @param shapes to show
     */
     public synchronized void showFrame(Iterable<Shape> shapes) {
         runOnUi( () -> {
             this.shapes.clear();
             shapes.forEach(this.shapes::add);
             repaint();
         });
    }

    public void repaint() {
         reset();
         paint(graphicContext);
    }

    /**
     * Paints the shapes on the canvas
     */
    public synchronized void paint(GraphicContext graphicContext) {
        if(shapes == null)
            return;

        for(Shape shape: shapes) {
             switch(shape) {
                 case Circle circle -> showCircle(graphicContext, circle);

                 case Rectangle rectangle -> showRectangle(graphicContext, rectangle);
             }
        }
    }

    /**
     * Shows a rectangle on the canvas
     * @param g  the specified Graphics context
     * @param rectangle to show
     */
    private static void showRectangle(GraphicContext g, Rectangle rectangle) {
        Vector2D upperLeft = rectangle.upperLeft();
        Vector2D lowerRight = rectangle.lowerRight();
        int x = (int) upperLeft.x();
        int y = (int) upperLeft.y();
        int width = (int) (lowerRight.x() - upperLeft.x());
        int height = (int) (lowerRight.y() - upperLeft.y());

        g.setFillStyle(getColor(rectangle));
        g.fillRect(x, y, width, height);
    }

    /**
     * Shows a circle on the canvas
     * @param g  the specified Graphics context
     * @param circle to show
     */
    private static void showCircle(GraphicContext g, Circle circle) {
        Vector2D position = circle.position();
        int x = (int) position.x();
        int y = (int) position.y();
        int radius = (int) circle.radius();

        g.setFillStyle(getColor(circle));
        g.fillCircle(x ,y , radius);

        if(showVelocity) {
            Vector2D velocity = circle.velocity();
            int vx = x + (int) velocity.x();
            int vy = y + (int) velocity.y();
            g.setStrokeStyle("red");
            g.drawLine(x, y, vx, vy);
        }
    }

    /**
     * Retrieves the color associated with the specified shape.
     *
     * This method examines the appearance of the given shape and determines its
     * color. If the appearance is an instance of {@code ColoredAppearance} and
     * the color is not null, the specified color is returned. Otherwise, the
     * default color "black" is returned.
     *
     * @param shape the shape whose color is to be determined
     * @return the color of the shape if specified, or "black" if no
     *         color is defined or the appearance is not of type {@code ColoredAppearance}
     */
    private static String getColor(Shape shape) {
        Appearance appearance  = shape.appearance();

        if(appearance instanceof ColoredAppearance colored) {
            if(colored != null)
                return colored.color();
            else
                return "black";
        } else
            return "black";
    }

}
