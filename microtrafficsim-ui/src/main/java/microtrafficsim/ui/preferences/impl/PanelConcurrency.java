package microtrafficsim.ui.preferences.impl;

import microtrafficsim.core.simulation.controller.configs.SimulationConfig;
import microtrafficsim.ui.preferences.ComponentId;
import microtrafficsim.ui.preferences.PrefPanel;

import javax.swing.*;

/**
 * @author Dominic Parga Cacheiro
 */
public class PanelConcurrency extends PrefPanel {

    private JTextField tfNThreads;
    private JTextField tfVehiclesPerRunnable;
    private JTextField tfNodesPerThread;

    public PanelConcurrency(SimulationConfig config) {
        super(config, "Concurrency");
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
    |==================|
    | (i) IPreferences |
    |==================|
    */

    @Override
    public void initSettings() {

        tfNThreads.setText("" + config.multiThreading().nThreads().get());
        tfVehiclesPerRunnable.setText("" + config.multiThreading().vehiclesPerRunnable().get());
        tfNodesPerThread.setText("" + config.multiThreading().nodesPerThread().get());
    }

    @Override
    public void create() {

        addNThreads();
        addVehiclesPerRunnable();
        addNodesPerThread();

        setAllEnabled(false);
    }

    @Override
    public boolean acceptChanges(SimulationConfig newConfig) {

        try {
            newConfig.multiThreading().nThreads().set(Integer.parseInt(tfNThreads.getText()));
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(null,
                    "\"# threads\" should be an integer.",
                    "Error: # threads",
                    JOptionPane.ERROR_MESSAGE);
            return true;
        }
        try {
            newConfig.multiThreading().vehiclesPerRunnable().set(Integer.parseInt(tfVehiclesPerRunnable.getText()));
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(null,
                    "\"# vehicles per runnable\" should be an integer.",
                    "Error: # vehicles per runnable",
                    JOptionPane.ERROR_MESSAGE);
            return true;
        }
        try {
            newConfig.multiThreading().nodesPerThread().set(Integer.parseInt(tfNodesPerThread.getText()));
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(null,
                    "\"# nodes per thread\" should be an integer.",
                    "Error: # nodes per thread",
                    JOptionPane.ERROR_MESSAGE);
            return true;
        }

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

        tfNThreads.setEnabled(enabled);
        tfVehiclesPerRunnable.setEnabled(enabled);
        tfNodesPerThread.setEnabled(enabled);
    }

    @Override
    public void setEnabled(boolean enabled, ComponentId component) {
        switch (component) {
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
}
