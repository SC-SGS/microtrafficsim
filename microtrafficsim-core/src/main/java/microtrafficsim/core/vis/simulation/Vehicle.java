package microtrafficsim.core.vis.simulation;

import microtrafficsim.core.frameworks.vehicle.IVisualizationVehicle;
import microtrafficsim.core.frameworks.vehicle.VehicleEntity;
import microtrafficsim.core.logic.DirectedEdge;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.core.vis.opengl.utils.Color;


public class Vehicle implements IVisualizationVehicle {
	
	private final static float MIN_TARGET_DISTANCE = 1.0f;

	private VehicleEntity entity;
	private Coordinate position;
	private Coordinate target;
	private Color color;

	public Vehicle(Color color) {
		this.entity = null;
		this.position = new Coordinate(0, 0);
		this.color = color;

		target = new Coordinate(1, 1);
	}

	@Override
	public void setEntity(VehicleEntity entity) {
		this.entity = entity;
	}

	@Override
	public VehicleEntity getEntity() {
		return entity;
	}

	@Override
	public Coordinate getTarget() {
		return target;
	}
	
	@Override
	public Color getBaseColor() {
		return this.color;
	}
	
	@Override
	public void setBaseColor(Color color) {
		this.color = color;
	}

	@Override
	public void updatePosition() {
		DirectedEdge edge = entity.getLogic().getDirectedEdge();
		Street geom = edge.getEntity().getGeometry();
		
		double pos = entity.getLogic().getCellPosition() + 0.5f;
		pos = pos * (geom.length / edge.getLength());
		
		Coordinate a;
		Coordinate b;
		Coordinate c;
		double dSegment = 0;
		double dToSegment = 0;
		
		if (edge == edge.getEntity().getForwardEdge()) {
			int segment = 0;
			for (int i = 0; i < geom.distances.length; i++) {
				segment = i;
				dSegment = geom.distances[i];
				
				double nd = dToSegment + dSegment;
				if (nd > pos)
					break;
				
				dToSegment = nd;
			}
			
			a = geom.coordinates[segment];
			b = geom.coordinates[segment + 1];
			if (segment + 2 < geom.coordinates.length)
				c = geom.coordinates[segment + 2];
			else
				c = b;
			
		} else {
			int segment = geom.distances.length - 1;
			for (int i = geom.distances.length - 1; i >= 0; i--) {
				segment = i;
				dSegment = geom.distances[i];
				
				double nd = dToSegment + dSegment;
				if (nd > pos)
					break;
				
				dToSegment = nd;
			}
			
			a = geom.coordinates[segment + 1];
			b = geom.coordinates[segment];
			if (segment - 1 >= 0)
				c = geom.coordinates[segment - 1];
			else
				c = b;
		}
		
		double pSegment = pos - dToSegment;
		
		if (dSegment - pSegment < MIN_TARGET_DISTANCE) {
			target.lat = c.lat;
			target.lon = c.lon;
		} else {
			target.lat = b.lat;
			target.lon = b.lon;
		}
		
		pSegment /= dSegment;
		position.lat = a.lat + (b.lat - a.lat) * pSegment;
		position.lon = a.lon + (b.lon - a.lon) * pSegment;
	}

	public Coordinate getPosition() {
		return position;
	}
}