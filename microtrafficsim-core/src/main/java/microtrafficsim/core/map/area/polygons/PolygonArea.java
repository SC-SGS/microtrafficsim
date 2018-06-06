package microtrafficsim.core.map.area.polygons;

import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.area.Area;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.math.Vec2d;
import microtrafficsim.math.geometry.polygons.Polygon;


/**
 * Simple area area, defined by a list of {@link Coordinate Coordinate}s.
 *
 * @author Maximilian Luz
 */
public interface PolygonArea extends Area {

    /**
     * Return the (ordered) list of coordinates that define this area.
     *
     * @return the array of coordinate defining this area.
     */
    Coordinate[] getCoordinates();

    /**
     * Todo: does only support {@link Polygon#outline}
     */
    default microtrafficsim.core.vis.scenario.areas.Area getProjectedArea(
            Projection projection,
            TypedPolygonArea area)
    {
        Coordinate[] coordinates = getCoordinates();

        Vec2d[] outline = new Vec2d[coordinates.length];
        for (int i = 0; i < outline.length; i++)
            outline[i] = projection.project(coordinates[i]);

        return new microtrafficsim.core.vis.scenario.areas.Area(
                new Polygon(outline),
                area.getType(),
                area.isMonitored());
    }
}