package microtrafficsim.core.simulation.builder;

import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.scenarios.Scenario;

/**
 * <p>
 * A simulation setup consists of three major parts: <br>
 * &bull {@link Simulation}: the executor of simulation steps <br>
 * &bull {@link Scenario}: the definition of routes etc. <br>
 * &bull {@link Builder}: the scenario builder; e.g. pre-calculating routes by a
 * given scenario
 *
 * <p>
 * The builder gets a scenario and prepares it before it is given to a
 * simulation.
 *
 * @author Dominic Parga Cacheiro
 */
public interface Builder {

    /**
     * Prepares the given scenario, e.g. it pre-calculates vehicle routes. If the scenario is already prepared, it
     * gets prepared again.
     *
     * @param scenario This scenario should be prepared
     * @return The prepared scenario (same reference as the given one, just for practical purposes)
     */
    Scenario prepare(Scenario scenario);
}
