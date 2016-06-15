package microtrafficsim.core.map.area;

import microtrafficsim.core.map.Coordinate;


/**
 * Simple area area, defined by a list of {@link Coordinate Coordinate}s.
 *
 * @author Maximilian Luz
 */
public interface ISimplePolygon extends Area {

    /**
     * Return the (ordered) list of coordinates that define this area.
     * @return the array of coordinate defining this area.
     */
    Coordinate[] getCoordinates();
}
