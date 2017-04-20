package microtrafficsim.ui.preferences.view;

import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.ui.preferences.IncorrectSettingsException;
import microtrafficsim.ui.preferences.model.GeneralModel;
import microtrafficsim.ui.preferences.model.PrefElement;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;

/**
 * @author Dominic Parga Cacheiro
 */
public class GeneralPanel extends PreferencesPanel {

    private final GeneralModel model;
    private final JSlider sliderSpeedup;
    private final JTextField tfMaxVehicleCount;
    private final JTextField tfSeed;
    private final JTextField tfMetersPerCell;

    /**
     * You should call {@link #create()} before you use this frame.
     */
    public GeneralPanel() {
        super();
        model = new GeneralModel();

        sliderSpeedup     = new JSlider(0, 100);
        tfMaxVehicleCount = new JTextField();
        tfSeed            = new JTextField();
        tfMetersPerCell   = new JTextField();

        create();
    }

    private void create() {

        setLayout(new GridBagLayout());

        int row = 0;
        addSlider(row++);
        addTextFieldToGridBagLayout(row++, "max vehicle count: ", tfMaxVehicleCount);
        addTextFieldToGridBagLayout(row++, "seed: ", tfSeed);
        addTextFieldToGridBagLayout(row++, "meters per cell: ", tfMetersPerCell);
    }

    private void addSlider(int row) {
        GridBagConstraints constraints = new GridBagConstraints();

        /* speedup slider */
        constraints.gridx   = 0;
        constraints.gridy   = row;
        constraints.weightx = 0;
        constraints.anchor  = GridBagConstraints.WEST;
        JLabel label        = new JLabel("speedup: ");
        label.setFont(PreferencesFrame.TEXT_FONT);
        add(label, constraints);

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


        constraints           = new GridBagConstraints();
        constraints.gridx     = 1;
        constraints.gridy     = row;
        constraints.weightx   = 1;
        constraints.gridwidth = 2;
        constraints.anchor    = GridBagConstraints.WEST;
        constraints.fill      = GridBagConstraints.HORIZONTAL;
        sliderSpeedup.setMinimumSize(new Dimension(420, 1));
        add(sliderSpeedup, constraints);
    }


    @Override
    public GeneralModel getModel() {
        return model;
    }

    @Override
    public void setSettings(SimulationConfig config) {
        sliderSpeedup.setValue(config.speedup);
        tfMaxVehicleCount.setText("" + config.maxVehicleCount);
        tfSeed.setText("" + config.seed);
        tfMetersPerCell.setText("" + config.metersPerCell);
    }

    @Override
    public SimulationConfig getCorrectSettings() throws IncorrectSettingsException {
        SimulationConfig config              = new SimulationConfig();
        boolean exceptionOccured             = false;
        IncorrectSettingsException exception = new IncorrectSettingsException();


        config.speedup = sliderSpeedup.getValue();
        try {
            config.maxVehicleCount = Integer.parseInt(tfMaxVehicleCount.getText());
        } catch (NumberFormatException e) {
            exception.appendToMessage("\"Max vehicle count\" should be an integer.\n");
            exceptionOccured = true;
        }
        try {
            config.seed = Long.parseLong(tfSeed.getText());
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
        enabled = id.isEnabled() && enabled;

        switch (id) {
            case sliderSpeedup:   sliderSpeedup.setEnabled(enabled);     break;
            case maxVehicleCount: tfMaxVehicleCount.setEnabled(enabled); break;
            case seed:            tfSeed.setEnabled(enabled);            break;
            case metersPerCell:   tfMetersPerCell.setEnabled(enabled);   break;
        }
    }
}