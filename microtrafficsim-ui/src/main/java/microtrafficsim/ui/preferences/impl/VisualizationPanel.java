package microtrafficsim.ui.preferences.impl;

import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.ui.preferences.IncorrectSettingsException;
import microtrafficsim.ui.preferences.PrefElement;


/**
 * @author Dominic Parga Cacheiro
 */
public class VisualizationPanel extends PreferencesPanel {

    public VisualizationPanel() {
        super("Visualization");
    }

    /*
    |============|
    | components |
    |============|
    */
    private void addProjection() {
    }

    /*
    |=================|
    | (i) Preferences |
    |=================|
    */
    @Override
    public void create() {
        addProjection();
        setAllEnabled(false);
    }

    @Override
    public void setSettings(ScenarioConfig config) {
    }

    @Override
    public ScenarioConfig getCorrectSettings() throws IncorrectSettingsException {
        return new ScenarioConfig();
    }

    @Override
    public void setEnabled(PrefElement id, boolean enabled) {
    }

    @Override
    public void setAllEnabled(boolean enabled) {
    }
}
