package lob.physics.engine;

import lob.physics.Vector2D;
import lob.physics.events.CollisionEvent;
import lob.physics.events.PhysicsObserver;
import lob.physics.shapes.Circle;
import lob.physics.shapes.Rectangle;
import lob.physics.shapes.Shape;

import java.util.*;

/**
 * Default {@link CollisionManager} for the engine. Implements pairwise
 * detection for circle/circle, circle/rectangle and rectangle/rectangle,
 * resolves contacts using the slides' formulae (<i>Repor a posição</i>,
 * <i>Rebater</i>, <i>Amortecer</i>) and broadcasts {@link CollisionEvent}s
 * to per-shape observers.
 */
public class SimpleCollisionManager implements CollisionManager {

    /** Coefficient of restitution τ ∈ [0, 1]. 1 = elastic bounce. */
    private double restitution = 1.0;

    /** Per-shape collision observers. */
    private final Map<Shape, List<PhysicsObserver<CollisionEvent>>> listeners = new HashMap<>();

    /** Default constructor &mdash; restitution = 1, no listeners. */
    public SimpleCollisionManager() {}

    /** {@inheritDoc} */
    @Override public double getRestitution() { return restitution; }

    /** {@inheritDoc} &mdash; rejects values outside [0, 1]. */
    @Override
    public void setRestitution(double r) {
        if (r < 0 || r > 1)
            throw new IllegalArgumentException("restitution must be in [0,1], got " + r);
        this.restitution = r;
    }

    // ---------------------------------------------------------------- detection

    /**
     * {@inheritDoc} &mdash; dispatches to the right
     * shape-pair-specific overload based on runtime types.
     */
    @Override
    public CollisionManifold checkCollision(Shape s1, Shape s2) {
        if (s1 instanceof Circle && s2 instanceof Circle)
            return checkCollision((Circle) s1, (Circle) s2);
        if (s1 instanceof Circle && s2 instanceof Rectangle)
            return checkCollision((Circle) s1, (Rectangle) s2);
        if (s1 instanceof Rectangle && s2 instanceof Circle)
            return checkCollision((Circle) s2, (Rectangle) s1).flipNormal();
        if (s1 instanceof Rectangle && s2 instanceof Rectangle)
            return checkCollision((Rectangle) s1, (Rectangle) s2);
        return CollisionManifold.noCollision();
    }

    /**
     * Circle&ndash;circle detection. They overlap iff the distance between
     * centres is less than the sum of radii. Normal points from B to A.
     */
    public CollisionManifold checkCollision(Circle a, Circle b) {
        Vector2D delta = b.position().minus(a.position());
        double   dist  = delta.length();
        double   sum   = a.radius() + b.radius();

        if (dist > sum) return CollisionManifold.noCollision();

        Vector2D normal;
        // Threshold guards against FP drift when centres almost coincide.
        if (dist > 1e-8) {
            // Normal on A's side points from B towards A (opposite of delta).
            double nx = -delta.x() / dist;
            double ny = -delta.y() / dist;
            // Snap micro-noise to 0 so axis-aligned normals are exact.
            if (Math.abs(nx) < 1e-10) nx = 0.0;
            if (Math.abs(ny) < 1e-10) ny = 0.0;
            double len = Math.sqrt(nx * nx + ny * ny);
            if (len > 0) { nx /= len; ny /= len; }
            normal = new Vector2D(nx, ny);
        } else {
            // Centres effectively coincide: pick a deterministic normal so
            // that swap(a, b) gives the opposite direction (needed by tests).
            int ha = System.identityHashCode(a);
            int hb = System.identityHashCode(b);
            normal = (ha <= hb) ? Vector2D.WEST : Vector2D.EAST;
        }
        // For the near-overlap case, treat dist as 0 so penetration = sum.
        double penetration = (dist > 1e-8) ? (sum - dist) : sum;
        return new CollisionManifold(true, penetration, normal);
    }

    /** Snap a value within FP noise of 0 to exact 0. */
    private static double snapToZero(double v) {
        return (Math.abs(v) < 1e-10) ? 0.0 : v;
    }

    /**
     * Circle&ndash;rectangle detection: project the circle's centre onto
     * the rectangle, then compare distance to radius.
     */
    public CollisionManifold checkCollision(Circle c, Rectangle r) {
        Vector2D closest = r.closestPoint(c.position());
        Vector2D delta   = closest.minus(c.position());
        double   dist    = delta.length();

        if (dist > c.radius()) return CollisionManifold.noCollision();

        Vector2D normal;
        if (dist > 0) {
            // delta points from circle to rect surface; outward normal is its negation.
            Vector2D d = delta.normalize();
            double nx = (d.x() == 0.0) ? 0.0 : -d.x();
            double ny = (d.y() == 0.0) ? 0.0 : -d.y();
            normal = new Vector2D(nx, ny);
        } else {
            // Circle centre lies inside the rectangle: use rect's outward normal directly.
            normal = r.getNormal(c.position());
        }
        return new CollisionManifold(true, c.radius() - dist, normal);
    }

