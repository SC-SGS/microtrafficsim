package microtrafficsim.ui.preferences;

import microtrafficsim.core.simulation.configs.ScenarioConfig;


/**
 * @author Dominic Parga Cacheiro
 */
public interface Preferences {
    void create();
    void setSettings(ScenarioConfig config);
    ScenarioConfig getCorrectSettings() throws IncorrectSettingsException;
    void setEnabled(PrefElement id, boolean enabled);
    void setAllEnabled(boolean enabled);
    default void addSubpreferences(Preferences preferences) {}
}
