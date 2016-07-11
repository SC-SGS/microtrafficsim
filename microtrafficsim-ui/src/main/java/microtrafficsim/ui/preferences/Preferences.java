package microtrafficsim.ui.preferences;

import microtrafficsim.core.simulation.configs.SimulationConfig;


/**
 * @author Dominic Parga Cacheiro
 */
public interface Preferences {
    void create();
    void setSettings(SimulationConfig config);
    SimulationConfig getCorrectSettings() throws IncorrectSettingsException;
    void setEnabled(PrefElement id, boolean enabled);
    void setAllEnabled(boolean enabled);
    default void addSubpreferences(Preferences preferences) {}
}
