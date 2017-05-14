package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streetgraph.UnmodifiableGraph;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.scenarios.containers.impl.ConcurrentVehicleContainer;

/**
 * This class should only implement the basic stuff for children classes.
 *
 * @author Dominic Parga Cacheiro
 */
public abstract class BasicScenario implements Scenario {
    private final SimulationConfig config;
    private final Graph            graph;
    private final VehicleContainer vehicleContainer;
    private boolean                isPrepared;

    /**
     * Default constructor
     *
     * @param config this config is used for internal settings and should be set already
     * @param graph used for route definitions etc.
     * @param vehicleContainer stores and manages vehicles running in this scenario
     */
    protected BasicScenario(SimulationConfig config,
                            Graph graph,
                            VehicleContainer vehicleContainer) {
        this.config = config;
        this.graph = graph;
        this.vehicleContainer = vehicleContainer;
        this.isPrepared = false;
    }

    protected BasicScenario(SimulationConfig config, Graph graph) {
        this(config, graph, new ConcurrentVehicleContainer());
    }

    @Override
    public final SimulationConfig getConfig() {
        return config;
    }

    @Override
    public final Graph getGraph() {
        return new UnmodifiableGraph(graph);
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
}
