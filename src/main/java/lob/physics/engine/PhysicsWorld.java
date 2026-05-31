package lob.physics.engine;

import lob.physics.Vector2D;
import lob.physics.actions.ActionOnShapes;
import lob.physics.actions.PendingActions;
import lob.physics.events.*;
import lob.physics.forces.ForceStrategy;
import lob.physics.forces.NoForceStrategy;
import lob.physics.shapes.Circle;
import lob.physics.shapes.Rectangle;
import lob.physics.shapes.Shape;
import lob.quadtree.PointQuadtree;

import java.util.*;
import java.util.function.Predicate;

/**
 * The 2D physics engine described in the slides (<i>Motor de física</i>).
 * Holds every {@link Shape} of the simulation in a {@link PointQuadtree},
 * integrates motion under a pluggable {@link ForceStrategy}, detects and
 * resolves collisions through a {@link CollisionManager}, and broadcasts
 * {@link CollisionEvent}s and {@link EscapeEvent}s using the
 * {@link PhysicsSubject} machinery.
 *
 * <p>Mutations during {@link #update(double)} are queued via
 * {@link PendingActions} (the <b>Command</b> pattern) so listener handlers
 * can safely add or remove shapes without invalidating iterators.
 *
 * <p>Implements {@link Iterable} so callers can iterate the world directly,
 * and so it can be passed to {@link lob.physics.shapes.FrameShower}.
 */
public class PhysicsWorld extends PhysicsSubject<PhysicsEvent> implements Iterable<Shape> {

    /** Static configurable margin used by some games for hit-tests. */
    private static double margin = 100.0;
    /** @return the current static margin. */
    public  static double getMargin() { return margin; }
    /** Sets the static margin. Rejects negative values. */
    public  static void setMargin(double m) {
        if (m < 0) throw new IllegalArgumentException("margin must be >= 0, got " + m);
        margin = m;
    }
    /** {@code int} overload of {@link #setMargin(double)}. */
    public  static void setMargin(int m) { setMargin((double) m); }

    /** Spatial index of every shape. */
    private PointQuadtree<Shape> shapes;
    /** Visible world dimensions (used for escape detection and resets). */
    private final double width;
    private final double height;
    /** Collision strategy &mdash; replaceable for tests. */
    private CollisionManager collisionManager = new SimpleCollisionManager();
    /** Force strategy &mdash; defaults to no force. */
    private ForceStrategy    forceStrategy    = new NoForceStrategy();

    /** Per-boundary escape listeners. */
    private final Map<Shape, List<PhysicsObserver<EscapeEvent>>> escapeListeners = new HashMap<>();

    /** Deferred-mutation queue applied at the end of every update. */
    private final PendingActions pendingActions = new PendingActions();
    /** Re-entrancy guard: {@code true} while inside {@link #update}. */
    private boolean updating = false;

    /**
     * Builds an empty world with the given visible size.
     * The underlying quadtree is sized far larger than the visible area so
     * shapes can travel "off-screen" without throwing out-of-bounds.
     */
    public PhysicsWorld(double width, double height) {
        this.width  = width;
        this.height = height;
        double margin = Math.max(width, height) * 1000;
        this.shapes = new PointQuadtree<>(-margin, -margin, width + margin, height + margin);
    }

    // ---------------------------------------------------------------- shapes

    /** Adds a shape, deferring the insert if currently updating. */
    public void addShape(Shape shape) {
        Objects.requireNonNull(shape, "shape must not be null");
        if (updating) {
            pendingActions.defer(() -> shapes.insert(shape));
        } else {
            shapes.insert(shape);
        }
    }

    /** Removes a shape and all listeners attached to it. Defers if updating. */
    public void removeShape(Shape shape) {
        Objects.requireNonNull(shape, "shape must not be null");
        if (updating) {
            pendingActions.defer(() -> {
                shapes.delete(shape);
                collisionManager.removeAllCollisionListeners(shape);
                escapeListeners.remove(shape);
            });
        } else {
            shapes.delete(shape);
            collisionManager.removeAllCollisionListeners(shape);
            escapeListeners.remove(shape);
        }
    }

    /** Convenience alias of {@link #addShape(Shape)}. */
    public void add(Shape shape)    { addShape(shape); }
    /** Convenience alias of {@link #removeShape(Shape)}. */
    public void remove(Shape shape) { removeShape(shape); }

