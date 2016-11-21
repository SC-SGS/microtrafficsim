package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.map.area.Area;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.utils.ODMatrix;

import java.util.Collection;

/**
 * This class should only implement the basic stuff for children classes. You can extend: <br>
 * &bull {@link #setODMatrix(ODMatrix)} <br>
 * &bull {@link #getODMatrix()} <br>
 * &bull {@link #getOriginFields()} <br>
 * &bull {@link #getDestinationFields()} <br>
 * Default return value for all of them is null.
 *
 * @author Dominic Parga Cacheiro
 */
public abstract class BasicScenario implements Scenario {

    private final SimulationConfig config;
    private final StreetGraph graph;
    private final VehicleContainer vehicleContainer;
    private boolean isPrepared, isODMatrixBuilt;

    /**
     * Default constructor
     */
    protected BasicScenario(SimulationConfig config,
                            StreetGraph graph,
                            VehicleContainer vehicleContainer) {
        this.config = config;
        this.graph = graph;
        this.vehicleContainer = vehicleContainer;
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
    public final void setODMatrixBuilt(boolean isBuilt) {
        this.isODMatrixBuilt = isBuilt;
    }

    @Override
    public final boolean isODMatrixBuilt() {
        return isODMatrixBuilt;
    }

    /**
     * Does nothing per default.
     *
     * @param matrix the matrix of this scenario gets set to this value and determines the routes of this scenario
     */
    @Override
    public void setODMatrix(ODMatrix matrix) {

    }

    /**
     * @return null per default
     */
    @Override
    public ODMatrix getODMatrix() {
        return null;
    }

    /**
     * @return null per default
     */
    @Override
    public Collection<Area> getOriginFields() {
        return null;
    }

    /**
     * @return null per default
     */
    @Override
    public Collection<Area> getDestinationFields() {
        return null;
    }
}