    /**
     * Rectangle&ndash;rectangle detection (AABB). The minimal-overlap axis
     * decides the contact normal.
     */
    public CollisionManifold checkCollision(Rectangle r1, Rectangle r2) {
        if (r1.xMax() < r2.xMin() || r1.xMin() > r2.xMax() ||
                r1.yMax() < r2.yMin() || r1.yMin() > r2.yMax())
            return CollisionManifold.noCollision();

        double overlapX = Math.min(r1.xMax(), r2.xMax()) - Math.max(r1.xMin(), r2.xMin());
        double overlapY = Math.min(r1.yMax(), r2.yMax()) - Math.max(r1.yMin(), r2.yMin());

        if (overlapX < overlapY) {
            Vector2D n = (r1.x() < r2.x()) ? Vector2D.WEST : Vector2D.EAST;
            return new CollisionManifold(true, overlapX, n);
        }
        Vector2D n = (r1.y() < r2.y()) ? Vector2D.NORTH : Vector2D.SOUTH;
        return new CollisionManifold(true, overlapY, n);
    }

    // ---------------------------------------------------------------- resolution

    /**
     * Reposition + reflect a circle out of another shape, with damping τ.
     * <pre>
     *     p' = p + ρ · n̂
     *     β  = −(1 + τ) · (v · n̂)
     *     v' = v + β · n̂
     * </pre>
     * (slides: <i>Repor a posição</i>, <i>Rebater</i>, <i>Amortecer</i>).
     */
    public Circle resolveCircleCollision(Circle c, Shape other, CollisionManifold manifold) {
        if (!manifold.isColliding()) return c;

        Vector2D v = c.velocity();
        Vector2D n = manifold.normal();
        double   p = manifold.penetration();

        Vector2D newPos = c.position().add(n.multiply(p));
        double   dot    = v.dot(n);
        double   beta   = -(1 + restitution) * dot;
        Vector2D newV   = v.add(n.multiply(beta));

        return new Circle(newPos, newV, c.radius(), c.appearance());
    }

    /**
     * {@inheritDoc} &mdash; the dynamic body in any pair is the
     * {@link Circle}, so we always resolve against whichever argument is
     * the circle, leaving rectangles untouched.
     */
    @Override
    public Shape resolveCollision(Shape colliding, Shape collided, CollisionManifold collision) {
        if (colliding instanceof Circle c)
            return resolveCircleCollision(c, collided, collision);
        if (collided instanceof Circle c)
            return resolveCircleCollision(c, colliding, collision.flipNormal());
        return colliding;  // rectangle ↔ rectangle: nothing to resolve
    }

    // ---------------------------------------------------------------- listeners

    /** {@inheritDoc} */
    @Override
    public void addCollisionListener(Shape shape, PhysicsObserver<CollisionEvent> observer) {
        Objects.requireNonNull(shape,    "shape must not be null");
        Objects.requireNonNull(observer, "observer must not be null");
        listeners.computeIfAbsent(shape, k -> new ArrayList<>()).add(observer);
    }

    /** {@inheritDoc} */
    @Override
    public void removeCollisionListener(Shape shape, PhysicsObserver<CollisionEvent> observer) {
        Objects.requireNonNull(shape,    "shape must not be null");
        Objects.requireNonNull(observer, "observer must not be null");
        List<PhysicsObserver<CollisionEvent>> list = listeners.get(shape);
        if (list != null) list.remove(observer);
    }

    /** {@inheritDoc} */
    @Override
    public void removeAllCollisionListeners(Shape shape) {
        Objects.requireNonNull(shape, "shape must not be null");
        listeners.remove(shape);
    }

    /** {@inheritDoc} &mdash; wraps the arguments in a {@link CollisionEvent}. */
    @Override
    public void notifyCollision(Shape s1, Shape s2, CollisionManifold manifold) {
        notifyCollision(new CollisionEvent(s1, s2, manifold));
    }

    /** Dispatches the event to listeners attached to either shape. */
    public void notifyCollision(CollisionEvent event) {
        notify(event.colliding(), event);
        notify(event.collided(),  event);
    }

    /** Sends {@code event} to every observer registered against {@code shape}. */
    private void notify(Shape shape, CollisionEvent event) {
        List<PhysicsObserver<CollisionEvent>> list = listeners.get(shape);
        if (list == null) return;
        for (PhysicsObserver<CollisionEvent> obs : new ArrayList<>(list))
            obs.notified(event);
    }
}
