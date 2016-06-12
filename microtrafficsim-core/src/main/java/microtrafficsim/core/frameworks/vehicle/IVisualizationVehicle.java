package microtrafficsim.core.frameworks.vehicle;

import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.vis.opengl.utils.Color;


public interface IVisualizationVehicle {
	
	VehicleEntity getEntity();
	void setEntity(VehicleEntity entity);

	void updatePosition();
	Coordinate getTarget();
	Coordinate getPosition();
	float getLayer();

	Color getBaseColor();
	void setBaseColor(Color color);
}