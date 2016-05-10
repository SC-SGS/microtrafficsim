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
	
	public MultiLine(long id, Coordinate[] coordinates) {
		super(id);
		this.coordinates = coordinates;
	}
}