    /** Bypasses the deferred check &mdash; used by {@link ActionOnShapes}
     *  commands after {@link #update} completes. */
    public void addShapeNow(Shape shape)    { shapes.insert(shape); }
    /** See {@link #addShapeNow}. */
    public void removeShapeNow(Shape shape) { shapes.delete(shape); }

    /** @return the underlying quadtree (mostly for tests). */
    public PointQuadtree<Shape> getShapes() { return shapes; }

    /** @return the current shape count. */
    public int countShapes() {
        int count = 0;
        for (Shape s : shapes) count++;
        return count;
    }

    /** Returns the first shape matching the predicate, or {@code null}. */
    public Shape findShape(Predicate<Shape> filter) {
        for (Shape s : shapes)
            if (filter.test(s)) return s;
        return null;
    }

    /** Returns every shape matching the predicate. */
    public List<Shape> findShapes(Predicate<Shape> filter) {
        List<Shape> result = new ArrayList<>();
        for (Shape s : shapes) if (filter.test(s)) result.add(s);
        return result;
    }

    /** {@inheritDoc} */
    @Override public Iterator<Shape> iterator() { return shapes.iterator(); }

    // ---------------------------------------------------------------- physics loop

    /**
     * Advances the simulation by {@code dt} seconds. Three phases:
     * <ol>
     *   <li>integrate motion of every {@link Circle} under the
     *       {@link ForceStrategy};</li>
     *   <li>detect &amp; resolve pairwise collisions, fire collision
     *       events;</li>
     *   <li>fire escape events for circles that left their boundary.</li>
     * </ol>
     * Mutations queued during steps 1&ndash;3 are applied at the end.
     */
    public void update(double dt) {
        updating = true;

        // 1. Move all circles per the force strategy.
        List<Shape> snapshot = new ArrayList<>();
        for (Shape s : shapes) snapshot.add(s);

        for (Shape s : snapshot) {
            if (s instanceof Circle) {
                Circle c = (Circle) s;
                Vector2D acc = forceStrategy.getAcceleration(c);
                Circle moved = c.move(acc, dt);
                shapes.delete(c);
                shapes.insert(moved);
            }
        }

        // 2. Detect / resolve collisions, then fire collision events.
        List<Shape> afterMove = new ArrayList<>();
        for (Shape s : shapes) afterMove.add(s);

        for (int i = 0; i < afterMove.size(); i++) {
            for (int j = i + 1; j < afterMove.size(); j++) {
                Shape s1 = afterMove.get(i);
                Shape s2 = afterMove.get(j);
                CollisionManifold manifold = collisionManager.checkCollision(s1, s2);
                if (manifold != null && manifold.isColliding()) {
                    // The dynamic body is whichever shape is the Circle:
                    // we replace it with the resolved version.
                    Shape resolved = collisionManager.resolveCollision(s1, s2, manifold);
                    if (resolved != null && resolved instanceof Circle) {
                        Shape original = (s1 instanceof Circle) ? s1 : s2;
                        if (resolved != original) {
                            shapes.delete(original);
                            shapes.insert(resolved);
                            int idx = (s1 instanceof Circle) ? i : j;
                            afterMove.set(idx, resolved);
                        }
                    }
                    collisionManager.notifyCollision(s1, s2, manifold);
                }
            }
        }

        // 3. Detect escapes (circles that exited their boundary rectangle).
        List<Shape> afterCollision = new ArrayList<>();
        for (Shape s : shapes) afterCollision.add(s);

        // Snapshot the entry set to avoid CME during notification.
        List<Map.Entry<Shape, List<PhysicsObserver<EscapeEvent>>>> escapeEntries =
                new ArrayList<>(escapeListeners.entrySet());

        for (Map.Entry<Shape, List<PhysicsObserver<EscapeEvent>>> e : escapeEntries) {
            Shape boundary = e.getKey();
            List<PhysicsObserver<EscapeEvent>> obs = new ArrayList<>(e.getValue());
            for (Shape s : afterCollision) {
                if (s instanceof Circle) {
                    Circle c = (Circle) s;
                    if (!isInsideBoundary(c, boundary)) {
                        EscapeEvent ev = new EscapeEvent(boundary, c);
                        for (PhysicsObserver<EscapeEvent> o : obs) o.notified(ev);
                    }
                }
            }
        }

        updating = false;
        pendingActions.executePendingActions();
    }

