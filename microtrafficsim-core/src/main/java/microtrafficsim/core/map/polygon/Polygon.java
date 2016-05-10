package microtrafficsim.core.map.polygon;

import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.Mappable;

/**
 * @author Dominic Parga Cacheiro
 */
public interface Polygon {

    public boolean contains(Coordinate c);

    public boolean contains(Mappable m);
}