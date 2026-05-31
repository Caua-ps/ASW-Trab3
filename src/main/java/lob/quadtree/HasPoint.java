package lob.quadtree;

/**
 * Marker interface for any object that can be located by a 2D point.
 *
 * <p>Anything that implements {@code HasPoint} can be inserted into a
 * {@link PointQuadtree}: the tree only ever asks an element <i>"where are you
 * on the plane?"</i>, never about its full type. This is what lets us put
 * positions ({@link lob.physics.Vector2D}), shapes, balls, or any custom
 * record into the same indexing structure.
 */
public interface HasPoint {

    /** @return the x coordinate of this object on the plane. */
    double x();

    /** @return the y coordinate of this object on the plane. */
    double y();
}
