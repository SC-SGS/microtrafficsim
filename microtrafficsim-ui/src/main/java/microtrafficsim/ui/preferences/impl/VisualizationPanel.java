package microtrafficsim.ui.preferences.impl;

import microtrafficsim.core.map.style.StyleSheet;
import microtrafficsim.core.map.style.impl.DarkStyleSheet;
import microtrafficsim.core.map.style.impl.LightStyleSheet;
import microtrafficsim.core.map.style.impl.MonochromeStyleSheet;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.ui.preferences.IncorrectSettingsException;
import microtrafficsim.ui.preferences.PrefElement;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.function.Supplier;


/**
 * @author Dominic Parga Cacheiro
 */
public class VisualizationPanel extends PreferencesPanel {

    private final JComboBox<String> cbStyle;
    private final ArrayList<Supplier<StyleSheet>> suppliers;

    public VisualizationPanel() {
        super("Visualization");

        suppliers = new ArrayList<>();
        cbStyle = new JComboBox<>();

        addStyleSheet(DarkStyleSheet.class);
        addStyleSheet(LightStyleSheet.class);
        addStyleSheet(MonochromeStyleSheet.class);
    }

    public void addStyleSheet(Class<? extends StyleSheet> clazz) {
        cbStyle.addItem(clazz.getSimpleName());
        suppliers.add(() -> {
            try {
                return clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    /*
    |============|
    | components |
    |============|
    */
    private void addStyleComboBox() {

        // row 0 column 0
        incRow();
        incColumn();

        cbStyle.setFont(PreferencesFrame.TEXT_FONT);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor             = GridBagConstraints.WEST;
        add(cbStyle, constraints);
        addGapPanel();
    }

    /*
    |=================|
    | (i) Preferences |
    |=================|
    */
    @Override
    public void create() {
        addStyleComboBox();
        setAllEnabled(false);
    }

    @Override
    public void setSettings(ScenarioConfig config) {
        cbStyle.setSelectedItem(config.visualization.style.getClass().getSimpleName());
    }

    @Override
    public ScenarioConfig getCorrectSettings() throws IncorrectSettingsException {

        ScenarioConfig config = new ScenarioConfig();
        config.visualization.style = suppliers.get(cbStyle.getSelectedIndex()).get();
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
