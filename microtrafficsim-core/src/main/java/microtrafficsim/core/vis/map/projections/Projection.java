package microtrafficsim.core.vis.map.projections;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;


public interface Projection {
    Bounds getMaximumBounds();
    Rect2d getProjectedMaximumBounds();

    Vec2d project(Coordinate c);
    Coordinate unproject(Vec2d v);

    default Rect2d project(Bounds b) {
        Vec2d min = project(b.min());
        Vec2d max = project(b.max());
        return new Rect2d(min, max);
    }

    default Bounds unproject(Rect2d r) {
        Coordinate min = unproject(r.min());
        Coordinate max = unproject(r.max());
        return new Bounds(min, max);
    }
}
