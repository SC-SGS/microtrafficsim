package microtrafficsim.ui.preferences.impl;

import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.ui.preferences.PrefElement;
import microtrafficsim.ui.preferences.IncorrectSettingsException;

import javax.swing.*;

/**
 * @author Dominic Parga Cacheiro
 */
public class VisualizationPanel extends PreferencesPanel {
  private JTextField tfProjection;

  public VisualizationPanel() {
    super("Visualization");
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
    tfProjection.setText(config.visualization.projection.getClass().getSimpleName());
  }

  @Override
  public SimulationConfig getCorrectSettings() throws IncorrectSettingsException {
    return new SimulationConfig();
  }

  @Override
  public void setEnabled(PrefElement id, boolean enabled) {
    switch (id) {
      case projection:
        tfProjection.setEnabled(enabled);
        break;
    }
  }

  @Override
  public void setAllEnabled(boolean enabled) {
    tfProjection.setEnabled(false);
  }
}
