package microtrafficsim.ui.preferences.view;

import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.ui.preferences.IncorrectSettingsException;
import microtrafficsim.ui.preferences.model.PrefElement;
import microtrafficsim.ui.preferences.model.VisualizationModel;

import javax.swing.*;
import java.awt.*;

/**
 * @author Dominic Parga Cacheiro
 */
public class VisualizationPanel extends PreferencesPanel {

    private final VisualizationModel model;
    private final JComboBox<String> cbStyle;

    /**
     * You should call {@link #create()} before you use this frame.
     */
    public VisualizationPanel() {
        super();
        model = new VisualizationModel();
        cbStyle = new JComboBox<>();
    }


    public void create() {

        model.getStyleSheets().stream()
                .map(Class::getSimpleName)
                .forEach(cbStyle::addItem);

        setLayout(new GridBagLayout());
        cbStyle.setFont(PreferencesFrame.TEXT_FONT);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx              = 0;
        constraints.gridy              = 0;
        constraints.weightx            = 0;
        constraints.anchor             = GridBagConstraints.WEST;
        add(cbStyle, constraints);


        /* gap panel */
        JPanel gap = new JPanel(null);
        constraints           = new GridBagConstraints();
        constraints.gridx     = 1;
        constraints.gridy     = 0;
        constraints.weightx   = 1;
        constraints.gridwidth = 1;
        constraints.fill      = GridBagConstraints.HORIZONTAL;
        constraints.anchor    = GridBagConstraints.WEST;
        add(gap, constraints);
    }


    @Override
    public VisualizationModel getModel() {
        return model;
    }

    @Override
    public void setSettings(ScenarioConfig config) {
        cbStyle.setSelectedItem(config.visualization.style.getClass().getSimpleName());
    }

    @Override
    public ScenarioConfig getCorrectSettings() throws IncorrectSettingsException {

        ScenarioConfig config = new ScenarioConfig();
        config.visualization.style = model.instantiate(cbStyle.getSelectedIndex());
        return config;
    }

    @Override
    public void setEnabled(PrefElement id, boolean enabled) {
        enabled = id.isEnabled() && enabled;

        switch (id) {
            case style: cbStyle.setEnabled(enabled); break;
        }
    }
}