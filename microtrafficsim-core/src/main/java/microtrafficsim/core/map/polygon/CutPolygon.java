package microtrafficsim.core.map.polygon;

import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.Mappable;

/**
 * TODO
 *
 * @author Dominic Parga Cacheiro
 */
public class CutPolygon implements Polygon {

    private Polygon in;
    private Polygon out;

    /**
     * Default constructor.
     *
     * @param in The {@link CutPolygon} contains all {@link Coordinate}s in this given polygon called "in" except for
     *           all coordinates in the given polygon called "out".
     * @param out The {@link CutPolygon} contains all {@link Coordinate}s in the given polygon called "in" except for
     *           all coordinates in this given polygon called "out".
     */
    public CutPolygon(Polygon in, Polygon out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public boolean contains(Coordinate c) {
        return in.contains(c) && !out.contains(c);
    }

    @Override
    public boolean contains(Mappable m) {
        Coordinate c = m.getCoordinate();
        return in.contains(c) && !out.contains(c);
    }
}
