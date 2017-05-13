package microtrafficsim.core.vis.simulation;

import microtrafficsim.core.simulation.builder.impl.VisVehicleFactory;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.vis.Overlay;


/**
 * Overlay to display simulated vehicles.
 *
 * @author Dominic Parga Cacheiro
 */
public interface VehicleOverlay extends Overlay {

    /**
     * Set the simulation of which the vehicles should be display with this overlay.
     *
     * @param simulation the simulation to be displayed.
     */
    void setSimulation(Simulation simulation);

    /**
     * Returns the factory used to create new, default-initialized visualization-components for vehicles.
     *
     * @return the visualization-vehicle factory.
     */
    VisVehicleFactory getVehicleFactory();
}
