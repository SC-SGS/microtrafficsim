package microtrafficsim.ui.preferences.view;

import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.ui.preferences.IncorrectSettingsException;
import microtrafficsim.ui.preferences.model.ConcurrencyModel;
import microtrafficsim.ui.preferences.model.PrefElement;

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
    }

    public void create() {

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
    public void setSettings(ScenarioConfig config) {
        tfNThreads.setText("" + config.multiThreading.nThreads);
        tfVehiclesPerRunnable.setText("" + config.multiThreading.vehiclesPerRunnable);
        tfNodesPerThread.setText("" + config.multiThreading.nodesPerThread);
    }

    @Override
    public ScenarioConfig getCorrectSettings() throws IncorrectSettingsException {
        ScenarioConfig             config           = new ScenarioConfig();
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
    public void setEnabled(PrefElement id, boolean enabled) {
        enabled = id.isEnabled() && enabled;

        switch (id) {
            case nThreads:            tfNThreads.setEnabled(enabled);            break;
            case vehiclesPerRunnable: tfVehiclesPerRunnable.setEnabled(enabled); break;
            case nodesPerThread:      tfNodesPerThread.setEnabled(enabled);      break;
        }
    }
}