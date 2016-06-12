package microtrafficsim.ui.preferences.impl;

import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.ui.preferences.PrefElement;
import microtrafficsim.ui.preferences.IncorrectSettingsException;

import javax.swing.*;

/**
 * @author Dominic Parga Cacheiro
 */
public class ConcurrencyPanel extends PreferencesPanel {
  private JTextField tfNThreads;
  private JTextField tfVehiclesPerRunnable;
  private JTextField tfNodesPerThread;

  public ConcurrencyPanel() {
    super("Concurrency");
  }

  /*
  |============|
  | components |
  |============|
  */
  private void addNThreads() {
    tfNThreads = new JTextField();
    configureAndAddJTextFieldRow("# threads: ", tfNThreads);
  }

  private void addVehiclesPerRunnable() {
    tfVehiclesPerRunnable = new JTextField();
    configureAndAddJTextFieldRow("# vehicles per runnable: ", tfVehiclesPerRunnable);
  }

  private void addNodesPerThread() {
    tfNodesPerThread = new JTextField();
    configureAndAddJTextFieldRow("# nodes per thread: ", tfNodesPerThread);
  }

  /*
  |=================|
  | (i) Preferences |
  |=================|
  */
  @Override
  public void create() {
    addNThreads();
    addVehiclesPerRunnable();
    addNodesPerThread();

    setAllEnabled(false);
  }

  @Override
  public void setSettings(SimulationConfig config) {
    tfNThreads.setText("" + config.multiThreading.nThreads);
    tfVehiclesPerRunnable.setText("" + config.multiThreading.vehiclesPerRunnable);
    tfNodesPerThread.setText("" + config.multiThreading.nodesPerThread);
  }

  @Override
  public SimulationConfig getCorrectSettings() throws IncorrectSettingsException {
    SimulationConfig config = new SimulationConfig();
    boolean exceptionOccured = false;
    IncorrectSettingsException exception = new IncorrectSettingsException();


    try {
      config.multiThreading.nThreads = Integer.parseInt(tfNThreads.getText());
    } catch (NumberFormatException e) {
      exception.appendToMessage("\"# threads\" should be an integer.\n");
      exceptionOccured = true;
    }
    try {
      config.multiThreading.vehiclesPerRunnable = Integer.parseInt(tfVehiclesPerRunnable.getText());
    } catch (NumberFormatException e) {
      exception.appendToMessage("\"# vehicles per runnable\" should be an integer.\n");
      exceptionOccured = true;
    }
    try {
      config.multiThreading.nodesPerThread = Integer.parseInt(tfNodesPerThread.getText());
    } catch (NumberFormatException e) {
      exception.appendToMessage("\"# nodes per thread\" should be an integer.\n");
      exceptionOccured = true;
    }


    if (exceptionOccured)
      throw exception;


    return config;
  }

  @Override
  public void setEnabled(PrefElement id, boolean enabled) {
    switch (id) {
      case nThreads:
        tfNThreads.setEnabled(enabled);
        break;
      case vehiclesPerRunnable:
        tfVehiclesPerRunnable.setEnabled(enabled);
        break;
      case nodesPerThread:
        tfNodesPerThread.setEnabled(enabled);
        break;
    }
  }

  @Override
  public void setAllEnabled(boolean enabled) {
    tfNThreads.setEnabled(enabled);
    tfVehiclesPerRunnable.setEnabled(enabled);
    tfNodesPerThread.setEnabled(enabled);
  }
}
