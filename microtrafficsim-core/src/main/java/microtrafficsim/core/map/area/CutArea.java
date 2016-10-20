package microtrafficsim.core.map.area;

import microtrafficsim.core.map.Coordinate;


/**
 * This area is a simple approach for an area with hole or cut part. It just saves two areas, one defining "in"
 * excluding the second one.
 *
 * @author Dominic Parga Cacheiro
 */
public class CutArea implements Area {

    private Area in;
    private Area out;

    /**
     * Default constructor.
     *
     * @param in  The {@link CutArea} contains all {@link Coordinate}s in this given area called "in" except for
     *            all coordinates in the given area called "out".
     * @param out The {@link CutArea} contains all {@link Coordinate}s in the given area called "in" except for
     *            all coordinates in this given area called "out".
     */
    public CutArea(Area in, Area out) {
        this.in  = in;
        this.out = out;
    }

    @Override
    public boolean contains(Coordinate c) {
        return in.contains(c) && !out.contains(c);
    }
}
