package microtrafficsim.core.map.features;

import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.FeaturePrimitive;


/**
 * A feature-primitive consisting of multiple adjacent line-segments.
 *
 * @author Maximilian Luz
 */
public class MultiLine extends FeaturePrimitive {
    public Coordinate[] coordinates;

    /**
     * Constructs a new {@code MultiLine}.
     *
     * @param id          the (unique) id of the multi-line.
     * @param coordinates the (ordered) array of coordinates describing this multi-line.
     */
    public MultiLine(long id, Coordinate[] coordinates) {
        super(id);
        this.coordinates = coordinates;
    }
}
