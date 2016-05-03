package microtrafficsim.osm.parser.features.streets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import microtrafficsim.core.frameworks.street.StreetEntity;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.Feature;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.core.parser.MapFeatureGenerator;
import microtrafficsim.core.parser.StreetGraphWayComponent;
import microtrafficsim.math.DistanceCalculator;
import microtrafficsim.math.HaversineDistanceCalculator;
import microtrafficsim.osm.parser.base.DataSet;
import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.entities.NodeEntity;
import microtrafficsim.osm.parser.ecs.entities.WayEntity;
import microtrafficsim.osm.parser.features.FeatureDefinition;
import microtrafficsim.osm.parser.processing.osm.sanitizer.SanitizerWayComponent;


public class StreetFeatureGenerator implements MapFeatureGenerator<Street> {
	
	private DistanceCalculator distcalc;
	private Map<String, Feature<Street>> generated;
	
	
	public StreetFeatureGenerator() {
		this(HaversineDistanceCalculator::getDistance);
	}
	
	public StreetFeatureGenerator(DistanceCalculator distcalc) {
		this.distcalc = distcalc;
		this.generated = new HashMap<>();
	}
	
	@Override
	public Map<String, Feature<Street>> getGeneratedFeatures() {
		return generated;
	}
	

	@Override
	public void execute(DataSet dataset, FeatureDefinition feature) {
		ArrayList<Street> streets = new ArrayList<>();
		ArrayList<Coordinate> coords = new ArrayList<>();
		
		for (WayEntity way : dataset.ways.values()) {
			if (!way.features.contains(feature)) continue;
			
			// get coordinates
			for (long ref : way.nodes) {
				NodeEntity node = dataset.nodes.get(ref);
				coords.add(new Coordinate(node.lat, node.lon));
			}
			
			// calculate distances
			double[] dist = new double[coords.size() - 1];
			Coordinate a = coords.get(0);
			Coordinate b;
			for (int i = 1; i < coords.size(); i++) {
				b = coords.get(i);
				dist[i - 1] = distcalc.getDistance(a, b);
				a = b;
			}
			
			// calculate length
			float len = 0;
			for (double d : dist)
				len += d;


			// create street
			StreetComponent sc = way.get(StreetComponent.class);
			Street street = new Street(way.id, coords.toArray(new Coordinate[coords.size()]), sc.layer, len, dist);
			
			StreetGraphWayComponent sgwc = way.get(StreetGraphWayComponent.class);
			if (sgwc != null) {
				StreetEntity entity = new StreetEntity(sgwc.forward, sgwc.backward, street);
				street.setEntity(entity);
				
				if (sgwc.forward != null)
					sgwc.forward.setEntity(entity);
				
				if (sgwc.backward != null)
					sgwc.backward.setEntity(entity);
			}
			
			streets.add(street);
			coords.clear();
		}
		
		Street[] data = streets.toArray(new Street[streets.size()]);
		generated.put(feature.getName(), new Feature<>(feature.getName(), Street.class, data));
	}


	@Override
	public Set<Class<? extends Component>> getRequiredWayComponents() {
		HashSet<Class<? extends Component>> required  = new HashSet<>();
		required.add(StreetComponent.class);
		required.add(SanitizerWayComponent.class);
		return required;
	}
}
