package microtrafficsim.core.frameworks.shortestpath.astar;

import microtrafficsim.core.frameworks.shortestpath.IDijkstrableEdge;

/**
 * Java has no tuples...
 * 
 * @author Dominic Parga Cacheiro
 */
class EdgeWeightTuple {
	float weight;
	IDijkstrableEdge edge;
	
	EdgeWeightTuple(float weight, IDijkstrableEdge edge) {
		this.weight = weight;
		this.edge = edge;
	}
}
