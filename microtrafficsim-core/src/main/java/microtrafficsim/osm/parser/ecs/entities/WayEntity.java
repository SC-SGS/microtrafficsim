package microtrafficsim.osm.parser.ecs.entities;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.Entity;
import microtrafficsim.osm.parser.features.FeatureDefinition;
import microtrafficsim.utils.collections.ArrayUtils;


/**
 * Way-Entity containing all way-relevant information, including id, an ordered
 * List of Nodes and a set of {@code FeatureDefinition}s which the
 * source-element of this entity matches.
 * 
 * @author Maximilian Luz
 */
public class WayEntity extends Entity {
	
	public long id;
	public long[] nodes;
	public Set<FeatureDefinition> features;
	
	public WayEntity(long id, long[] nodes, Set<FeatureDefinition> features) {
		this.id = id;
		this.nodes = nodes;
		
		this.features = features;
	}
	
	
	@Override
	public Collection<Long> getRequiredNodes() {
		Collection<Long> required = super.getRequiredNodes();
		required.addAll(ArrayUtils.toList(nodes));
		return required;
	}
	
	@Override
	public WayEntity clone() {
		long[] nodes = this.nodes.clone();
		Set<FeatureDefinition> features = new HashSet<>(this.features);
		WayEntity ce = new WayEntity(this.id, nodes, features);
		
		for (Component component : this.components.values()) {
			Component cc = component.clone(ce);
			ce.components.put(cc.getType(), cc);
		}
		
		return ce;
	}
}
