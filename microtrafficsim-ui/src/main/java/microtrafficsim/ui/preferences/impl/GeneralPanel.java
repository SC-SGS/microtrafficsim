package microtrafficsim.ui.preferences.impl;

import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.utils.id.ConcurrentSeedGenerator;
import microtrafficsim.ui.preferences.IncorrectSettingsException;
import microtrafficsim.ui.preferences.PrefElement;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;


/**
 * @author Dominic Parga Cacheiro
 */
public class GeneralPanel extends PreferencesPanel {
    private JSlider    sliderSpeedup;
    private JTextField tfAgeForPause, tfMaxVehicleCount, tfSeed, tfMetersPerCell;

    public GeneralPanel() {
        super("General");
    }

    /*
    |============|
    | components |
    |============|
    */
    private void addSpeedup() {
        incRow();

        /* components */
        sliderSpeedup = new JSlider(0, 100);

        /* JLabel */
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx            = 0;
        constraints.anchor             = GridBagConstraints.WEST;
        JLabel label                   = new JLabel("speedup: ");
        label.setFont(PreferencesFrame.TEXT_FONT);
        incColumn();
        add(label, constraints);

        /* JSlider */
        sliderSpeedup.setMajorTickSpacing(5);
        sliderSpeedup.setMinorTickSpacing(1);
        // set labels for slider
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        for (int i = sliderSpeedup.getMinimum(); i <= sliderSpeedup.getMaximum(); i++)
            if (i % 10 == 0) labelTable.put(i, new JLabel("" + i));
        sliderSpeedup.setLabelTable(labelTable);
        sliderSpeedup.setPaintTicks(true);
        sliderSpeedup.setSnapToTicks(true);
        sliderSpeedup.setPaintLabels(true);
        sliderSpeedup.setPreferredSize(
                new Dimension(2 * sliderSpeedup.getPreferredSize().width, sliderSpeedup.getPreferredSize().height));

        constraints           = new GridBagConstraints();
        constraints.fill      = GridBagConstraints.HORIZONTAL;
        constraints.weightx   = 1;
        constraints.gridwidth = 2;
        constraints.anchor    = GridBagConstraints.WEST;
        incColumn();
        add(sliderSpeedup, constraints);
    }

    private void addAgeForPause() {
        tfAgeForPause = new JTextField();
        configureAndAddJTextFieldRow("age for pause: ", tfAgeForPause);
    }

    private void addMaxVehicleCount() {
        tfMaxVehicleCount = new JTextField();
        configureAndAddJTextFieldRow("max vehicle count: ", tfMaxVehicleCount);
    }

    private void addSeed() {
        tfSeed = new JTextField();
        configureAndAddJTextFieldRow("seed: ", tfSeed);
    }

    private void addMetersPerCell() {
        tfMetersPerCell = new JTextField();
        configureAndAddJTextFieldRow("meters per cell", tfMetersPerCell);
    }

    /*
    |=================|
    | (i) Preferences |
    |=================|
    */
    @Override
    public void create() {
        addSpeedup();
        addAgeForPause();
        addMaxVehicleCount();
        addSeed();
        addMetersPerCell();
        setAllEnabled(false);
    }

    @Override
    public void setSettings(SimulationConfig config) {
        sliderSpeedup.setValue(config.speedup);
        tfAgeForPause.setText("" + config.ageForPause);
        tfMaxVehicleCount.setText("" + config.maxVehicleCount);
        tfSeed.setText("" + config.seed);
        tfMetersPerCell.setText("" + config.metersPerCell);
    }

    @Override
    public SimulationConfig getCorrectSettings() throws IncorrectSettingsException {
        SimulationConfig           config           = new SimulationConfig();
        boolean                    exceptionOccured = false;
        IncorrectSettingsException exception        = new IncorrectSettingsException();


        config.speedup = sliderSpeedup.getValue();
        try {
            config.ageForPause = Integer.parseInt(tfAgeForPause.getText());
        } catch (NumberFormatException e) {
            exception.appendToMessage("\"Age for pause\" should be an integer.\n");
            exceptionOccured = true;
        }
        try {
            config.maxVehicleCount = Integer.parseInt(tfMaxVehicleCount.getText());
        } catch (NumberFormatException e) {
            exception.appendToMessage("\"Max vehicle count\" should be an integer.\n");
            exceptionOccured = true;
        }
        try {
            config.seed = Long.parseLong(tfSeed.getText());
            config.rndGenGenerator = new ConcurrentSeedGenerator(config.seed);
        } catch (NumberFormatException e) {
            exception.appendToMessage("\"Seed\" should be a long.\n");
            exceptionOccured = true;
        }
        try {
            config.metersPerCell = Float.parseFloat(tfMetersPerCell.getText());
        } catch (NumberFormatException e) {
            exception.appendToMessage("\"Meters per cell\" should be a float.\n");
            exceptionOccured = true;
        }


        if (exceptionOccured) throw exception;


        return config;
    }

    @Override
    public void setEnabled(PrefElement id, boolean enabled) {
        switch (id) {
        case sliderSpeedup:   sliderSpeedup.setEnabled(enabled);     break;
        case ageForPause:     tfAgeForPause.setEnabled(enabled);     break;
        case maxVehicleCount: tfMaxVehicleCount.setEnabled(enabled); break;
        case seed:            tfSeed.setEnabled(enabled);            break;
        case metersPerCell:   tfMetersPerCell.setEnabled(enabled);   break;
        }
    }

    @Override
    public void setAllEnabled(boolean enabled) {
        sliderSpeedup.setEnabled(enabled);
        tfAgeForPause.setEnabled(enabled);
        tfMaxVehicleCount.setEnabled(enabled);
        tfSeed.setEnabled(enabled);
        tfMetersPerCell.setEnabled(enabled);
    }
}
