package microtrafficsim.ui.preferences.impl;

import microtrafficsim.core.simulation.controller.configs.SimulationConfig;
import microtrafficsim.ui.preferences.ComponentId;
import microtrafficsim.ui.preferences.PrefPanel;

import javax.swing.*;
import java.awt.*;

/**
 * @author Dominic Parga Cacheiro
 */
public class PanelCrossingLogic extends PrefPanel {

    private JCheckBox cbDrivingOnTheRight;
    private JCheckBox cbEdgePriorityEnabled;
    private JCheckBox cbPriorityToTheRightEnabled;
    private JCheckBox cbOnlyOneVehicleEnabled;
    private JCheckBox cbGoWithoutPriorityEnabled;

    public PanelCrossingLogic(SimulationConfig config) {
        super(config, "Crossing logic");
    }

    /*
    |============|
    | components |
    |============|
    */
    private void addDrivingOnTheRight() {

        // row 0 column 0
        incRow();
        incColumn();

        cbDrivingOnTheRight = new JCheckBox("Driving on the right");
        cbDrivingOnTheRight.setFont(PrefFrame.TEXT_FONT);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        add(cbDrivingOnTheRight, constraints);
    }

    private void addEdgePriorityEnabled() {

        // row 1 column 0
        incRow();
        incColumn();

        cbEdgePriorityEnabled = new JCheckBox("Edge priorities");
        cbEdgePriorityEnabled.setFont(PrefFrame.TEXT_FONT);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        add(cbEdgePriorityEnabled, constraints);
    }

    private void addPriorityToTheRightEnabled() {

        // row 2 column 0
        incRow();
        incColumn();

        cbPriorityToTheRightEnabled = new JCheckBox("priority to the left/right");
        cbPriorityToTheRightEnabled.setFont(PrefFrame.TEXT_FONT);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        add(cbPriorityToTheRightEnabled, constraints);
    }

    private void addOnlyOneVehicleEnabled() {

        // row 0 column 1
        incRow();
        incColumn();
        incColumn();

        cbOnlyOneVehicleEnabled = new JCheckBox("only one vehicle crosses");
        cbOnlyOneVehicleEnabled.setFont(PrefFrame.TEXT_FONT);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        add(cbOnlyOneVehicleEnabled, constraints);
    }

    private void addGoWithoutPriorityEnabled() {

        // row 1 column 1
        incRow();
        incColumn();
        incColumn();

        cbGoWithoutPriorityEnabled = new JCheckBox("crossing without priority");
        cbGoWithoutPriorityEnabled.setFont(PrefFrame.TEXT_FONT);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        add(cbGoWithoutPriorityEnabled, constraints);
    }

    /*
    |==================|
    | (i) IPreferences |
    |==================|
    */
    @Override
    public void initSettings() {

        cbDrivingOnTheRight.setSelected(config.crossingLogic().drivingOnTheRight().get());
        cbEdgePriorityEnabled.setSelected(config.crossingLogic().edgePriorityEnabled);
        cbPriorityToTheRightEnabled.setSelected(config.crossingLogic().priorityToTheRightEnabled);
        cbOnlyOneVehicleEnabled.setSelected(config.crossingLogic().isOnlyOneVehicleEnabled());
        cbGoWithoutPriorityEnabled.setSelected(config.crossingLogic().goWithoutPriorityEnabled);
    }

    @Override
    public void create() {

//        /* JLabel */
//        incRow();
//        GridBagConstraints constraints = new GridBagConstraints();
//        constraints.weightx = 0;
//        constraints.anchor = GridBagConstraints.WEST;
//        JLabel label = new JLabel("De-/Activating traffic rules");
//        label.setFont(PrefFrame.TEXT_FONT);
//        incColumn();
//        add(label, constraints);
//        /* gap */
//        incColumn();
//        addGapPanel(1);

        /* components */
        addDrivingOnTheRight(); // row 0 column 0
        addEdgePriorityEnabled(); // row 1 column 0
        addPriorityToTheRightEnabled(); // row 2 column 0
        incColumn();
        addGapPanel(1); // row 2 column 1+
        decRow();
        decRow();
        decRow();
        addOnlyOneVehicleEnabled(); // row 0 column 1
        incColumn();
        addGapPanel(1); // row 0 column 2+
        addGoWithoutPriorityEnabled(); // row 1 column 1
        incColumn();
        addGapPanel(1); // row 1 column 2+

        setAllEnabled(false);
    }

    @Override
    public boolean acceptChanges(SimulationConfig newConfig) {

        newConfig.crossingLogic().drivingOnTheRight().set(cbDrivingOnTheRight.isSelected());
        newConfig.crossingLogic().edgePriorityEnabled = cbEdgePriorityEnabled.isSelected();
        newConfig.crossingLogic().priorityToTheRightEnabled = cbPriorityToTheRightEnabled.isSelected();
        newConfig.crossingLogic().setOnlyOneVehicle(cbOnlyOneVehicleEnabled.isSelected());
        newConfig.crossingLogic().goWithoutPriorityEnabled = cbGoWithoutPriorityEnabled.isSelected();

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

        cbDrivingOnTheRight.setEnabled(enabled);
        cbEdgePriorityEnabled.setEnabled(enabled);
        cbPriorityToTheRightEnabled.setEnabled(enabled);
        cbOnlyOneVehicleEnabled.setEnabled(enabled);
        cbGoWithoutPriorityEnabled.setEnabled(enabled);
    }

    @Override
    public void setEnabled(boolean enabled, ComponentId componentId) {
        switch (componentId) {
            case drivingOnTheRight:
                cbDrivingOnTheRight.setEnabled(enabled);
                break;
            case edgePriorityEnabled:
                cbEdgePriorityEnabled.setEnabled(enabled);
                break;
            case priorityToTheRightEnabled:
                cbPriorityToTheRightEnabled.setEnabled(enabled);
                break;
            case onlyOneVehicleEnabled:
                cbOnlyOneVehicleEnabled.setEnabled(enabled);
                break;
            case goWithoutPriorityEnabled:
                cbGoWithoutPriorityEnabled.setEnabled(enabled);
                break;
        }
    }
}
