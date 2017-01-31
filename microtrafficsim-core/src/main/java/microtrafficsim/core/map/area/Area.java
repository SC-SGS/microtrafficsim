package microtrafficsim.core.map.area;

import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.Mappable;


/**
 * This interface represents an area, which should serve basic methods for area interaction like
 * {@link #contains(Coordinate)}.
 *
 * @author Dominic Parga Cacheiro
 */
public interface Area {

    /**
     * If borders are included or not depends on the extending method.
     *
     * @param c Checks whether this coordinate is in this area
     * @return true, if the coordinate is in this area; false otherwise
     */
    boolean contains(Coordinate c);

    /**
     * Per default, this method calls {@link #contains(Coordinate)} using the mappable's coordinate.
     *
     * @param m Checks whether this mappable is in this area
     * @return true, if the mappable is in this area; false otherwise
     */
    default boolean contains(Mappable m) {
        return contains(m.getCoordinate());
    }
}