package microtrafficsim.core.shortestpath.astar;

import microtrafficsim.core.shortestpath.ShortestPathEdge;

/**
 * Java has no tuples...
 * 
 * @author Dominic Parga Cacheiro
 */
class EdgeWeightTuple {
	float weight;
	ShortestPathEdge edge;
	
	EdgeWeightTuple(float weight, ShortestPathEdge edge) {
		this.weight = weight;
		this.edge = edge;
	}
}
