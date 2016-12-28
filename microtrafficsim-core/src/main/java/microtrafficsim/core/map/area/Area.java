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
    boolean contains(Coordinate c);

    default boolean contains(Mappable m) {
        return contains(m.getCoordinate());
    }
}