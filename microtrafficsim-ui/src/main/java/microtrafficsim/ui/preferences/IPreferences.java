package microtrafficsim.ui.preferences;

import microtrafficsim.core.simulation.controller.configs.SimulationConfig;

/**
 * @author Dominic Parga Cacheiro
 */
public interface IPreferences {

    void create();
    void initSettings();

    /**
     *
     * @param newConfig second instance for resetting later
     * @return true if an error occured
     */
    boolean acceptChanges(SimulationConfig newConfig);
    void cancelChanges();
    void addListener(PreferencesListener listener);
}
