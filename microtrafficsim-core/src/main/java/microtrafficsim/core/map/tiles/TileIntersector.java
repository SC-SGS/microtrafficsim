package microtrafficsim.core.map.tiles;

import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.math.Rect2d;


/**
 * Intersection testing for tiles.
 *
 * @param <T> the type for which the {@code TileIntersector} is.
 * @author Maximilian Luz
 */
public interface TileIntersector<T> {

    /**
     * Tests the given object projected using the given projection for intersection against the given tile.
     *
     * @param obj        the object to test for intersection.
     * @param rect       the tile to test the object against.
     * @param projection the projection used to project the given object.
     * @return {@code true} if the projected object intersects with the given tile.
     */
    boolean intersect(T obj, Rect2d rect, Projection projection);
}
