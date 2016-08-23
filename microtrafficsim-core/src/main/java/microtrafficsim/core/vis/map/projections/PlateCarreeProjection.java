package microtrafficsim.core.vis.map.projections;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.math.Rect2d;
import microtrafficsim.math.Vec2d;


/**
 * Plate-carree projection.
 *
 * @author Maximilian Luz
 */
public class PlateCarreeProjection implements Projection {

    public static final Bounds MAXIMUM_BOUNDS           = new Bounds(-90, -180, 90, 180);
    public static final Rect2d PROJECTED_MAXIMUM_BOUNDS = new Rect2d(-180, -90, 180, 90);

    @Override
    public Bounds getMaximumBounds() {
        return new Bounds(MAXIMUM_BOUNDS);
    }

    @Override
    public Rect2d getProjectedMaximumBounds() {
        return new Rect2d(PROJECTED_MAXIMUM_BOUNDS);
    }

    @Override
    public Vec2d project(Coordinate c) {
        return new Vec2d(c.lon, c.lat);
    }

    @Override
    public Coordinate unproject(Vec2d v) {
        return new Coordinate(v.y, v.x);
    }


    @Override
    public boolean equals(Object obj) {
        return (obj instanceof PlateCarreeProjection);
    }

    @Override
    public int hashCode() {
        return PlateCarreeProjection.class.hashCode();
    }
}
