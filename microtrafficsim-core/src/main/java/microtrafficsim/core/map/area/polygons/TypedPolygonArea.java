package microtrafficsim.core.map.area.polygons;

import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.vis.scenario.areas.Area;

public class TypedPolygonArea extends BasicPolygonArea {

    public Area.Type type;

    public TypedPolygonArea(Coordinate[] coordinates, Area.Type type) {
        super(coordinates);
        this.type = type;
    }

    public Area.Type getType() {
        return type;
    }
}