package microtrafficsim.osm.parser.ecs.entities;

import java.util.HashSet;
import java.util.Set;

import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.Entity;
import microtrafficsim.osm.parser.features.FeatureDefinition;


/**
 * Node-Entity containing all node-relevant information, including id,
 * latitude, longitude and a set of {@code FeatureDefinition}s which the
 * source-element of this entity matches.
 * 
 * @author Maximilian Luz
 */
public class NodeEntity extends Entity {
	public long id;
	public double lat;
	public double lon;
	public Set<FeatureDefinition> features;
	
	
	public NodeEntity(long id, double lat, double lon, Set<FeatureDefinition> features) {
		this.id = id;
		this.lat = lat;
		this.lon = lon;
		
		this.features = features;
	}
	
	@Override
	public NodeEntity clone() {
		Set<FeatureDefinition> features = new HashSet<>(this.features);
		NodeEntity ce = new NodeEntity(this.id, this.lat, this.lon, features);
		
		for (Component component : this.components.values()) {
			Component cc = component.clone(ce);
			ce.components.put(cc.getType(), cc);
		}
		
		return ce;
	}
}
