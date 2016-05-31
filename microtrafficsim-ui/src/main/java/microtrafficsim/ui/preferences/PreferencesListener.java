package microtrafficsim.ui.preferences;

import microtrafficsim.core.simulation.controller.configs.SimulationConfig;

/**
 * @author Dominic Parga Cacheiro
 */
public interface PreferencesListener {

    void didAcceptChanges(SimulationConfig newConfig);
}
