package microtrafficsim.ui.preferences.view;

import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.configs.SimulationConfig.Element;
import microtrafficsim.ui.preferences.IncorrectSettingsException;
import microtrafficsim.ui.preferences.model.ConcurrencyModel;

import javax.swing.*;
import java.awt.*;

/**
 * @author Dominic Parga Cacheiro
 */
public class ConcurrencyPanel extends PreferencesPanel {

    private final ConcurrencyModel model;
    private final JTextField tfNThreads;
    private final JTextField tfVehiclesPerRunnable;
    private final JTextField tfNodesPerThread;

    /**
     * You should call {@link #create()} before you use this frame.
     */
    public ConcurrencyPanel() {
        super();
        model = new ConcurrencyModel();

        tfNThreads = new JTextField();
        tfVehiclesPerRunnable = new JTextField();
        tfNodesPerThread = new JTextField();

        create();
    }

    private void create() {

        setLayout(new GridBagLayout());


        int row = 0;
        addTextFieldToGridBagLayout(row++, "# threads:", tfNThreads);
        addTextFieldToGridBagLayout(row++, "# vehicles per runnable:", tfVehiclesPerRunnable);
        addTextFieldToGridBagLayout(row++, "# nodes per thread:", tfNodesPerThread);
    }


    @Override
    public ConcurrencyModel getModel() {
        return model;
    }

    @Override
    public void setSettings(boolean indeed, SimulationConfig config) {
        if (indeed) {
            tfNThreads.setText("" + config.multiThreading.nThreads);
            tfVehiclesPerRunnable.setText("" + config.multiThreading.vehiclesPerRunnable);
            tfNodesPerThread.setText("" + config.multiThreading.nodesPerThread);
        } else {
            if (model.getEnableLexicon().isEnabled(Element.nThreads))
                tfNThreads.setText("" + config.multiThreading.nThreads);
            if (model.getEnableLexicon().isEnabled(Element.vehiclesPerRunnable))
                tfVehiclesPerRunnable.setText("" + config.multiThreading.vehiclesPerRunnable);
            if (model.getEnableLexicon().isEnabled(Element.nodesPerThread))
                tfNodesPerThread.setText("" + config.multiThreading.nodesPerThread);
        }
    }

    @Override
    public SimulationConfig getCorrectSettings() throws IncorrectSettingsException {
        SimulationConfig config           = new SimulationConfig();
        boolean                    exceptionOccured = false;
        IncorrectSettingsException exception        = new IncorrectSettingsException();


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


        if (exceptionOccured) throw exception;


        return config;
    }

    @Override
    public boolean setEnabledIfEditable(Element element, boolean enabled) {
        enabled = super.setEnabledIfEditable(element, enabled);

        switch (element) {
            case nThreads:            tfNThreads.setEnabled(enabled);            break;
            case vehiclesPerRunnable: tfVehiclesPerRunnable.setEnabled(enabled); break;
            case nodesPerThread:      tfNodesPerThread.setEnabled(enabled);      break;
        }

        return enabled;
    }
}