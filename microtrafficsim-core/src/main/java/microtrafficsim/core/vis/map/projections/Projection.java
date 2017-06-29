package microtrafficsim.core.vis.map.projections;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;


/**
 * Projection to project spherical coordinates.
 *
 * @author Maximilian Luz
 */
public interface Projection {

    /**
     * Returns the maximum (un-projected) bounds accepted by this projection.
     *
     * @return the maximum (un-projected) bounds accepted by this projection.
     */
    Bounds getMaximumBounds();

    /**
     * Returns the maximum (projected) bounds accepted by this projection.
     *
     * @return the maximum (projected) bounds accepted by this projection.
     */
    Rect2d getProjectedMaximumBounds();

    /**
     * Project the given coordinate.
     *
     * @param c the coordinate to project.
     * @return the projected coordinate as vector.
     */
    Vec2d project(Coordinate c);

    /**
     * Un-projects the given vector.
     *
     * @param v the vector to un-project.
     * @return the un-projected vector as coordinate.
     */
    Coordinate unproject(Vec2d v);

    /**
     * Project the array of coordinates using this projection.
     *
     * @param coords the coordinates to project.
     * @return the projected coordinates.
     */
    default Vec2d[] project(Coordinate[] coords) {
        Vec2d[] projected = new Vec2d[coords.length];
        for (int i = 0; i < coords.length; i++) {
            projected[i] = project(coords[i]);
        }
        return projected;
    }

    /**
     * Un-project the array of coordinates using this projection.
     *
     * @param coords the coordinates to un-project.
     * @return the un-projected coordinates.
     */
    default Coordinate[] project(Vec2d[] coords) {
        Coordinate[] projected = new Coordinate[coords.length];
        for (int i = 0; i < coords.length; i++) {
            projected[i] = unproject(coords[i]);
        }
        return projected;
    }

    /**
     * Projected the given bounds.
     *
     * @param b the bounds to project.
     * @return the projected coordinate-bounds as rectangle.
     */
    default Rect2d project(Bounds b) {
        Vec2d min = project(b.min());
        Vec2d max = project(b.max());
        return new Rect2d(min, max);
    }

    /**
     * Un-project the given rectangle.
     *
     * @param r the rectangle to un-project
     * @return the un-projected rectangle as coordinate-bounds
     */
    default Bounds unproject(Rect2d r) {
        Coordinate min = unproject(r.min());
        Coordinate max = unproject(r.max());
        return new Bounds(min, max);
    }
}
