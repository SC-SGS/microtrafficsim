package microtrafficsim.ui.preferences.impl;

import microtrafficsim.core.simulation.controller.configs.SimulationConfig;
import microtrafficsim.ui.preferences.ComponentId;
import microtrafficsim.ui.preferences.PrefPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;

/**
 * @author Dominic Parga Cacheiro
 */
public class PanelGeneral extends PrefPanel {

    private JSlider sliderSpeedup;
    private JTextField tfAgeForPause, tfMaxVehicleCount, tfSeed, tfMetersPerCell;

    public PanelGeneral(SimulationConfig config) {
        super(config, "General");
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
        constraints.weightx = 0;
        constraints.anchor = GridBagConstraints.WEST;
        JLabel label = new JLabel("speedup: ");
        label.setFont(PrefFrame.TEXT_FONT);
        incColumn();
        add(label, constraints);

        /* JSlider */
        sliderSpeedup.setMajorTickSpacing(5);
        sliderSpeedup.setMinorTickSpacing(1);
        // set labels for slider
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        for (int i = sliderSpeedup.getMinimum(); i <= sliderSpeedup.getMaximum(); i++)
            if (i % 10 == 0)
                labelTable.put(i, new JLabel("" + i));
        sliderSpeedup.setLabelTable(labelTable);
        sliderSpeedup.setPaintTicks(true);
        sliderSpeedup.setSnapToTicks(true);
        sliderSpeedup.setPaintLabels(true);
        sliderSpeedup.setPreferredSize(new Dimension(
                2 * sliderSpeedup.getPreferredSize().width,
                sliderSpeedup.getPreferredSize().height));

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.WEST;
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
    |==================|
    | (i) IPreferences |
    |==================|
    */
    @Override
    public void initSettings() {
        sliderSpeedup.setValue(config.speedup().get());
        tfAgeForPause.setText("" + config.ageForPause);
        tfMaxVehicleCount.setText("" + config.maxVehicleCount);
        tfSeed.setText("" + config.seed().get());
        tfMetersPerCell.setText("" + config.metersPerCell().get());
    }

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
    public boolean acceptChanges(SimulationConfig newConfig) {
        newConfig.speedup().set(sliderSpeedup.getValue());
        try {
            newConfig.ageForPause = Integer.parseInt(tfAgeForPause.getText());
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(null,
                    "\"Age for pause\" should be an integer.",
                    "Error: age for pause",
                    JOptionPane.ERROR_MESSAGE);
            return true;
        }
        try {
            newConfig.maxVehicleCount = Integer.parseInt(tfMaxVehicleCount.getText());
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(null,
                    "\"Max vehicle count\" should be an integer.",
                    "Error: max vehicle count",
                    JOptionPane.ERROR_MESSAGE);
            return true;
        }
        try {
            newConfig.seed().set(Long.parseLong(tfSeed.getText()));
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(null,
                    "\"Seed\" should be a long.",
                    "Error: seed",
                    JOptionPane.ERROR_MESSAGE);
            return true;
        }
        try {
            newConfig.metersPerCell().set(Float.parseFloat(tfMetersPerCell.getText()));
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(null,
                    "\"Meters per cell\" should be a float.",
                    "Error: age for pause",
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
        sliderSpeedup.setEnabled(enabled);
        tfAgeForPause.setEnabled(enabled);
        tfMaxVehicleCount.setEnabled(enabled);
        tfSeed.setEnabled(enabled);
        tfMetersPerCell.setEnabled(enabled);
    }

    @Override
    public void setEnabled(boolean enabled, ComponentId componentId) {
        switch (componentId) {
            case sliderSpeedup:
                sliderSpeedup.setEnabled(enabled);
                break;
            case ageForPause:
                tfAgeForPause.setEnabled(enabled);
                break;
            case maxVehicleCount:
                tfMaxVehicleCount.setEnabled(enabled);
                break;
            case seed:
                tfSeed.setEnabled(enabled);
                break;
            case metersPerCell:
                tfMetersPerCell.setEnabled(enabled);
                break;
        }
    }
}
