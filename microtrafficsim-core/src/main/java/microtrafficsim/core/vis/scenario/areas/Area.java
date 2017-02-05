package microtrafficsim.core.vis.scenario.areas;

import microtrafficsim.math.geometry.polygons.Polygon;


public class Area {
    public Polygon polygon;
    public Type type;

    public Area(Polygon polygon, Type type) {
        this.polygon = polygon;
        this.type = type;
    }

    public enum Type { ORIGIN, DESTINATION }
}
