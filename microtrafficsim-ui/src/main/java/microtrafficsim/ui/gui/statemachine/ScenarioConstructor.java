package microtrafficsim.ui.gui.statemachine;

import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.ui.gui.statemachine.impl.SimulationController;

/**
 * This interface gives the opportunity to call the constructor of {@link SimulationController} with a parameter,
 * that is the constructor of the used Simulation.
 *
 * @author Dominic Parga Cacheiro
 */
public interface ScenarioConstructor {
    Scenario instantiate(SimulationConfig config, StreetGraph streetgraph);
}
