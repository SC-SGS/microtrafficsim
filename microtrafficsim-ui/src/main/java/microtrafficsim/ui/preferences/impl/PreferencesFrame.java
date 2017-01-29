package microtrafficsim.ui.preferences.impl;

import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.ui.gui.statemachine.GUIController;
import microtrafficsim.ui.gui.statemachine.GUIEvent;
import microtrafficsim.ui.preferences.IncorrectSettingsException;
import microtrafficsim.ui.preferences.PrefElement;
import microtrafficsim.ui.preferences.Preferences;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;


/**
 * @author Dominic Parga Cacheiro
 */
public class PreferencesFrame extends JFrame implements Preferences {
    public static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 14);
    public static final Font TEXT_FONT   = new Font("Arial", Font.PLAIN, 14);
    public final GeneralPanel generalPanel;
    public final CrossingLogicPanel crossingLogicPanel;
    public final VisualizationPanel visualizationPanel;
    public final ConcurrencyPanel concurrencyPanel;
    private final JPanel content, bottom;
    private final GUIController guiController;

    public PreferencesFrame(GUIController guiController) {
        super("Simulation parameter settings");

        this.guiController = guiController;
        generalPanel       = new GeneralPanel();
        crossingLogicPanel = new CrossingLogicPanel();
        visualizationPanel = new VisualizationPanel();
        concurrencyPanel   = new ConcurrencyPanel();

        setLayout(new BorderLayout());
        //        setMinimumSize(new Dimension(550, 330));

        /* JFrame <- JScrollPane <- JPanel content <- JPanels top and bottom */
        content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        add(new JScrollPane(content), BorderLayout.CENTER);

        bottom = new JPanel(new GridBagLayout());
        add(bottom, BorderLayout.SOUTH);
    }

    /**
     * TUTORIAL todo remove
     */
    public static JFrame create_demo() {
        JFrame jframe = new JFrame();
        jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jframe.setMinimumSize(new Dimension(500, 100));
        jframe.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        JComponent         c;

        constraints.gridwidth  = GridBagConstraints.REMAINDER;
        constraints.gridheight = 2;
        constraints.insets     = new Insets(0, 5, 0, 5);
        JLabel header          = new JLabel("Benno's Pizza Service");
        header.setFont(PreferencesFrame.HEADER_FONT);
        jframe.add(header, constraints);

        constraints.gridheight = 1;
        constraints.gridwidth  = GridBagConstraints.RELATIVE;
        constraints.anchor     = GridBagConstraints.WEST;
        c                      = new JLabel("Name:");
        c.setFont(PreferencesFrame.TEXT_FONT);
        jframe.add(c, constraints);

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill      = GridBagConstraints.HORIZONTAL;
        constraints.insets    = new Insets(0, 5, 0, 5);
        c                     = new JTextField("");
        c.setFont(PreferencesFrame.TEXT_FONT);
        jframe.add(c, constraints);

        constraints.weightx   = 1.0;
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        c                     = new JLabel("Sorte:");
        c.setFont(PreferencesFrame.TEXT_FONT);
        jframe.add(c, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        c                     = new JLabel("Extras:");
        c.setFont(PreferencesFrame.TEXT_FONT);
        jframe.add(c, constraints);

        constraints.fill      = GridBagConstraints.NONE;
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        c                     = new JCheckBox("Margherita");
        c.setFont(PreferencesFrame.TEXT_FONT);
        jframe.add(c, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        c                     = new JCheckBox("Mais");
        c.setFont(PreferencesFrame.TEXT_FONT);
        jframe.add(c, constraints);
        c = new JCheckBox("Schinken");
        c.setFont(PreferencesFrame.TEXT_FONT);
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        jframe.add(c, constraints);
        c = new JCheckBox("Pfeffersalami");
        c.setFont(PreferencesFrame.TEXT_FONT);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        jframe.add(c, constraints);

        c = new JLabel("Anmerkungen:");
        c.setFont(PreferencesFrame.TEXT_FONT);
        jframe.add(c, constraints);

        constraints.fill       = GridBagConstraints.BOTH;
        constraints.gridwidth  = 1;
        constraints.gridheight = 2;
        constraints.insets     = new Insets(0, 5, 0, 5);

        c = new JTextField();
        c.setFont(PreferencesFrame.TEXT_FONT);
        jframe.add(c, constraints);

        constraints.anchor     = GridBagConstraints.WEST;
        constraints.fill       = GridBagConstraints.NONE;
        constraints.gridwidth  = GridBagConstraints.REMAINDER;
        constraints.gridheight = 1;
        c                      = new JCheckBox("Abholer");
        c.setFont(PreferencesFrame.TEXT_FONT);
        jframe.add(c, constraints);
        c = new JButton("Löschen");
        c.setFont(PreferencesFrame.TEXT_FONT);
        jframe.add(c, constraints);

        constraints.anchor    = GridBagConstraints.CENTER;
        constraints.fill      = GridBagConstraints.NONE;
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        constraints.insets    = new Insets(20, 0, 0, 0);
        c                     = new JButton("OK");
        c.setFont(PreferencesFrame.TEXT_FONT);
        jframe.add(c, constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        c                     = new JButton("Abbruch");
        c.setFont(PreferencesFrame.TEXT_FONT);
        jframe.add(c, constraints);
        jframe.pack();
        jframe.setLocationRelativeTo(null);    // center on screen; close to setVisible

        return jframe;
    }

    @Override
    public void create() {
        /* add sub panels */
        for (PreferencesPanel panel :
             new PreferencesPanel[] {generalPanel, crossingLogicPanel, visualizationPanel, concurrencyPanel}) {
            /* define margins and line */
            Border margins = BorderFactory.createEmptyBorder(10, 5, 10, 5);
            Border line    = BorderFactory.createLineBorder(Color.black);

            /* set titled border */
            TitledBorder border = BorderFactory.createTitledBorder(line, panel.title);
            border.setTitleFont(HEADER_FONT);
            /* add */
            panel.setBorder(BorderFactory.createCompoundBorder(margins, border));
            panel.create();
            content.add(panel);
        }

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx            = 1;
        constraints.fill               = GridBagConstraints.HORIZONTAL;
        JPanel gap                     = new JPanel();
        bottom.add(gap, constraints);

        constraints         = new GridBagConstraints();
        constraints.weightx = 0;
        JButton buAccept    = new JButton("Accept");
        buAccept.addActionListener(e -> guiController.transiate(GUIEvent.ACCEPT));
        bottom.add(buAccept, constraints);

        constraints         = new GridBagConstraints();
        constraints.weightx = 0;
        JButton buCancel    = new JButton("Cancel");
        buCancel.addActionListener(e -> guiController.transiate(GUIEvent.CANCEL));
        bottom.add(buCancel, constraints);
    }

    @Override
    public void setSettings(ScenarioConfig config) {
        generalPanel.setSettings(config);
        crossingLogicPanel.setSettings(config);
        visualizationPanel.setSettings(config);
        concurrencyPanel.setSettings(config);
    }

    @Override
    public ScenarioConfig getCorrectSettings() throws IncorrectSettingsException {
        ScenarioConfig             config           = new ScenarioConfig();
        boolean                    exceptionOccured = false;
        IncorrectSettingsException exception        = new IncorrectSettingsException();


        try {
            config.update(generalPanel.getCorrectSettings());
        } catch (IncorrectSettingsException e) {
            exception.appendToMessage(e.getMessage());
            exceptionOccured = true;
        }
        try {
            config.crossingLogic.update(crossingLogicPanel.getCorrectSettings().crossingLogic);
        } catch (IncorrectSettingsException e) {
            exception.appendToMessage(e.getMessage());
            exceptionOccured = true;
        }
        try {
            config.visualization.update(visualizationPanel.getCorrectSettings().visualization);
        } catch (IncorrectSettingsException e) {
            exception.appendToMessage(e.getMessage());
            exceptionOccured = true;
        }
        try {
            config.multiThreading.update(concurrencyPanel.getCorrectSettings().multiThreading);
        } catch (IncorrectSettingsException e) {
            exception.appendToMessage(e.getMessage());
            exceptionOccured = true;
        }


        if (exceptionOccured) throw exception;


        return config;
    }

    @Override
    public void setEnabled(PrefElement id, boolean enabled) {
        switch (id) {
        // General
        case sliderSpeedup:
        case maxVehicleCount:
        case seed:
        case metersPerCell:
            generalPanel.setEnabled(id, enabled);
            break;

        // Visualization

        // concurrency
        case nThreads:
        case vehiclesPerRunnable:
        case nodesPerThread:
            concurrencyPanel.setEnabled(id, enabled);
            break;

        // crossing logic
        case edgePriority:
        case priorityToThe:
        case onlyOneVehicle:
        case friendlyStandingInJam: crossingLogicPanel.setEnabled(id, enabled);
        }
    }

    /*
    |=======|
    | stuff |
    |=======|
    */

    @Override
    public void setAllEnabled(boolean enabled) {
        generalPanel.setAllEnabled(enabled);
        visualizationPanel.setAllEnabled(enabled);
        concurrencyPanel.setAllEnabled(enabled);
        crossingLogicPanel.setAllEnabled(enabled);
    }
}
