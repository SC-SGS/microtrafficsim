package microtrafficsim.core.simulation.builder;

import microtrafficsim.core.simulation.core.OldSimulation;
import microtrafficsim.core.simulation.scenarios.Scenario;

/**
 * <p>
 * A simulation setup consists of three major parts: <br>
 * &bull {@link OldSimulation}: the executor of simulation steps <br>
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

    void prepare(Scenario scenario);
}
