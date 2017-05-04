package microtrafficsim.ui.preferences.view;

import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.ui.gui.statemachine.GUIController;
import microtrafficsim.ui.gui.statemachine.GUIEvent;
import microtrafficsim.ui.gui.utils.ScrollablePanel;
import microtrafficsim.ui.preferences.IncorrectSettingsException;
import microtrafficsim.ui.preferences.model.PreferencesFrameModel;
import microtrafficsim.ui.preferences.model.PreferencesModel;
import microtrafficsim.utils.functional.Procedure;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Dominic Parga Cacheiro
 */
public class PreferencesFrame extends JFrame implements PreferencesView {

    public static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 14);
    public static final Font TEXT_FONT   = new Font("Arial", Font.PLAIN, 14);


    private final ReentrantLock lockUserInput;
    private final AtomicBoolean isBusy;

    private final PreferencesFrameModel model;
    private final GeneralPanel generalPanel;
    private final ScenarioPanel scenarioPanel;
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
        lockUserInput = new ReentrantLock();
        isBusy        = new AtomicBoolean(false);

        model = new PreferencesFrameModel("Simulation parameter settings", guiController);
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
        scenarioPanel = new ScenarioPanel();
        crossingLogicPanel = new CrossingLogicPanel();
        visualizationPanel = new VisualizationPanel();
        concurrencyPanel = new ConcurrencyPanel();


        /* bottom panel with buttons outside of scroll panel */
        bottom = new JPanel();
        add(bottom, BorderLayout.SOUTH);


        /* setup gui */
        create();
    }

    private void create() {

        center.add(border(generalPanel));
        center.add(border(scenarioPanel));
        center.add(border(crossingLogicPanel));
        center.add(border(visualizationPanel));
        center.add(border(concurrencyPanel));


        /* bottom buttons */
        GridBagConstraints constraints;
        JButton button;

        constraints         = new GridBagConstraints();
        constraints.weightx = 0;
        button              = new JButton("Load config...");
        button.addActionListener(e -> model.getGuiController().transiate(GUIEvent.LOAD_CONFIG));
        bottom.add(button, constraints);

        constraints         = new GridBagConstraints();
        constraints.weightx = 0;
        button              = new JButton("Save config...");
        button.addActionListener(e -> model.getGuiController().transiate(GUIEvent.SAVE_CONFIG));
        bottom.add(button, constraints);

        bottom.setLayout(new GridBagLayout());
        constraints         = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.fill    = GridBagConstraints.HORIZONTAL;
        JPanel gap          = new JPanel();
        bottom.add(gap, constraints);

        constraints         = new GridBagConstraints();
        constraints.weightx = 0;
        button              = new JButton("Accept");
        button.addActionListener(e -> model.getGuiController().transiate(GUIEvent.ACCEPT_PREFS));
        bottom.add(button, constraints);

        constraints         = new GridBagConstraints();
        constraints.weightx = 0;
        button              = new JButton("Cancel");
        button.addActionListener(e -> model.getGuiController().transiate(GUIEvent.CANCEL_PREFS));
        bottom.add(button, constraints);


        /* finish and return */
        setPreferredSize(new Dimension(550, 650));
        getRootPane().revalidate();
        pack();
    }


    /**
     * @param panel
     * @return the given panel (just for practical purposes)
     */
    private PreferencesPanel border(PreferencesPanel panel) {

        /* set titled border */
        Border line         = BorderFactory.createLineBorder(Color.black);
        TitledBorder border = BorderFactory.createTitledBorder(line, panel.getModel().getTitle());
        border.setTitleFont(HEADER_FONT);


        Border outsideBorder = BorderFactory.createEmptyBorder(10, 5, 10, 5);
        Border insideBorder = BorderFactory.createCompoundBorder(border, new EmptyBorder(0, 5, 0, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(outsideBorder, insideBorder));

        return panel;
    }


    @Override
    public PreferencesModel getModel() {
        return model;
    }

    @Override
    public void setSettings(boolean indeed, SimulationConfig config) {
        generalPanel.setSettings(indeed, config);
        scenarioPanel.setSettings(indeed, config);
        crossingLogicPanel.setSettings(indeed, config);
        visualizationPanel.setSettings(indeed, config);
        concurrencyPanel.setSettings(indeed, config);
    }

    @Override
    public SimulationConfig getCorrectSettings() throws IncorrectSettingsException {
        SimulationConfig config              = new SimulationConfig();
        boolean exceptionOccured             = false;
        IncorrectSettingsException exception = new IncorrectSettingsException();


        try {
            config.update(generalPanel.getCorrectSettings());
        } catch (IncorrectSettingsException e) {
            exception.appendToMessage(e.getMessage());
            exceptionOccured = true;
        }
        try {
            config.scenario.update(scenarioPanel.getCorrectSettings().scenario);
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
    public boolean setEnabledIfEditable(SimulationConfig.Element element, boolean enabled) {
        enabled = PreferencesView.super.setEnabledIfEditable(element, enabled);

        switch (element) {
            // General
            case sliderSpeedup:
            case maxVehicleCount:
            case seed:
            case metersPerCell:
                generalPanel.setEnabledIfEditable(element, enabled);
                break;

            // scenario
            case showAreasWhileSimulating:
            case nodesAreWeightedUniformly:
            case scenarioSelection:
                scenarioPanel.setEnabledIfEditable(element, enabled);
                break;

            // crossing logic
            case edgePriority:
            case priorityToThe:
            case onlyOneVehicle:
            case friendlyStandingInJam:
                crossingLogicPanel.setEnabledIfEditable(element, enabled);
                break;

            // Visualization
            case style:
                visualizationPanel.setEnabledIfEditable(element, enabled);

            // concurrency
            case nThreads:
            case vehiclesPerRunnable:
            case nodesPerThread:
                concurrencyPanel.setEnabledIfEditable(element, enabled);
                break;
        }

        return enabled;
    }

    /*
    |=============|
    | concurrency |
    |=============|
    */
    /**
     * Creates a thread executing the given procedure. The thread is NOT started yet. If the preferences frame is busy,
     * nothing is done. If the procedure has finished, it should call {@link #hasFinishedProcedureExecution()}.
     *
     * @return The thread able to execute the procedure; null if busy
     */
    public Thread requestAnExecutionThread(Procedure procedure) {
        if (!lockUserInput.tryLock())
            return null;

        Thread thread = null;
        if (isBusy.compareAndSet(false, true)) {
            thread = new Thread(procedure::invoke);
        }

        lockUserInput.unlock();
        return thread;
    }

    /**
     * This method just sets this {@code preferences frame} to not busy.
     */
    public void hasFinishedProcedureExecution() {
        isBusy.set(false);
    }
}
