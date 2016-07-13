package microtrafficsim.core.entities.vehicle;

import microtrafficsim.core.logic.DirectedEdge;


public interface LogicVehicleEntity {
    VehicleEntity getEntity();
    void setEntity(VehicleEntity entity);

    DirectedEdge getDirectedEdge();

    int getCellPosition();
}