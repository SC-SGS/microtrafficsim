package microtrafficsim.core.vis.scenario.areas;

import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.area.polygons.BasicPolygonArea;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.math.geometry.polygons.Polygon;


public class Area {
    public Polygon polygon;
    public Type type;

    public Area(Polygon polygon, Type type) {
        this.polygon = polygon;
        this.type = type;
    }

    public enum Type { ORIGIN, DESTINATION }

    /**
     * Todo: does only support {@link Polygon#outline}
     */
    public BasicPolygonArea getUnprojectedArea(Projection projection) {
        Coordinate[] coordinates = new Coordinate[polygon.outline.length];
        for (int i = 0; i < coordinates.length; i++)
            coordinates[i] = projection.unproject(polygon.outline[i]);

        return new BasicPolygonArea(coordinates);
    }
}
