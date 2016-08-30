package microtrafficsim.core.entities.vehicle;

import microtrafficsim.core.simulation.configs.SimulationConfig;


public class VehicleEntity {

    private LogicVehicleEntity    logic;
    private VisualizationVehicleEntity visualization;
    private SimulationConfig      config;

    public VehicleEntity(SimulationConfig config, LogicVehicleEntity logic, VisualizationVehicleEntity visualization) {
        this.config        = config;
        this.logic         = logic;
        this.visualization = visualization;
    }

    public LogicVehicleEntity getLogic() {
        return logic;
    }

    public VisualizationVehicleEntity getVisualization() {
        return visualization;
    }

    public SimulationConfig getConfig() {
        return config;
    }
}