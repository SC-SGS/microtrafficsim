package microtrafficsim.core.entities.vehicle;

import microtrafficsim.core.logic.streets.DirectedEdge;


/**
 * Serves methods of a vehicle entity used by other parts of this vehicle entity.
 *
 * @author Dominic Parga Cacheiro
 */
public interface LogicVehicleEntity {

    /**
     * Returns the {@code VehicleEntity} of this component.
     *
     * @return the {@code VehicleEntity} of this component.
     */
    VehicleEntity getEntity();

    /**
     * Sets the {@code VehicleEntity} of this component.
     *
     * @param entity the new {@code VehicleEntity} of this component.
     */
    void setEntity(VehicleEntity entity);


    /**
     * Returns the {@code DirectedEdge} of this vehicle.
     *
     * @return the {@code DirectedEdge} of this vehicle.
     */
    DirectedEdge getDirectedEdge();

    /**
     * Returns the cell position of this vehicle on its street.
     *
     * @return the cell position of this vehicle on its street.
     */
    int getCellPosition();
}