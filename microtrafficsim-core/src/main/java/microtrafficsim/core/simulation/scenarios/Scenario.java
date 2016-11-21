package microtrafficsim.core.simulation.scenarios;

import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.map.area.Area;
import microtrafficsim.core.simulation.builder.Builder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.utils.ODMatrix;

import java.util.Collection;

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
     * @param isBuilt should be true if ODMatrix has been set by {@link #setODMatrix(ODMatrix)} correctly (<=> not null)
     */
    void setODMatrixBuilt(boolean isBuilt);

    /**
     * @return whether the matrix has been built or not
     */
    boolean isODMatrixBuilt();

    /**
     * @param matrix the matrix of this scenario gets set to this value and determines the routes of this scenario
     */
    void setODMatrix(ODMatrix matrix);

    /**
     * @return the matrix used in this scenario determining the routes of this scenario
     */
    ODMatrix getODMatrix();

    /*
    |==================|
    | start/end fields |
    |==================|
    */
    /**
     * @return a predefined collection of origin-areas for creating the {@link ODMatrix} in a {@link Builder}
     */
    Collection<Area> getOriginFields();

    /**
     * @return a predefined collection of destination-areas for creating the {@link ODMatrix} in a {@link Builder}
     */
    Collection<Area> getDestinationFields();
}
