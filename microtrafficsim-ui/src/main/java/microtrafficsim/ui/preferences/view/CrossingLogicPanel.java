package microtrafficsim.ui.preferences.view;

import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.configs.SimulationConfig.Element;
import microtrafficsim.ui.preferences.IncorrectSettingsException;
import microtrafficsim.ui.preferences.model.CrossingLogicModel;

import javax.swing.*;
import java.awt.*;

/**
 * @author Dominic Parga Cacheiro
 */
public class CrossingLogicPanel extends PreferencesPanel {

    private final CrossingLogicModel model;
    private final JCheckBox cbEdgePriority;
    private final JComboBox<String> cbPriorityToThe;
    private final JCheckBox cbOnlyOneVehicle;
    private final JCheckBox cbFriendlyStandingInJam;

    public CrossingLogicPanel() {
        super();
        model = new CrossingLogicModel();

        cbEdgePriority          = new JCheckBox("edge priorities");
        cbPriorityToThe         = new JComboBox<>(model.getCombosPriorityToThe());
        cbOnlyOneVehicle        = new JCheckBox("only one vehicle crosses");
        cbFriendlyStandingInJam = new JCheckBox("friendly-standing-in-jam");

        create();
    }

    private void create() {

        setLayout(new GridBagLayout());
        Insets insets = new Insets(0, 0, 0, 10);


        /* row 0 column 0 */
        cbEdgePriority.setFont(PreferencesFrame.TEXT_FONT);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx  = 0;
        constraints.gridy  = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = insets;
        add(cbEdgePriority, constraints);


        // row 0 column 1
        cbOnlyOneVehicle.setFont(PreferencesFrame.TEXT_FONT);

        constraints = new GridBagConstraints();
        constraints.gridx  = 1;
        constraints.gridy  = 0;
        constraints.anchor = GridBagConstraints.WEST;
        add(cbOnlyOneVehicle, constraints);


        // row 0 column 2 - gap panel
        JPanel gap = new JPanel();

        constraints = new GridBagConstraints();
        constraints.gridx   = 2;
        constraints.gridy   = 0;
        constraints.fill    = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        add(gap, constraints);


        /* row 1 column 0 */
        cbPriorityToThe.setFont(PreferencesFrame.TEXT_FONT);

        constraints = new GridBagConstraints();
        constraints.gridx  = 0;
        constraints.gridy  = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = insets;
        add(cbPriorityToThe, constraints);


        // row 1 column 1
        cbFriendlyStandingInJam.setFont(PreferencesFrame.TEXT_FONT);

        constraints = new GridBagConstraints();
        constraints.gridx  = 1;
        constraints.gridy  = 1;
        constraints.anchor = GridBagConstraints.WEST;
        add(cbFriendlyStandingInJam, constraints);


        // row 1 column 2 - gap panel
        gap = new JPanel();

        constraints = new GridBagConstraints();
        constraints.gridx   = 2;
        constraints.gridy   = 1;
        constraints.fill    = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        add(gap, constraints);
    }


    @Override
    public CrossingLogicModel getModel() {
        return model;
    }

    @Override
    public void setSettings(boolean indeed, SimulationConfig config) {
        if (indeed) {
            cbEdgePriority.setSelected(config.crossingLogic.edgePriorityEnabled);
            cbPriorityToThe.setSelectedItem(model.getSelectedItem(config));
            cbOnlyOneVehicle.setSelected(config.crossingLogic.onlyOneVehicleEnabled);
            cbFriendlyStandingInJam.setSelected(config.crossingLogic.friendlyStandingInJamEnabled);
        } else {
            if (model.getEnableLexicon().isEnabled(Element.edgePriority))
                cbEdgePriority.setSelected(config.crossingLogic.edgePriorityEnabled);
            if (model.getEnableLexicon().isEnabled(Element.priorityToThe))
                cbPriorityToThe.setSelectedItem(model.getSelectedItem(config));
            if (model.getEnableLexicon().isEnabled(Element.onlyOneVehicle))
                cbOnlyOneVehicle.setSelected(config.crossingLogic.onlyOneVehicleEnabled);
            if (model.getEnableLexicon().isEnabled(Element.friendlyStandingInJam))
                cbFriendlyStandingInJam.setSelected(config.crossingLogic.friendlyStandingInJamEnabled);
        }
    }

    @Override
    public SimulationConfig getCorrectSettings() throws IncorrectSettingsException {
        SimulationConfig config = new SimulationConfig();

        config.crossingLogic.edgePriorityEnabled = cbEdgePriority.isSelected();
        config.crossingLogic.onlyOneVehicleEnabled = cbOnlyOneVehicle.isSelected();
        config.crossingLogic.friendlyStandingInJamEnabled = cbFriendlyStandingInJam.isSelected();

        model.updateConfig(config, cbPriorityToThe.getSelectedIndex());

        return config;
    }

    @Override
    public boolean setEnabledIfEditable(Element element, boolean enabled) {
        enabled = super.setEnabledIfEditable(element, enabled);

        switch (element) {
            case edgePriority:          cbEdgePriority.setEnabled(enabled);          break;
            case priorityToThe:         cbPriorityToThe.setEnabled(enabled);         break;
            case onlyOneVehicle:        cbOnlyOneVehicle.setEnabled(enabled);        break;
            case friendlyStandingInJam: cbFriendlyStandingInJam.setEnabled(enabled); break;
        }

        return enabled;
    }
}