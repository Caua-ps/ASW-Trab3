package lob.physics;

import lob.quadtree.HasPoint;

/**
 * Immutable two-dimensional vector with double-precision components.
 *
 * <p>Implemented as a Java {@code record} so equality, hashing and a textual
 * representation are derived for free from the {@code (x, y)} pair &mdash; this
 * is what the unit tests rely on for set/list comparisons.
 *
 * <p>Implements {@link HasPoint} so a {@code Vector2D} can be inserted directly
 * into a {@link lob.quadtree.PointQuadtree} when it represents a position
 * (instead of a velocity, an acceleration, or a normal).
 *
 * <p><b>Convention:</b> matching the project's slides on the <i>Referencial</i>,
 * the y-axis grows <i>downwards</i>. That is why {@link #NORTH} is
 * {@code (0, -1)} and {@link #SOUTH} is {@code (0, +1)}.
 */
public record Vector2D(double x, double y) implements HasPoint {

    // ---------------------------------------------------------------- direction constants

    /** The zero vector {@code (0, 0)}. Useful as a neutral element and as the
     *  result of normalising a zero-length vector. */
    public static final Vector2D NULL_VECTOR = new Vector2D(0, 0);
    /** Unit vector pointing up the screen (y grows downwards, hence -1). */
    public static final Vector2D NORTH = new Vector2D(0, -1);
    /** Unit vector pointing down the screen. */
    public static final Vector2D SOUTH = new Vector2D(0, 1);
    /** Unit vector pointing right. */
    public static final Vector2D EAST  = new Vector2D(1, 0);
    /** Unit vector pointing left. */
    public static final Vector2D WEST  = new Vector2D(-1, 0);
    /** Diagonal vector pointing up-left (not unit length). */
    public static final Vector2D NORTH_WEST = new Vector2D(-1, -1);
    /** Diagonal vector pointing down-left (not unit length). */
    public static final Vector2D SOUTH_WEST = new Vector2D(-1, 1);
    /** Diagonal vector pointing up-right (not unit length). */
    public static final Vector2D NORTH_EAST = new Vector2D(1, -1);
    /** Diagonal vector pointing down-right (not unit length). */
    public static final Vector2D SOUTH_EAST = new Vector2D(1, 1);

    // ---------------------------------------------------------------- accessors

    /** @return the x component. Bean-style alias of the record component. */
    public double getX() { return x; }
    /** @return the y component. Bean-style alias of the record component. */
    public double getY() { return y; }

    // ---------------------------------------------------------------- magnitude

    /**
     * Euclidean length / norm: {@code sqrt(x² + y²)}.
     *
     * @return the magnitude of this vector.
     */
    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    /**
     * Squared length, avoiding the {@code sqrt}. Use this when comparing
     * magnitudes or when computing distances inside an inner loop.
     *
     * @return {@code x² + y²}.
     */
    public double lengthSquared() {
        return (x * x) + (y * y);
    }

    /** Alias of {@link #length()}, matching the slides' notation
     *  &Vert;p&Vert; = norm(p). */
    public double norm() {
        return length();
    }

    // ---------------------------------------------------------------- arithmetic

    /**
     * Vector sum.
     *
     * @param v the vector to add.
     * @return a new {@code Vector2D} equal to {@code this + v}.
     */
    public Vector2D add(Vector2D v) {
        return new Vector2D(this.x + v.x, this.y + v.y);
    }

    /** Alias of {@link #add(Vector2D)} for callers that prefer infix-style
     *  naming ({@code a.plus(b)}). */
    public Vector2D plus(Vector2D v) {
        return add(v);
    }

    /**
     * Vector difference.
     *
     * @param v the vector to subtract.
     * @return a new {@code Vector2D} equal to {@code this - v}.
     */
    public Vector2D minus(Vector2D v) {
        return new Vector2D(this.x - v.x, this.y - v.y);
    }

    /** Alias of {@link #minus(Vector2D)}. */
    public Vector2D subtract(Vector2D v) {
        return minus(v);
    }

    /**
     * Scalar multiplication: {@code (x, y) → (x·factor, y·factor)}.
     *
     * @param factor the scalar.
     * @return a new {@code Vector2D} scaled by {@code factor}.
     */
    public Vector2D multiply(double factor) {
        return new Vector2D(x * factor, y * factor);
    }

    /** Alias of {@link #multiply(double)}. */
    public Vector2D scale(double scalar) {
        return multiply(scalar);
    }

    /**
     * Returns a unit vector pointing in the same direction as this one.
     *
     * <p>Special case: normalising the {@link #NULL_VECTOR} returns the
     * {@link #NULL_VECTOR} itself (length 0), instead of producing a
     * {@code NaN} from a division by zero. The unit test
     * {@code normalize()} relies on this behaviour.
     *
     * @return a vector of length 1 (or 0 if {@code this} is the zero vector).
     */
    public Vector2D normalize() {
        double len = length();
        if (len == 0) return NULL_VECTOR;
        return new Vector2D(x / len, y / len);
    }

    /**
     * Dot product (also called inner / scalar product):
     * {@code x₁·x₂ + y₁·y₂ = ‖p‖·‖q‖·cos(θ)}.
     *
     * @param v the other vector.
     * @return the scalar dot product.
     */
    public double innerProduct(Vector2D v) {
        return (this.x * v.x) + (this.y * v.y);
    }

    /** Alias of {@link #innerProduct(Vector2D)} matching common
     *  {@code v.dot(w)} naming. */
    public double dot(Vector2D v) {
        return innerProduct(v);
    }
}
