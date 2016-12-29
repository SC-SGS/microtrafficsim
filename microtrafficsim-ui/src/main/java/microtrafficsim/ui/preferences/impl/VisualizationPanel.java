package microtrafficsim.ui.preferences.impl;

import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.ui.preferences.IncorrectSettingsException;
import microtrafficsim.ui.preferences.PrefElement;

import javax.swing.*;


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
    public void setSettings(SimulationConfig config) {
    }

    @Override
    public SimulationConfig getCorrectSettings() throws IncorrectSettingsException {
        return new SimulationConfig();
    }

    @Override
    public void setEnabled(PrefElement id, boolean enabled) {
    }

    @Override
    public void setAllEnabled(boolean enabled) {
    }
}
