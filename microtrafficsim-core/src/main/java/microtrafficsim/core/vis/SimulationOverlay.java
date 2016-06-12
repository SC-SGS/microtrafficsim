package microtrafficsim.core.vis;

import microtrafficsim.core.frameworks.vehicle.IVisualizationVehicle;
import microtrafficsim.core.simulation.Simulation;

import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public interface SimulationOverlay extends Overlay {
  void setSimulation(Simulation simulation);
  Supplier<IVisualizationVehicle> getVehicleFactory();
}
