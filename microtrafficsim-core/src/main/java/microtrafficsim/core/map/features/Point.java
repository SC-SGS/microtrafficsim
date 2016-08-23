package microtrafficsim.core.map.features;

import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.FeaturePrimitive;


/**
 * A simple point feature-primitive containing only a coordinate.
 *
 * @author Maximilian Luz
 */
public class Point extends FeaturePrimitive {
    public Coordinate coordinate;

    /**
     * Constructs a new {@code Point}.
     *
     * @param id         the (unique) id of the point.
     * @param coordinate the coordinate describing this point.
     */
    public Point(long id, Coordinate coordinate) {
        super(id);
        this.coordinate = coordinate;
    }
}
