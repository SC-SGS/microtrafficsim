package microtrafficsim.core.frameworks.street;

import microtrafficsim.core.logic.DirectedEdge;
import microtrafficsim.core.map.features.Street;


public class StreetEntity {

	private final DirectedEdge forward;
	private final DirectedEdge backward;
	private final Street geometry;

	public StreetEntity(DirectedEdge forward, DirectedEdge backward, Street geometry) {
		this.forward = forward;
		this.backward = backward;
		this.geometry = geometry;
	}
	
	
	public DirectedEdge getForwardEdge() {
		return forward;
	}
	
	public DirectedEdge getBackwardEdge() {
		return backward;
	}
	
	public Street getGeometry() {
		return geometry;
	}
}