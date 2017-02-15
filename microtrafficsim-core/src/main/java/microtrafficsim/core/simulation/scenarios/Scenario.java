package microtrafficsim.core.simulation.scenarios;

import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.core.StepListener;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.utils.ODMatrix;
import microtrafficsim.utils.Resettable;

import java.util.function.Supplier;

/**
 * <p>
 * A simulation setup consists of three major parts: <br>
 * &bull {@link Simulation}: the executor of simulation steps <br>
 * &bull {@link Scenario}: the definition of routes etc. <br>
 * &bull {@link ScenarioBuilder}: the scenario builder; e.g. pre-calculating routes by a
 * given scenario
 *
 * <p>
 * The scenario defines vehicle routes, the simulation config etc. and is
 * executed by the simulation after a builder prepared it.
 *
 * @author Dominic Parga Cacheiro
 */
public interface Scenario extends StepListener, Resettable {

    /*
    |=========|
    | general |
    |=========|
    */
    /**
     * @return config file of this scenario including all important information about it
     */
    ScenarioConfig getConfig();

    /**
     * @return streetgraph used in this scenario
     */
    Graph getGraph();

    /**
     * @return the vehicle container managing (not) spawned vehicles of this scenario
     */
    VehicleContainer getVehicleContainer();

    /**
     * @param isPrepared sets the prepared-state of this scenario to this value
     */
    void setPrepared(boolean isPrepared);

    /**
     * @return whether this scenario has already been prepared by a {@link ScenarioBuilder}
     */
    boolean isPrepared();
    
    /*
    |===========================|
    | origin-destination-matrix |
    |===========================|
    */
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


    /*
    |==================|
    | (i) StepListener |
    |==================|
    */
    @Override
    default void didOneStep(Simulation simulation) {

    }


    /*
    |================|
    | (i) Resettable |
    |================|
    */
    /**
     * Resets this scenario by<br>
     * &bull setting prepared to false <br>
     * &bull clearing the vehicle container <br>
     * &bull resetting the streetgraph
     *
     * @see #setPrepared(boolean)
     * @see #getVehicleContainer()
     * @see VehicleContainer#clearAll()
     * @see #getGraph()
     * @see Graph#reset()
     */
    @Override
    default void reset() {
        setPrepared(false);
        getVehicleContainer().clearAll();
        getGraph().reset();
    }
}
