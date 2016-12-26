package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.utils.ODMatrix;
import microtrafficsim.core.simulation.utils.SparseODMatrix;
import microtrafficsim.core.simulation.utils.UnmodifiableODMatrix;

/**
 * This class should only implement the basic stuff for children classes.
 *
 * @author Dominic Parga Cacheiro
 */
public abstract class BasicScenario implements Scenario {

    private final SimulationConfig config;
    private final StreetGraph graph;
    private final VehicleContainer vehicleContainer;
    private boolean isPrepared;
    protected ODMatrix odMatrix;

    /**
     * Default constructor
     */
    protected BasicScenario(SimulationConfig config,
                            StreetGraph graph,
                            VehicleContainer vehicleContainer) {
        this.config = config;
        this.graph = graph;
        this.vehicleContainer = vehicleContainer;

        this.odMatrix = new SparseODMatrix();
    }

    @Override
    public final SimulationConfig getConfig() {
        return config;
    }

    @Override
    public final StreetGraph getGraph() {
        return graph;
    }

    @Override
    public final VehicleContainer getVehicleContainer() {
        return vehicleContainer;
    }

    @Override
    public final void setPrepared(boolean isPrepared) {
        this.isPrepared = isPrepared;
    }

    @Override
    public final boolean isPrepared() {
        return isPrepared;
    }

    @Override
    public void setODMatrix(ODMatrix odMatrix) {
        this.odMatrix = odMatrix;
    }

    @Override
    public UnmodifiableODMatrix getODMatrix() {
        return new UnmodifiableODMatrix(odMatrix);
    }
}
