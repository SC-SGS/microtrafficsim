package microtrafficsim.core.map.features;

import microtrafficsim.core.frameworks.street.StreetEntity;
import microtrafficsim.core.map.Coordinate;


/**
 * Feature-primitive containing all data necessary for basic street rendering,
 * extending the {@code MultiLine} class.
 * 
 * @author Maximilian Luz
 */
public class Street extends MultiLine {

	private StreetEntity entity;
	public final float layer;
	public final double length;
	public final double[] distances;

	// to be extended

	public Street(long id, Coordinate[] nodes, float layer, double length, double[] distances) {
		super(id, nodes);

		this.entity = null;
		this.layer = layer;
		this.length = length;
		this.distances = distances;
	}


	public StreetEntity getEntity() {
		return entity;
	}

	public void setEntity(StreetEntity entity) {
		this.entity = entity;
	}
}
