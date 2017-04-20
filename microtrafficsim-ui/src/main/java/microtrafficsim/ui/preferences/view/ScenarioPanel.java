package microtrafficsim.ui.preferences.view;

import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.ui.preferences.IncorrectSettingsException;
import microtrafficsim.ui.preferences.model.PrefElement;
import microtrafficsim.ui.preferences.model.ScenarioModel;
import microtrafficsim.utils.Descriptor;

import javax.swing.*;
import java.awt.*;

/**
 * @author Dominic Parga Cacheiro
 */
public class ScenarioPanel extends PreferencesPanel {

    private final ScenarioModel model;
    private final JCheckBox cbShowAreasWhileSimulating;
    private final JCheckBox cbNodesAreWeightedUniformly;
    private final JComboBox<String> cbScenarioChoice;

    public ScenarioPanel() {
        super();
        model = new ScenarioModel();

        cbShowAreasWhileSimulating = new JCheckBox("show areas while simulating");
        cbNodesAreWeightedUniformly = new JCheckBox("nodes are weighted uniformly");
        cbScenarioChoice = new JComboBox<>();

        create();
    }

    private void create() {
        model.getScenarios().stream()
                .map(Descriptor::getDescription)
                .forEach(cbScenarioChoice::addItem);

        setLayout(new GridBagLayout());
        Insets insets = new Insets(0, 0, 0, 10);
        int row = 0;
        GridBagConstraints constraints;
        JPanel gap;

        /* row 0 column 0 */
        cbShowAreasWhileSimulating.setFont(PreferencesFrame.TEXT_FONT);

        constraints = new GridBagConstraints();
        constraints.gridx  = 0;
        constraints.gridy  = row;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = insets;
        add(cbShowAreasWhileSimulating, constraints);


        // row 0 column 1 - gap panel
        gap = new JPanel();

        constraints = new GridBagConstraints();
        constraints.gridx   = 1;
        constraints.gridy   = row;
        constraints.fill    = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        add(gap, constraints);


        row++;
        /* row 1 column 0 */
        cbNodesAreWeightedUniformly.setFont(PreferencesFrame.TEXT_FONT);

        constraints = new GridBagConstraints();
        constraints.gridx  = 0;
        constraints.gridy  = row;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = insets;
        add(cbNodesAreWeightedUniformly, constraints);


        // row 0 column 1 - gap panel
        gap = new JPanel();

        constraints = new GridBagConstraints();
        constraints.gridx   = 1;
        constraints.gridy   = row;
        constraints.fill    = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        add(gap, constraints);


        row++;
        /* row 2 column 0 */
        cbScenarioChoice.setFont(PreferencesFrame.TEXT_FONT);

        constraints = new GridBagConstraints();
        constraints.gridx  = 0;
        constraints.gridy  = row;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = insets;
        add(cbScenarioChoice, constraints);


        // row 2 column 1 - gap panel
        gap = new JPanel();

        constraints = new GridBagConstraints();
        constraints.gridx   = 1;
        constraints.gridy   = row;
        constraints.fill    = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        add(gap, constraints);
    }


    @Override
    public ScenarioModel getModel() {
        return model;
    }

    @Override
    public void setSettings(SimulationConfig config) {
        cbShowAreasWhileSimulating.setSelected(config.scenario.showAreasWhileSimulating);
        cbNodesAreWeightedUniformly.setSelected(config.scenario.nodesAreWeightedUniformly);

        /* update model */
        model.clearAllScenarios();
        config.scenario.supportedClasses.forEach(model::addScenario);
        /* update checkbox for scenario choice */
        cbScenarioChoice.removeAllItems();
        model.getScenarios().stream()
                .map(Descriptor::getDescription)
                .forEach(cbScenarioChoice::addItem);
        cbScenarioChoice.setSelectedItem(config.scenario.selectedClass);
    }

    @Override
    public SimulationConfig getCorrectSettings() throws IncorrectSettingsException {
        SimulationConfig config = new SimulationConfig();

        config.scenario.showAreasWhileSimulating = cbShowAreasWhileSimulating.isSelected();
        config.scenario.nodesAreWeightedUniformly = cbNodesAreWeightedUniformly.isSelected();

        model.getScenarios().forEach(config.scenario.supportedClasses::add);
        config.scenario.selectedClass = model.get(cbScenarioChoice.getSelectedIndex());

        return config;
    }

    @Override
    public void setEnabled(PrefElement id, boolean enabled) {
        enabled = id.isEnabled() && enabled;

        switch (id) {
            case showAreasWhileSimulating:  cbShowAreasWhileSimulating.setEnabled(enabled); break;
            case nodesAreWeightedUniformly: cbNodesAreWeightedUniformly.setEnabled(enabled);
            case scenarioSelection:         cbScenarioChoice.setEnabled(enabled);           break;
        }
    }
}