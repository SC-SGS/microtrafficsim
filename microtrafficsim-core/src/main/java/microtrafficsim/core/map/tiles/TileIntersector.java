package microtrafficsim.core.map.tiles;

import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.math.Rect2d;


public interface TileIntersector<T> {
    boolean intersect(T obj, Rect2d rect, Projection projection);
}
