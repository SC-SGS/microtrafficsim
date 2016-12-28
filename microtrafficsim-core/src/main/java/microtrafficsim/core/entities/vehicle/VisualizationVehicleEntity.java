package microtrafficsim.core.entities.vehicle;

import microtrafficsim.core.vis.opengl.utils.Color;


/**
 * Visualization component of a vehicle.
 *
 * @author Maximilian Luz
 */
public interface VisualizationVehicleEntity {

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
     * Updates the position of the vehicle.
     */
    void updatePosition();

    /**
     * Returns the base-color of this vehicle.
     *
     * @return the base-color of this vehicle.
     */
    Color getBaseColor();

    /**
     * Sets the base-color of this vehicle.
     *
     * @param color the new base-color of this vehicle.
     */
    void setBaseColor(Color color);
}