    /** {@code int} overload of {@link #update(double)}. */
    public void update(int dt) { update((double) dt); }

    /** Whether {@code c}'s centre still lies inside {@code boundary}. */
    private boolean isInsideBoundary(Circle c, Shape boundary) {
        if (boundary instanceof Rectangle) {
            Rectangle r = (Rectangle) boundary;
            return c.position().x() >= r.xMin() && c.position().x() <= r.xMax()
                && c.position().y() >= r.yMin() && c.position().y() <= r.yMax();
        }
        return true;
    }

    // ---------------------------------------------------------------- reset

    /** Clears every shape; deferred if currently inside {@link #update}. */
    public void reset() {
        if (updating) {
            pendingActions.defer(this::resetNow);
        } else {
            resetNow();
        }
    }

    /** Rebuilds the underlying quadtree (the actual reset). */
    private void resetNow() {
        double margin = Math.max(width, height) * 1000;
        shapes = new PointQuadtree<>(-margin, -margin, width + margin, height + margin);
    }

    /** Alias of {@link #reset()} used by {@link lob.physics.actions.ResetAction}. */
    public void clear() { resetNow(); }

    // ---------------------------------------------------------------- force strategy

    /** @return the current force strategy. */
    public ForceStrategy getForceStrategy()        { return forceStrategy; }
    /** Replaces the force strategy. Rejects {@code null}. */
    public void setForceStrategy(ForceStrategy s)  {
        Objects.requireNonNull(s, "force strategy must not be null");
        this.forceStrategy = s;
    }

    // ---------------------------------------------------------------- restitution

    /** @return the bounce restitution coefficient τ. */
    public double getRestitution()       { return collisionManager.getRestitution(); }
    /** Sets the bounce restitution coefficient τ. */
    public void   setRestitution(double r){ collisionManager.setRestitution(r); }

    // ---------------------------------------------------------------- collision manager

    /** @return the current collision manager. */
    public CollisionManager getCollisionManager() { return collisionManager; }
    /** Replaces the collision manager. Rejects {@code null}. */
    public void setCollisionManager(CollisionManager m) {
        Objects.requireNonNull(m, "collision manager must not be null");
        this.collisionManager = m;
    }

    // ---------------------------------------------------------------- collision listeners

    /** Registers an observer for collisions involving {@code shape}. */
    public void addCollisionListener(Shape shape, PhysicsObserver<CollisionEvent> observer) {
        Objects.requireNonNull(shape,    "shape must not be null");
        Objects.requireNonNull(observer, "observer must not be null");
        collisionManager.addCollisionListener(shape, observer);
    }

    /** Unregisters a previously registered observer. */
    public void removeCollisionListener(Shape shape, PhysicsObserver<CollisionEvent> observer) {
        Objects.requireNonNull(shape,    "shape must not be null");
        Objects.requireNonNull(observer, "observer must not be null");
        collisionManager.removeCollisionListener(shape, observer);
    }

    /** Removes every collision listener registered against {@code shape}. */
    public void removeAllCollisionListeners(Shape shape) {
        Objects.requireNonNull(shape, "shape must not be null");
        collisionManager.removeAllCollisionListeners(shape);
    }

    // ---------------------------------------------------------------- escape listeners

    /** Registers an observer fired when a circle exits {@code boundary}. */
    public void addEscapeListener(Shape boundary, PhysicsObserver<EscapeEvent> observer) {
        Objects.requireNonNull(boundary, "boundary must not be null");
        Objects.requireNonNull(observer, "observer must not be null");
        escapeListeners.computeIfAbsent(boundary, k -> new ArrayList<>()).add(observer);
    }

    /** Unregisters a previously registered escape observer. */
    public void removeEscapeListener(Shape boundary, PhysicsObserver<EscapeEvent> observer) {
        Objects.requireNonNull(boundary, "boundary must not be null");
        Objects.requireNonNull(observer, "observer must not be null");
        List<PhysicsObserver<EscapeEvent>> list = escapeListeners.get(boundary);
        if (list != null) list.remove(observer);
    }

    /** Removes every escape listener registered against {@code boundary}. */
    public void removeAllEscapeListeners(Shape boundary) {
        Objects.requireNonNull(boundary, "boundary must not be null");
        escapeListeners.remove(boundary);
    }

    /** @return the visible world width. */
    public double getWidth()  { return width; }
    /** @return the visible world height. */
    public double getHeight() { return height; }
}
