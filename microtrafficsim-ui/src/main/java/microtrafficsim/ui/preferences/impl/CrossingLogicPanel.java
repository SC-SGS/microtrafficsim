package microtrafficsim.ui.preferences.impl;

import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.ui.preferences.IncorrectSettingsException;
import microtrafficsim.ui.preferences.PrefElement;

import javax.swing.*;
import java.awt.*;

/**
 * @author Dominic Parga Cacheiro
 */
public class CrossingLogicPanel extends PreferencesPanel {
    private final JCheckBox cbEdgePriority;
    private final JComboBox<String> cbPriorityToThe;
    private final String[] combos = new String[] {"priority to the left", "priority to the right", "priority to the random"};
    private final JCheckBox cbOnlyOneVehicle;
    private final JCheckBox cbFriendlyStandingInJam;

    public CrossingLogicPanel() {
        super("Crossing logic");
        cbEdgePriority          = new JCheckBox("Edge priorities");
        cbPriorityToThe         = new JComboBox<>(combos);
        cbOnlyOneVehicle        = new JCheckBox("only one vehicle crosses");
        cbFriendlyStandingInJam = new JCheckBox("friendly-standing-in-jam");
    }

    /*
    |============|
    | components |
    |============|
    */
    private void addEdgePriorityEnabled() {

        // row 0 column 0
        incRow();
        incColumn();

        cbEdgePriority.setFont(PreferencesFrame.TEXT_FONT);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor             = GridBagConstraints.WEST;
        add(cbEdgePriority, constraints);
    }

    private void addPriorityToThe() {

        // row 1 column 0
        incRow();
        incColumn();

        cbPriorityToThe.setFont(PreferencesFrame.TEXT_FONT);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor             = GridBagConstraints.WEST;
        add(cbPriorityToThe, constraints);
    }

    private void addOnlyOneVehicleEnabled() {

        // row 0 column 2
        incRow();
        incColumn();
        incColumn();
        incColumn();

        cbOnlyOneVehicle.setFont(PreferencesFrame.TEXT_FONT);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor             = GridBagConstraints.WEST;
        add(cbOnlyOneVehicle, constraints);
    }

    private void addGoWithoutPriorityEnabled() {

        // row 1 column 2
        incRow();
        incColumn();
        incColumn();
        incColumn();

        cbFriendlyStandingInJam.setFont(PreferencesFrame.TEXT_FONT);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor             = GridBagConstraints.WEST;
        add(cbFriendlyStandingInJam, constraints);
    }

    /*
    |=================|
    | (i) Preferences |
    |=================|
    */
    @Override
    public void create() {
        //        /* JLabel */
        //        incRow();
        //        GridBagConstraints constraints = new GridBagConstraints();
        //        constraints.weightx = 0;
        //        constraints.anchor = GridBagConstraints.WEST;
        //        JLabel label = new JLabel("De-/Activating traffic rules");
        //        label.setFont(PreferencesFrame.TEXT_FONT);
        //        incColumn();
        //        add(label, constraints);
        //        /* gap */
        //        incColumn();
        //        addGapPanel(1);

        /* components */
        addEdgePriorityEnabled();         // row 0 column 0
        addPriorityToThe();               // row 1 column 0
        incColumn();
        decRow();
        decRow();
        addOnlyOneVehicleEnabled();       // row 0 column 1
        incColumn();
        addGapPanel(1);                   // row 0 column 2+
        addGoWithoutPriorityEnabled();    // row 1 column 1
        incColumn();
        addGapPanel(1);                   // row 1 column 2+

        setAllEnabled(false);
    }

    @Override
    public void setSettings(SimulationConfig config) {
        cbEdgePriority.setSelected(config.crossingLogic.edgePriorityEnabled);

        String selectedItem;
        if (config.crossingLogic.priorityToTheRightEnabled)
            selectedItem = combos[config.crossingLogic.drivingOnTheRight ? 1 : 0];
        else
            selectedItem = combos[2];

        cbPriorityToThe.setSelectedItem(selectedItem);
        cbOnlyOneVehicle.setSelected(config.crossingLogic.isOnlyOneVehicleEnabled());
        cbFriendlyStandingInJam.setSelected(config.crossingLogic.friendlyStandingInJamEnabled);
    }

    @Override
    public SimulationConfig getCorrectSettings() throws IncorrectSettingsException {
        SimulationConfig config = new SimulationConfig();

        config.crossingLogic.edgePriorityEnabled = cbEdgePriority.isSelected();
        config.crossingLogic.setOnlyOneVehicle(cbOnlyOneVehicle.isSelected());
        config.crossingLogic.friendlyStandingInJamEnabled = cbFriendlyStandingInJam.isSelected();

        switch (cbPriorityToThe.getSelectedIndex()) {
        case 0:    // combos[0] = "priority to the left"
            config.crossingLogic.priorityToTheRightEnabled = true;
            config.crossingLogic.drivingOnTheRight         = false;
            break;

        case 1:    // combos[1] = "priority to the right"
            config.crossingLogic.priorityToTheRightEnabled = true;
            config.crossingLogic.drivingOnTheRight         = true;
            break;

        case 2:    // combos[2] = "priority to the random"
            config.crossingLogic.priorityToTheRightEnabled = false;
            break;
        }

        return config;
    }

    @Override
    public void setEnabled(PrefElement id, boolean enabled) {
        switch (id) {
        case edgePriority:          cbEdgePriority.setEnabled(enabled);          break;
        case priorityToThe:         cbPriorityToThe.setEnabled(enabled);         break;
        case onlyOneVehicle:        cbOnlyOneVehicle.setEnabled(enabled);        break;
        case friendlyStandingInJam: cbFriendlyStandingInJam.setEnabled(enabled); break;
        }
    }

    @Override
    public void setAllEnabled(boolean enabled) {
        cbEdgePriority.setEnabled(enabled);
        cbPriorityToThe.setEnabled(enabled);
        cbOnlyOneVehicle.setEnabled(enabled);
        cbFriendlyStandingInJam.setEnabled(enabled);
    }
}
