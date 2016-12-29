package microtrafficsim.core.map.features;

import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.FeaturePrimitive;


public class Polygon extends FeaturePrimitive {

    public Coordinate[] outline;

    /**
     * Constructs a new {@code Polygon}.
     *
     * @param id      the (unique) id of the multi-line.
     * @param outline the (ordered) array of coordinates describing the outline of the polygon.
     */
    public Polygon(long id, Coordinate[] outline) {
        super(id);
        this.outline = outline;
    }
}
