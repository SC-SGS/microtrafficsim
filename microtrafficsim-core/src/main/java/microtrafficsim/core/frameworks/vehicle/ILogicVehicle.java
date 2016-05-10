package microtrafficsim.core.frameworks.vehicle;

import microtrafficsim.core.logic.DirectedEdge;


public interface ILogicVehicle {
	
	VehicleEntity getEntity();
    void setEntity(VehicleEntity entity);
	DirectedEdge getDirectedEdge();
	int getCellPosition();
}