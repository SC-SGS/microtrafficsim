package microtrafficsim.ui.preferences.view;

import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.ui.gui.ScrollablePanel;
import microtrafficsim.ui.gui.statemachine.GUIController;
import microtrafficsim.ui.gui.statemachine.GUIEvent;
import microtrafficsim.ui.preferences.IncorrectSettingsException;
import microtrafficsim.ui.preferences.model.PrefElement;
import microtrafficsim.ui.preferences.model.PreferencesFrameModel;
import microtrafficsim.ui.preferences.model.PreferencesModel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * @author Dominic Parga Cacheiro
 */
public class PreferencesFrame extends JFrame implements PreferencesView {

    public static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 14);
    public static final Font TEXT_FONT   = new Font("Arial", Font.PLAIN, 14);

    private final PreferencesFrameModel model;
    private final GeneralPanel generalPanel;
    private final CrossingLogicPanel crossingLogicPanel;
    private final VisualizationPanel visualizationPanel;
    private final ConcurrencyPanel concurrencyPanel;
    private final JPanel center;
    private final JPanel bottom;

    /**
     * You should call {@link #create()} before you use this frame.
     *
     * @param guiController
     */
    public PreferencesFrame(GUIController guiController) {
        super();
        model = new PreferencesFrameModel("Scenario parameter settings", guiController);
        setTitle(model.getTitle());

        setLayout(new BorderLayout());

        /* JFrame <- JScrollPane <- JPanel content <- JPanel center <- stuff */
        ScrollablePanel content = new ScrollablePanel(new BorderLayout());
        content.setScrollableTracksViewportWidth(true);
        content.setScrollableTracksViewportHeight(false);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);


        center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        content.add(center, BorderLayout.CENTER);


        /* init sub panels */
        generalPanel = new GeneralPanel();
        crossingLogicPanel = new CrossingLogicPanel();
        visualizationPanel = new VisualizationPanel();
        concurrencyPanel = new ConcurrencyPanel();


        /* bottom panel with buttons outside of scroll panel */
        bottom = new JPanel();
        add(bottom, BorderLayout.SOUTH);
    }

    public void create() {

        setMinimumSize(new Dimension(200, 200));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 630));

        generalPanel.create();
        center.add(border(generalPanel));

        crossingLogicPanel.create();
        center.add(border(crossingLogicPanel));

        visualizationPanel.create();
        center.add(border(visualizationPanel));

        concurrencyPanel.create();
        center.add(border(concurrencyPanel));


        /* bottom buttons */
        bottom.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx            = 1;
        constraints.fill               = GridBagConstraints.HORIZONTAL;
        JPanel gap                     = new JPanel();
        bottom.add(gap, constraints);

        constraints         = new GridBagConstraints();
        constraints.weightx = 0;
        JButton buAccept    = new JButton("Accept");
        buAccept.addActionListener(e -> model.getGuiController().transiate(GUIEvent.ACCEPT_PREFS));
        bottom.add(buAccept, constraints);

        constraints         = new GridBagConstraints();
        constraints.weightx = 0;
        JButton buCancel    = new JButton("Cancel");
        buCancel.addActionListener(e -> model.getGuiController().transiate(GUIEvent.CANCEL_PREFS));
        bottom.add(buCancel, constraints);


        /* finish and return */
        getRootPane().revalidate();
        pack();
    }


    /**
     * @param panel
     * @return the given panel (just for practical purposes)
     */
    private PreferencesPanel border(PreferencesPanel panel) {

        /* define margins and line */
        Border margins = BorderFactory.createEmptyBorder(10, 5, 10, 5);
        Border line    = BorderFactory.createLineBorder(Color.black);

        /* set titled border */
        TitledBorder border = BorderFactory.createTitledBorder(line, panel.getModel().getTitle());
        border.setTitleFont(HEADER_FONT);
        panel.setBorder(BorderFactory.createCompoundBorder(margins, border));

        return panel;
    }


    @Override
    public PreferencesModel getModel() {
        return model;
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
            case style:
                visualizationPanel.setEnabled(id, enabled);

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
}