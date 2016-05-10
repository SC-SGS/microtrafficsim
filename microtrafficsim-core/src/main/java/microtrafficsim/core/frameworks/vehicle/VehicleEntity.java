package microtrafficsim.core.frameworks.vehicle;

import microtrafficsim.core.simulation.controller.configs.SimulationConfig;

public class VehicleEntity {
	
	private ILogicVehicle logic;
	private IVisualizationVehicle visualization;
	private SimulationConfig config;

	public VehicleEntity(SimulationConfig config, ILogicVehicle logic, IVisualizationVehicle visualization) {
		this.config = config;
		this.logic = logic;
		this.visualization = visualization;
	}
	
	public ILogicVehicle getLogic() {
		return logic;
	}

	public IVisualizationVehicle getVisualization() {
		return visualization;
	}
	
	public SimulationConfig getConfig() {
		return config;
	}
}