package microtrafficsim.ui.preferences.view;

import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.ui.preferences.IncorrectSettingsException;
import microtrafficsim.ui.preferences.model.CrossingLogicModel;
import microtrafficsim.ui.preferences.model.PrefElement;

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

    /**
     * You should call {@link #create()} before you use this frame.
     */
    public CrossingLogicPanel() {
        super();
        model = new CrossingLogicModel();

        cbEdgePriority          = new JCheckBox("Edge priorities");
        cbPriorityToThe         = new JComboBox<>(model.getCombosPriorityToThe());
        cbOnlyOneVehicle        = new JCheckBox("only one vehicle crosses");
        cbFriendlyStandingInJam = new JCheckBox("friendly-standing-in-jam");
    }

    public void create() {

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
    public void setSettings(ScenarioConfig config) {
        cbEdgePriority.setSelected(config.crossingLogic.edgePriorityEnabled);
        cbPriorityToThe.setSelectedItem(model.getSelectedItem(config));
        cbOnlyOneVehicle.setSelected(config.crossingLogic.onlyOneVehicleEnabled);
        cbFriendlyStandingInJam.setSelected(config.crossingLogic.friendlyStandingInJamEnabled);
    }

    @Override
    public ScenarioConfig getCorrectSettings() throws IncorrectSettingsException {
        ScenarioConfig config = new ScenarioConfig();

        config.crossingLogic.edgePriorityEnabled = cbEdgePriority.isSelected();
        config.crossingLogic.onlyOneVehicleEnabled = cbOnlyOneVehicle.isSelected();
        config.crossingLogic.friendlyStandingInJamEnabled = cbFriendlyStandingInJam.isSelected();

        model.updateConfig(config, cbPriorityToThe.getSelectedIndex());

        return config;
    }

    @Override
    public void setEnabled(PrefElement id, boolean enabled) {
        enabled = id.isEnabled() && enabled;

        switch (id) {
            case edgePriority:          cbEdgePriority.setEnabled(enabled);          break;
            case priorityToThe:         cbPriorityToThe.setEnabled(enabled);         break;
            case onlyOneVehicle:        cbOnlyOneVehicle.setEnabled(enabled);        break;
            case friendlyStandingInJam: cbFriendlyStandingInJam.setEnabled(enabled); break;
        }
    }
}