package lob.physics.engine;

import lob.physics.Vector2D;
import java.util.Objects;

/**
 * Result of a collision check: tells whether two shapes overlap and, if
 * so, by how much (penetration) and along which direction (surface normal).
 *
 * <p>Per the slides (<i>Colisões</i>), these two values are exactly what
 * the engine needs to <i>repor a posição</i> ({@code p' = p + ρ·n̂}) and
 * to <i>rebater</i> the velocity.
 */
public final class CollisionManifold {

    /** Singleton returned when there is no contact. */
    public static final CollisionManifold NO_COLLISION = new CollisionManifold(false, 0.0, null);

    private final boolean  colliding;
    private final double   penetration;
    private final Vector2D normal;

    /** Canonical constructor: (flag, penetration, normal). */
    public CollisionManifold(boolean colliding, double penetration, Vector2D normal) {
        this.colliding   = colliding;
        this.penetration = penetration;
        this.normal      = normal;
    }

    /** Alternate argument order kept for tests that use (flag, normal, penetration). */
    public CollisionManifold(boolean colliding, Vector2D normal, double penetration) {
        this(colliding, penetration, normal);
    }

    /** @return whether the two shapes are actually overlapping. */
    public boolean  isColliding()  { return colliding; }
    /** @return the penetration depth ρ. */
    public double   penetration()  { return penetration; }
    /** @return the unit normal n̂ at the contact. */
    public Vector2D normal()       { return normal; }
    /** Bean-style alias of {@link #penetration()}. */
    public double   getPenetration(){ return penetration; }
    /** Bean-style alias of {@link #normal()}. */
    public Vector2D getNormal()    { return normal; }

    /**
     * Returns a copy with the normal pointing the other way &mdash; useful
     * to keep symmetry between rect-vs-circle and circle-vs-rect checks.
     */
    public CollisionManifold flipNormal() {
        if (normal == null) return this;
        return new CollisionManifold(colliding, penetration,
                new Vector2D(-normal.x(), -normal.y()));
    }

    /** Convenience accessor for the {@link #NO_COLLISION} singleton. */
    public static CollisionManifold noCollision() { return NO_COLLISION; }

    /** Value-based equality on (colliding, penetration, normal). */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CollisionManifold)) return false;
        CollisionManifold m = (CollisionManifold) o;
        return colliding == m.colliding
                && Double.compare(penetration, m.penetration) == 0
                && Objects.equals(normal, m.normal);
    }

    /** Hash consistent with {@link #equals(Object)}. */
    @Override
    public int hashCode() {
        return Objects.hash(colliding, penetration, normal);
    }
}
