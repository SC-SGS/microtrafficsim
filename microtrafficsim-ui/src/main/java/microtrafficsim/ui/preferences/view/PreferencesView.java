package microtrafficsim.ui.preferences.view;

import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.ui.preferences.IncorrectSettingsException;
import microtrafficsim.ui.preferences.model.PreferencesModel;

/**
 * @author Dominic Parga Cacheiro
 */
public interface PreferencesView {

    PreferencesModel getModel();

    /**
     * Calls {@link #setSettings(boolean, SimulationConfig) setSettings(false, config)}
     */
    default void setSettings(SimulationConfig config) {
        setSettings(false, config);
    }

    /**
     * Sets all relevant settings to the value given by the {@code config}
     *
     * @param indeed if true, the config elements are updated indeed independent of their enable-state.
     * @param config Gives values for the (initial) settings
     */
    void setSettings(boolean indeed, SimulationConfig config);

    SimulationConfig getCorrectSettings() throws IncorrectSettingsException;

    /**
     * Makes the id-preference editable, if the given parameter {@code enabled} is true. If the id is not enabled
     * ({@link SimulationConfig.Element#isEditable()}), the given parameter is ignored and the id-preference is
     * not editable.
     *
     * @param element This id should be editable.
     * @param enabled Decides whether the given id-preference should be editable or not.
     * @return true if enabled, false otherwise
     */
    default boolean setEnabledIfEditable(SimulationConfig.Element element, boolean enabled) {
        return getModel().getEnableLexicon().setEnabledIfEditable(element, enabled);
    }

    default void setAllEnabled(boolean enabled) {
        for (SimulationConfig.Element element : SimulationConfig.Element.values())
            setEnabledIfEditable(element, enabled);
    }
}
