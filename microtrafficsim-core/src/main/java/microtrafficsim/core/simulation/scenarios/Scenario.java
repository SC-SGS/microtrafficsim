package microtrafficsim.core.simulation.scenarios;

import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.map.area.Area;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.simulation.builder.Builder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.utils.ODMatrix;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * <p>
 * A simulation setup consists of three major parts: <br>
 * &bull {@link Simulation}: the executor of simulation steps <br>
 * &bull {@link Scenario}: the definition of routes etc. <br>
 * &bull {@link Builder}: the scenario builder; e.g. pre-calculating routes by a
 * given scenario
 *
 * <p>
 * The scenario defines vehicle routes, the simulation config etc. and is
 * executed by the simulation after a builder prepared it.
 *
 * <p>
 * It is important to call {@link #setODMatrixBuilt(boolean)} setting the value to true, if you define the
 * origin-destination matrix itself and not
 *
 * @author Dominic Parga Cacheiro
 */
public interface Scenario {

    /*
    |=========|
    | general |
    |=========|
    */
    /**
     * @return config file of this scenario including all important information about it
     */
    SimulationConfig getConfig();

    /**
     * @return streetgraph used in this scenario
     */
    StreetGraph getGraph();

    /**
     * @return the vehicle container managing (not) spawned vehicles of this scenario
     */
    VehicleContainer getVehicleContainer();

    /**
     * @param isPrepared sets the prepared-state of this scenario to this value
     */
    void setPrepared(boolean isPrepared);

    /**
     * @return whether this scenario has already been prepared by a {@link Builder}
     */
    boolean isPrepared();
    
    /*
    |===========================|
    | origin-destination-matrix |
    |===========================|
    */
    /**
     * @param matrix the matrix of this scenario gets set to this value and determines the routes of this scenario
     */
    void setODMatrix(ODMatrix matrix);

    /**
     * @return the matrix used in this scenario determining the routes of this scenario
     */
    ODMatrix getODMatrix();

    /*
    |================|
    | route creation |
    |================|
    */
    /**
     * @return A scout factory serving a ready shortest path algorithm for vehicle route calculation
     */
    Supplier<ShortestPathAlgorithm> getScoutFactory();
}
