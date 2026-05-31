package lob.app;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Simple bind to the HTML 5 canvas to support the implementation of
 * web interfaces of LotsOfBalls games within the Vaadin framework.
 * It does not aim to be a full add-on (which already exists).
 *
 * Instances of this class accept event listeners for simple mouse events
 * through the {@link ::addClickListener()} and {@link ::addMouseMotionListener()}
 * methods. The arguments are functions that receive a pair of coordinates,
 * with origen at the canvas's upper left corner.
 *
 * @author Jos&eacute; Paulo Leal {@code jpleal@fc.up.pt}
 */
public class Canvas extends Div {
    private final UI ui;
    private Page page;
    private Element element;

    class GraphicContext {

        /**
         * Set the width of subsequently drawn lines
         * @param width of line
         */
        void setLineWidth(int width) {
            executeJsOnUi("$0.graphicContext.setLineWidth = $1;",element,width);
        }

        /**
         * Set the style of strokes of paths and shapes.
         * @param style of strokes
         */
        void setStrokeStyle(String style) {
            executeJsOnUi("$0.graphicContext.strokeStyle = $1;",element,style);
        }

        /**
         * Set the style for filling paths and shapes.
         * @param style to fill paths and shapes.
         */
        void setFillStyle(String style) {
            executeJsOnUi("$0.graphicContext.fillStyle = $1;",element,style);
        }

        /**
         * Fill a rectangle with the given upper left corner and dimensions,
         * using the current style.
         * @param x coordinate of the rectangle's upper left corner
         * @param y coordinate of the rectangle's upper left corner.
         * @param width of the rectangle.
         * @param height of the rectangle.
         */
        void fillRect(int x, int y, int width, int height) {
            executeJsOnUi("$0.graphicContext.fillRect($1,$2,$3,$4);",element,x,y,width,height);
        }

        /**
         * Fill a circle with the given center coordinates and radius.
         * @param x coordinate of the circle's center.
         * @param y coordinate of the circle's center.
         * @param radius if the circle.
         */
        void fillCircle(int x, int y, int radius) {
            executeJsOnUi("""
                $0.graphicContext.beginPath();
                $0.graphicContext.arc($1,$2,$3,0,2*Math.PI,true);
                $0.graphicContext.fill();
            """,element,x,y,radius);
        }

        /**
         * Draw a line between the points with given coordinates,
         * using the current style and line width.
         * @param x1 x coordinate of the first point.
         * @param y1 y coordinate of the first point.
         * @param x2 x coordinate of the second point.
         * @param y2 y coordinate of the second point.
         */
        void drawLine(int x1, int y1, int x2, int y2) {
            executeJsOnUi("""
                $0.graphicContext.beginPath();
                $0.graphicContext.moveTo($1,$2);
                $0.graphicContext.lineTo($3,$4);
                $0.graphicContext.stroke();
            """,element,x1,y1,x2,y2);
        }


    }

    /**
     * Creates a new Canvas instance with the specified dimensions.
     * Initializes the HTML canvas element and registers mouse event listeners for click and movement.
     * The JavaScript logic sets up the canvas element, attaches it to the component, and establishes
     * mechanisms for capturing user interactions such as mouse clicks and movements.
     *
     * @param width  the width of the canvas in pixels
     * @param height the height of the canvas in pixels
     * @throws IllegalStateException if the Canvas is created when a Vaadin UI is not available
     */
    public Canvas(int width, int height) {
        ui = UI.getCurrent();
        if (ui == null) {
            throw new IllegalStateException("Canvas must be created while a Vaadin UI is available");
        }
        element = getElement();
        page = ui.getPage();
        page.executeJs("""
                {
                    const canvas = document.createElement("canvas");
                    
                    canvas.width = $1;
                    canvas.height = $2;
                    
                    $0.appendChild(canvas);
                    
                    $0.canvas = canvas;
                    
                    window.relativeCoords = function(event) {
                        const bounds = event.target.getBoundingClientRect();
                        const x = event.clientX - bounds.left;
                        const y = event.clientY - bounds.top;
                        return {x, y};
                    }
                    
                    $0.addEventListener('click', e => { const p = relativeCoords(e); $0.$server.clicked(p.x,p.y) } );
                    $0.addEventListener('mousemove', e => { const p = relativeCoords(e); $0.$server.mouseMoved(p.x,p.y); } );
                }
                """,element,width,height);
    }

    private List<BiConsumer<Integer,Integer>> clickListeners = new ArrayList<>();
    private List<BiConsumer<Integer,Integer>> mouseMotionListeners = new ArrayList<>();


    /**
     * Registers a click listener to the canvas. The listener will be notified
     * whenever the canvas is clicked, providing the coordinates of the click.
     *
     * @param listener a BiConsumer that accepts two integer parameters representing
     *                 the x and y coordinates of the click location, respectively.
     */
    public void addClickListener(BiConsumer<Integer,Integer> listener) {
        clickListeners.add(listener);
    }

    /**
     * Registers a mouse motion listener to the canvas.
     * The listener will be notified whenever the mouse is moved over the canvas,
     * providing the current coordinates of the mouse pointer.
     *
     * @param listener a BiConsumer that accepts two integer parameters representing
     *                 the x and y coordinates of the mouse pointer, respectively.
     */
    public void addMouseMotionListener(BiConsumer<Integer,Integer> listener) {
        mouseMotionListeners.add(listener);
    }

    /**
     * Invoked when the canvas is clicked at the specified coordinates.
     * This method notifies all registered click listeners by passing the click coordinates.
     *
     * @param x the x-coordinate of the click location.
     * @param y the y-coordinate of the click location.
     */
    @ClientCallable
    private void clicked(int x, int y) {
        clickListeners.forEach(l -> l.accept(x,y));
    }

    /**
     * Handles the event when the mouse is moved over the canvas.
     * This method notifies all registered mouse motion listeners
     * by providing the current coordinates of the mouse pointer.
     *
     * @param x the x-coordinate of the mouse pointer's current position.
     * @param y the y-coordinate of the mouse pointer's current position.
     */
    @ClientCallable
    private void mouseMoved(int x, int y) {
        mouseMotionListeners.forEach(l -> l.accept(x,y));
    }

    /**
     * Get a graphic context for this canvas, used as for drawing graphic primitives.
     *
     * @param type of GraphicContext (only '2d' is actually supported)
     * @return graphic context
     */
    GraphicContext getContext(String type) {
        switch (type) {
            case "2d":
                page.executeJs(" { $0.graphicContext = $0.canvas.getContext('2d'); } ",element);
                return new GraphicContext();
            default:
                throw new IllegalArgumentException("Unsupported canvas type");
        }
    }

    /**
     * Resets the canvas to its initial state by clearing its content.
     * This is achieved by invoking a JavaScript command that effectively
     * resets the canvas width, which forces the canvas to clear its drawings.
     */
    public void reset() {
        executeJsOnUi(" { $0.canvas.width = $0.canvas.width; } ",element);
    }

    /**
     * Ensure the given action is executed on the user interface (UI).
     *
     * @param action to execute on the UI.
     */
    protected void runOnUi(Runnable action) {
        if (ui.getSession() == null) {
            throw new IllegalStateException("Vaadin session is not available");
        }
        if (ui.getSession().hasLock()) {
            action.run();
        } else {
            ui.access(action::run);
        }
    }

    /**
     * Execute the given JavaScript with its args on the user interface (UI)
     *
     * @param script to execute
     * @param args to embed on the script
     */
    private void executeJsOnUi(String script,Object... args) {
        runOnUi( () -> page.executeJs(script,args) );
    }

}
