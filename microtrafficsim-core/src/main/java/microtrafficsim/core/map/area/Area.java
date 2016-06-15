package microtrafficsim.core.map.area;

import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.Mappable;


/**
 * TODO
 *
 * @author Dominic Parga Cacheiro
 */
public interface Area {
    boolean contains(Coordinate c);

    default boolean contains(Mappable m) {
        return contains(m.getCoordinate());
    }
}