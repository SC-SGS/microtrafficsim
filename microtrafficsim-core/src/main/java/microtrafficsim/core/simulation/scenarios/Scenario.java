package microtrafficsim.core.simulation.scenarios;

import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.simulation.builder.Builder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.OldSimulation;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;

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

    SimulationConfig getConfig();

    StreetGraph getGraph();

    boolean isPrepared();

    VehicleContainer getVehicleContainer();
}
