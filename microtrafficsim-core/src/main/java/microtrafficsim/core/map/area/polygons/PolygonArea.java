package microtrafficsim.core.map.area.polygons;

import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.area.Area;


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
}