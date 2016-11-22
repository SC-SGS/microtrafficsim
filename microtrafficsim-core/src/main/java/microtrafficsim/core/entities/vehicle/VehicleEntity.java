package microtrafficsim.core.entities.vehicle;

import microtrafficsim.core.simulation.configs.SimulationConfig;


/**
 * This vehicle entity contains the logic and geometric parts of a vehicle. This class is used for communication between
 * logic and visualization of a vehicle.
 *
 * @author Maximilian Luz, Dominic Parga Cacheiro
 */
public class VehicleEntity {

    private LogicVehicleEntity    logic;
    private VisualizationVehicleEntity visualization;

    public VehicleEntity(LogicVehicleEntity logic, VisualizationVehicleEntity visualization) {
        this.logic         = logic;
        this.visualization = visualization;
    }

    /**
     * Returns the logic part of this entity.
     *
     * @return the logic part of this entity.
     */
    public LogicVehicleEntity getLogic() {
        return logic;
    }

    /**
     * Returns the visualization part of this entity.
     *
     * @return the visualization part of this entity.
     */
    public VisualizationVehicleEntity getVisualization() {
        return visualization;
    }
}