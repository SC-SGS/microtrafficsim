package microtrafficsim.core.map.area.polygons;

import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.vis.scenario.areas.Area;

public class TypedPolygonArea extends BasicPolygonArea {

    private Area.Type type;
    private boolean isMonitored;

    public TypedPolygonArea(Coordinate[] coordinates, Area.Type type) {
        this(coordinates, type, false);
    }

    public TypedPolygonArea(Coordinate[] coordinates, Area.Type type, boolean isMonitored) {
        super(coordinates);
        this.type = type;
        this.isMonitored = isMonitored;
    }

    public Area.Type getType() {
        return type;
    }

    public boolean isMonitored() {
        return isMonitored;
    }
}