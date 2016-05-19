package microtrafficsim.ui.preferences.impl;

import microtrafficsim.core.simulation.controller.configs.SimulationConfig;
import microtrafficsim.ui.preferences.ComponentId;
import microtrafficsim.ui.preferences.PrefPanel;

import javax.swing.*;

/**
 * @author Dominic Parga Cacheiro
 */
public class PanelVisualization extends PrefPanel {

    private JTextField tfProjection;

    public PanelVisualization(SimulationConfig config) {
        super(config, "Visualization");
    }

    /*
    |============|
    | components |
    |============|
    */
    private void addProjection() {
        tfProjection = new JTextField();
        configureAndAddJTextFieldRow("projection: ", tfProjection);
    }

    /*
    |==================|
    | (i) IPreferences |
    |==================|
    */
    @Override
    public void initSettings() {

        tfProjection.setText(config.visualization().projection().get().getClass().getSimpleName());
    }

    @Override
    public void create() {

        addProjection();
        setAllEnabled(false);
    }

    @Override
    public boolean acceptChanges(SimulationConfig newConfig) {
        return false;
    }

    @Override
    public void cancelChanges() {

    }

    /*
    |===============|
    | (c) PrefPanel |
    |===============|
    */
    @Override
    public void setAllEnabled(boolean enabled) {

        tfProjection.setEnabled(false);
    }

    @Override
    public void setEnabled(boolean enabled, ComponentId component) {
        switch (component) {
            case projection:
                tfProjection.setEnabled(enabled);
                break;
        }
    }
}
