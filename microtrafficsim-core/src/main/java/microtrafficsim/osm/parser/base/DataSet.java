package microtrafficsim.osm.parser.base;

import java.util.HashMap;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.osm.parser.ecs.entities.NodeEntity;
import microtrafficsim.osm.parser.ecs.entities.WayEntity;
import microtrafficsim.osm.parser.relations.RelationCollection;


/**
 * A collection for all parser-internal data after abstraction.
 * 
 * @author Maximilian Luz
 */
public class DataSet {
	public Bounds bounds;
	public HashMap<Long, NodeEntity> nodes;
	public HashMap<Long, WayEntity> ways;
	public RelationCollection relations;
	
	public DataSet() {
		this.bounds = null;
		this.nodes = new HashMap<>();
		this.ways = new HashMap<>();
		this.relations = new RelationCollection();
	}
}
