package microtrafficsim.ui.gui.statemachine.impl;

import microtrafficsim.core.convenience.DefaultParserConfig;
import microtrafficsim.core.convenience.MapViewer;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.simulation.scenarios.impl.AreaScenario;
import microtrafficsim.core.simulation.scenarios.impl.EndOfTheWorldScenario;
import microtrafficsim.core.simulation.scenarios.impl.RandomRouteScenario;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.input.KeyCommand;
import microtrafficsim.core.vis.scenario.areas.Area;
import microtrafficsim.core.vis.scenario.areas.ScenarioAreaOverlay;
import microtrafficsim.core.vis.simulation.VehicleOverlay;
import microtrafficsim.math.geometry.polygons.*;
import microtrafficsim.math.geometry.polygons.Polygon;
import microtrafficsim.ui.gui.menues.MTSMenuBar;
import microtrafficsim.ui.gui.statemachine.GUIController;
import microtrafficsim.ui.gui.statemachine.GUIEvent;
import microtrafficsim.ui.preferences.IncorrectSettingsException;
import microtrafficsim.ui.preferences.model.PrefElement;
import microtrafficsim.ui.preferences.view.PreferencesFrame;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>
 * Controls user input and represents an interface between visualization/parsing, simulation and GUI.
 *
 * <p>
 * A central method is {@link #transiate(GUIEvent)}, which is initializing user tasks. It is thread-safe, so you
 * can call it concurrently. One call is accepted, all others are ignored. Otherwise, you or an angry user could spam
 * the GUI.
 *
 * <p>
 * This class uses a static instance of {@link EasyMarkableLogger} to log. You can change its logging behaviour
 * using {@link microtrafficsim.utils.logging.LoggingLevel LoggingLevel}
 * .{@link microtrafficsim.utils.logging.LoggingLevel.Type Type}.
 *
 * @author Dominic Parga Cacheiro
 *
 * @see #transiate(GUIEvent, File)
 */
public class SimulationController implements GUIController {

    private static final Logger logger = new EasyMarkableLogger(SimulationController.class);

    /*
    |=======|
    | ideas |
    |=======|
    */
    // TODO map double buffering (in the way that you ask for changing the map after parsing has finished)
    // TODO simulation double buffering (in the way that you ask for changing the map after parsing has finished)

    /* multithreading: user input and task execution */
    private final AtomicBoolean isExecutingUserTask;
    private final ReentrantLock lock_user_input;

    /* multithreading: interrupting parsing */
    private final AtomicBoolean isParsing;
    private       Thread        parsingThread;

    /* multithreading: interrupting scenario preparation */
    private final AtomicBoolean isBuildingScenario;
    private       Thread        scenarioBuildThread;

    /* state marker */
    private boolean newSim;
    private boolean isCreated;

    /* general */
    private final SimulationConfig config;

    /* visualization and parsing */
    private final MapViewer      mapviewer;
    private final VehicleOverlay overlay;
    private ScenarioAreaOverlay  scenarioAreaOverlay;
    private       OSMParser      parser;
    private       Graph          streetgraph;

    /* simulation */
    private Simulation      simulation;
    private ScenarioBuilder scenarioBuilder;

    /* gui */
    private final String           frameTitleRaw;
    private final JFrame           frame;
    private final MTSMenuBar       menubar;
    private final PreferencesFrame preferences;
    private final JFileChooser     mapfileChooser;

    public SimulationController() {
        this(new BuildSetup());
    }

    public SimulationController(BuildSetup buildSetup) {

        /* multithreading */
        isExecutingUserTask = new AtomicBoolean(false);
        lock_user_input    = new ReentrantLock();
        isParsing          = new AtomicBoolean(false);
        isBuildingScenario = new AtomicBoolean(false);

        /* state marker */
        isCreated = false;
        newSim    = false;

        /* general */
        config = buildSetup.config;

        /* visualization and parsing */
        mapviewer        = buildSetup.mapviewer;
        overlay          = buildSetup.overlay;

        /* simulation */
        simulation      = buildSetup.simulation;
        scenarioBuilder = buildSetup.scenarioBuilder;
        overlay.setSimulation(simulation);

        /* gui */
        frameTitleRaw    = buildSetup.frameTitle;
        frame            = new JFrame(frameTitleRaw);
        menubar          = new MTSMenuBar();
        preferences      = new PreferencesFrame(this);
        mapfileChooser   = new JFileChooser();
        mapfileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        mapfileChooser.setFileFilter(new FileFilter() {

            @Override
            public String getDescription() {
                return ".osm";
            }

            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) return true;

                String extension = null;

                String filename = file.getName();
                int    i        = filename.lastIndexOf('.');

                if (i > 0)
                    if (i < filename.length() - 1)
                        extension = filename.substring(i + 1).toLowerCase();

                if (extension == null) return false;

                switch (extension) {
                    case "osm": return true;
                    default:    return false;
                }
            }
        });


        /* create */
        create();
        show();
        updateMenuBar();
    }


    /*
    |=========|
    | general |
    |=========|
    */
    private void create() {
        if (isCreated) throw new RuntimeException("The simulation controller has already been created.");

        try {
            mapviewer.create(config);
        } catch (UnsupportedFeatureException e) { e.printStackTrace(); }

        parser = DefaultParserConfig.get(config).build();

        /* create preferences */
        preferences.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        preferences.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                transiate(GUIEvent.CANCEL_PREFS);
            }
        });
        preferences.pack();
        preferences.setLocationRelativeTo(null);    // center on screen; close to setVisible
        preferences.setVisible(false);

        /* overlays */
        scenarioAreaOverlay = new ScenarioAreaOverlay();
        SwingUtilities.invokeLater(() -> {
            scenarioAreaOverlay.setEnabled(false, false, false);
        });
        mapviewer.addOverlay(0, scenarioAreaOverlay);
        mapviewer.addOverlay(1, overlay);

        /* setup JFrame */
        menubar.menuMap.addActions(this);
        menubar.menuLogic.addActions(this);
        // todo toolbar for icons for run etc.
        //            JToolBar toolbar = new JToolBar("Menu");
        //            toolbar.add(new MTSMenuBar(this).create());
        //            addToTopBar(toolbar);
        frame.add(menubar, BorderLayout.NORTH);
        frame.setSize(mapviewer.getInitialWindowWidth(), mapviewer.getInitialWindowHeight());
        frame.add(mapviewer.getVisualizationPanel());

        /*
         * Note: JOGL automatically calls glViewport, we need to make sure that this
         * function is not called with a height or width of 0! Otherwise the program
         * crashes.
         */
        frame.setMinimumSize(new Dimension(100, 100));

        /* on close: stop the visualization and shutdown */
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });

        /* set state */
        isCreated = true;
    }

    private void show() {
        if (!isCreated) throw new RuntimeException("The simulation controller has to be created before it is shown.");

        frame.setLocationRelativeTo(null);    // center on screen; close to setVisible
        frame.setVisible(true);
        mapviewer.show();
    }

    private void shutdown() {
        int choice = JOptionPane.showConfirmDialog(frame,
                "Do you really want to exit?", "Close Program",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            if (parsingThread != null)
                parsingThread.interrupt();
            if (scenarioBuildThread != null)
                scenarioBuildThread.interrupt();
            mapviewer.destroy();
            frame.dispose();
            System.exit(0);
        }
    }


    /*
    |===================|
    | (i) GUIController |
    |===================|
    */
    /**
     * <p>
     * Works like a transition of a state machine. Depending on the event, several functions are called.<br>
     * NOTE: If not differently mentioned below, all events initializes executions if and only if no other execution
     * is currently active.
     *
     * <ul>
     * <li> {@link GUIEvent#EXIT} - If this is called, all running executions are interrupted and the application
     * exits. So this event is getting executed even if there are other executions active. <br>
     * <li> {@link GUIEvent#LOAD_MAP} - Asks the user to choose a map file. If (and only if) a parsing is already
     * running, the user is asked to cancel the currently running parsing task. <br>
     * <li> {@link GUIEvent#NEW_SCENARIO} - Pauses a running simulation and shows the scenario-preferences-window for
     * choosing scenario parameters. If (and only if) a scenario is already preparing (not finished), the user is
     * asked to cancel the currently running scenario-building task. <br>
     * <li> {@link GUIEvent#ACCEPT_PREFS} - Closes the scenario-preferences-window if all settings are correctly formatted.
     * If the scenario should be a new one (due to {@code GUIEvent#NEW_SCENARIO}), a new scenario is build. This
     * event does ignore other running tasks because it has to be called after choosing scenario parameters. It
     * is only closing the scenario-preferences-window and updating the GUI, if no new scenario is chosen. BUT it
     * unlocks the lock of other executions, so we recommend to call it only to finish the event NEW_SCENARIO. <br>
     *
     * <li> {@link GUIEvent#CANCEL_PREFS} Analogue to {@code GUIEvent.ACCEPT_PREFS}: It is only closing the
     * scenario-preferences-window and updating the GUI. BUT it unlocks the lock of other executions, so we recommend to
     * call it only to finish the event NEW_SCENARIO. <br>
     * <li> {@link GUIEvent#EDIT_SCENARIO} Pauses a running simulation and shows the scenario-preferences-window for
     * choosing scenario parameters. <br>
     * <li> {@link GUIEvent#RUN_SIM} Runs the simulation. <br>
     * <li> {@link GUIEvent#RUN_SIM_ONE_STEP} Pauses the simulation and runs it one step. <br>
     * <li> {@link GUIEvent#PAUSE_SIM} Pauses the simulation. <br>
     * </ul>
     */
    @Override
    public void transiate(GUIEvent event, final File file) {
        logger.debug("GUIEvent called = GUIEvent." + event);

        if (event == GUIEvent.EXIT)
            shutdown();
        if (!lock_user_input.tryLock())
            return;

        switch (event) {
            case LOAD_MAP:
                transitionLoadMap(file);
                break;
            case CHANGE_AREA_SELECTION:
                transitionChangeAreaSelection();
                break;
            case NEW_SCENARIO:
                transitionNewScenario();
                break;
            case ACCEPT_PREFS:
                transitionAcceptPreferences();
                break;
            case CANCEL_PREFS:
                transitionCancelPreferences();
                break;
            case EDIT_SCENARIO:
                transitionEditScenario();
                break;
            case RUN_SIM:
                transitionRunSim();
                break;
            case RUN_SIM_ONE_STEP:
                transitionRunSimOneStep();
                break;
            case PAUSE_SIM:
                transitionPauseSim();
                break;
        }

        lock_user_input.unlock();
    }

    private void transitionLoadMap(File file) {
        if (isParsing.get()) {
            new Thread(() -> {
                // ask user to cancel parsing
                Object[] options = { "Yes", "No" };
                int choice = JOptionPane.showOptionDialog(
                        null,
                        "Are you sure to cancel the parsing?",
                        "Cancel parsing?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        options,
                        options[1]);
                // if yes: cancel
                if (choice == JOptionPane.YES_OPTION)
                    cancelParsing();
            }).start();
        } else if (isExecutingUserTask.compareAndSet(false, true)) {
            parsingThread = new Thread(() -> {
                isParsing.set(true);

                closePreferences();
                pauseSim();

                File loadedFile = file == null ? askForMapFile() : file;
                if (loadedFile != null)
                    parseAndShow(loadedFile);

                isParsing.set(false);
                updateMenuBar();
                isExecutingUserTask.set(false);
            });
            parsingThread.start();
        }
    }

    private void transitionChangeAreaSelection() {
        /* goal: enable scenario area overlay */

        if (isExecutingUserTask.compareAndSet(false, true)) {
            new Thread(() -> {
                boolean enableScenarioAreaOverlay =
                        /* check if overlay is already enabled */
                        !scenarioAreaOverlay.hasEventsEnabled()
                        /* check if there is a map */
                        && streetgraph != null;

                if (enableScenarioAreaOverlay) {
                    /* check if there is a scenario => ask for removing it */
                    if (simulation.hasScenario()) {
                        /* ask user to remove currently running scenario */
                        Object[] options = {"Yes", "No"};
                        int choice = JOptionPane.showOptionDialog(
                                null,
                                "To change the origin/destination areas, the currently running scenario has to be removed."
                                        + System.lineSeparator()
                                        + "Do you still like to change the areas?",
                                "Remove currently running scenario?",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE,
                                null,
                                options,
                                options[1]);
                        /* if yes: remove */
                        if (choice == JOptionPane.YES_OPTION)
                            simulation.removeCurrentScenario();
                        else
                            enableScenarioAreaOverlay = false;
                    }

                    scenarioAreaOverlay.setEventsEnabled(enableScenarioAreaOverlay);
                }

                isExecutingUserTask.set(false);
            }).start();
        }
    }

    private void transitionNewScenario() {
        if (isBuildingScenario.get()) {
            askUserToCancelScenarioBuilding();
        } else if (isExecutingUserTask.compareAndSet(false, true)) { // unlock after accept/cancel
            new Thread(() -> {
                if (streetgraph != null) {
                    pauseSim();
                    newSim = true;
                    showPreferences();
                } else
                    isExecutingUserTask.set(false);
            }).start();
        }
    }

    private void transitionAcceptPreferences() {
        scenarioBuildThread = new Thread(() -> {
            if (isBuildingScenario.compareAndSet(false, true)) {

                /* get new config */
                SimulationConfig newConfig = null;
                try {
                    newConfig = preferences.getCorrectSettings();
                } catch (IncorrectSettingsException e) {
                    JOptionPane.showMessageDialog(
                            null,
                            e.getMessage(),
                            "Error: wrong preferences values",
                            JOptionPane.ERROR_MESSAGE);
                }

                /* process new config if correct input */
                if (newConfig != null) {
                    closePreferences();
                    config.update(newConfig);

                    updateScenario();
                    if (newSim) {
                        startNewScenario();
                        newSim = false;
                    }

                    updateMenuBar();
                    isExecutingUserTask.set(false);
                }

                isBuildingScenario.set(false);
            }
        });
        scenarioBuildThread.start();
    }

    private void transitionCancelPreferences() {
        new Thread(() -> {
            closePreferences();
            updateMenuBar();
            isExecutingUserTask.set(false);
        }).start();
    }

    private void transitionEditScenario() {
        if (isExecutingUserTask.compareAndSet(false, true)) { // unlock after accept/cancel
            new Thread(() -> {
                pauseSim();
                newSim = false;
                showPreferences();

                updateMenuBar();
            }).start();
        }
    }

    private void transitionRunSim() {
        if (isExecutingUserTask.compareAndSet(false, true)) {
            if (!simulation.hasScenario())
                isExecutingUserTask.set(false);
            else {
                new Thread(() -> {
                    runSim();

                    updateMenuBar();
                    isExecutingUserTask.set(false);
                }).start();
            }
        }
    }

    private void transitionRunSimOneStep() {
        if (isExecutingUserTask.compareAndSet(false, true)) {
            if (!simulation.hasScenario())
                isExecutingUserTask.set(false);
            else {
                new Thread(() -> {
                    pauseSim();
                    runSimOneStep();

                    updateMenuBar();
                    isExecutingUserTask.set(false);
                }).start();
            }
        }
    }

    private void transitionPauseSim() {
        if (isExecutingUserTask.compareAndSet(false, true)) {
            new Thread(() -> {
                pauseSim();

                updateMenuBar();
                isExecutingUserTask.set(false);
            }).start();
        }
    }

    @Override
    public void addKeyCommand(short event, short vk, KeyCommand command) {
        mapviewer.addKeyCommand(event, vk, command);
    }


    /*
    |========|
    | window |
    |========|
    */
    private String getDefaultFrameTitle() {
        return frameTitleRaw;
    }

    /**
     * Return the default window title.
     * @return the default frame title.
     */
    private String getDefaultFrameTitle(File file) {
        if (file != null)
            return getDefaultFrameTitle() + " - [" + file + "]";

        return getDefaultFrameTitle();
    }

    private String getParsingFrameTitle() {
        return getDefaultFrameTitle() + " - Parsing new map, please wait...";
    }

    private String getParsingFrameTitle(File file) {
        return getDefaultFrameTitle() + " - Parsing [" + file + "]";
    }

    private void updateMenuBar() {
        if (!isCreated) return;

        boolean hasStreetgraph = streetgraph != null;
        boolean hasScenario    = simulation.getScenario() != null;

        menubar.menuMap.setEnabled(true);
        menubar.menuMap.itemLoadMap.setEnabled(true);

        menubar.menuLogic.setEnabled(true);
        menubar.menuLogic.itemRunPause.setEnabled(hasStreetgraph && hasScenario);
        menubar.menuLogic.itemRunOneStep.setEnabled(hasStreetgraph && hasScenario);
        menubar.menuLogic.itemEditSim.setEnabled(true);
        menubar.menuLogic.itemNewSim.setEnabled(hasStreetgraph);
        menubar.menuLogic.itemChangeAreaSelection.setEnabled(hasStreetgraph);
    }

    /*
    |================|
    | map and parser |
    |================|
    */
    private File askForMapFile() {
        int action = mapfileChooser.showOpenDialog(null);
        if (action == JFileChooser.APPROVE_OPTION)
            return mapfileChooser.getSelectedFile();

        return null;
    }

    private void parseAndShow(File file) {
        String cachedTitle = frame.getTitle();
        frame.setTitle(getParsingFrameTitle(file));

        OSMParser.Result result;

        try {
            /* parse file and create tiled provider */
            result = parser.parse(file);
        } catch (InterruptedException e) {
            logger.info("Parsing interrupted by user");
            result = null; // might be unnecessary here
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "The chosen file has a wrong format.\nTherefore it could not be parsed.",
                    "Error: wrong osm-file format",
                    JOptionPane.ERROR_MESSAGE);
            result = null;
        }


        if (result != null) {
            if (result.streetgraph != null) {
                simulation.removeCurrentScenario();
                streetgraph = result.streetgraph;
                overlay.setEnabled(true);
                scenarioAreaOverlay.setEnabled(true, true, false);

                try {
                    mapviewer.setMap(result.segment);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                cachedTitle = getDefaultFrameTitle(file);
            }
        }

        frame.setTitle(cachedTitle);
    }

    private void cancelParsing() {
        parsingThread.interrupt();
        try {
            parsingThread.join();
        } catch (InterruptedException ignored) {}
    }

    /*
    |============|
    | simulation |
    |============|
    */
    private void runSim() {
        menubar.menuLogic.simIsPaused(false);
        simulation.run();
    }

    private void runSimOneStep() {
        menubar.menuLogic.simIsPaused(true);
        simulation.runOneStep();
    }

    private void pauseSim() {
        menubar.menuLogic.simIsPaused(true);
        simulation.cancel();
    }

    private void startNewScenario() {

        String oldTitle = frame.getTitle();
        EventQueue.invokeLater(() -> frame.setTitle("Starting new scenario"));


        /* update streetgraph */
        // mapviewer.createParser(config) is not needed because the mapviewer gets the final config-reference
        streetgraph.reset();
        streetgraph.setSeed(config.seed);


        /* create new scenario */
        EventQueue.invokeLater(() -> frame.setTitle("Calculating vehicle routes 0%"));


        /* remove old scenario */
        simulation.removeCurrentScenario();
        scenarioAreaOverlay.setEventsEnabled(false);
        scenarioAreaOverlay.setPropertiesVisible(false);


        /* create and prepare new scenario */
        AreaScenario scenario;
        if (config.scenario.selectedClass == AreaScenario.class) {
            scenario = new AreaScenario(config.seed, config, streetgraph);
            /* get areas from overlay */
            scenarioAreaOverlay.getAreas().stream()
                    .map(area -> area.getUnprojectedArea(mapviewer.getProjection()))
                    .forEach(scenario::addArea);
            scenario.refillNodeLists();
        } else if (config.scenario.selectedClass == EndOfTheWorldScenario.class) {
            scenario = new EndOfTheWorldScenario(config.seed, config, streetgraph);
        } else {
            if (config.scenario.selectedClass != RandomRouteScenario.class)
                logger.error("Chosen scenario could not be found. " + RandomRouteScenario.class.getSimpleName() + " is used instead.");
            scenario = new RandomRouteScenario(config.seed, config, streetgraph);
        }
        /* update area overlay */
        scenarioAreaOverlay.removeAllAreas();
        scenario.getAreas().stream()
                .map(area -> area.getProjectedArea(mapviewer.getProjection(), area.getType()))
                .forEach(scenarioAreaOverlay::add);

        try {
            scenarioBuilder.prepare(
                    scenario,
                    currentInPercent -> EventQueue.invokeLater(() -> {
                        frame.setTitle("Calculating vehicle routes " + currentInPercent + "%");
                    }));


            /* initialize the scenario */
            simulation.setAndInitPreparedScenario(scenario);
            simulation.runOneStep();
        } catch (InterruptedException ignored) {
            logger.info("Scenario building interrupted by user");
            scenarioAreaOverlay.setEnabled(true, true, true);
        }


        /* finish creation */
        EventQueue.invokeLater(() -> frame.setTitle(oldTitle));
    }

    private void updateScenario() {
        // todo update scenario although it is not new => activate PrefElements

        if(config.scenario.showAreasWhileSimulating)
            scenarioAreaOverlay.setEnabled(true, false, false);
        else
            scenarioAreaOverlay.setEnabled(false, false, false);
    }

    private void askUserToCancelScenarioBuilding() {
        new Thread(() -> {
            // ask user to cancel scenario building
            Object[] options = { "Yes", "No" };
            int choice = JOptionPane.showOptionDialog(
                    null,
                    "Are you sure to cancel the scenario building?",
                    "Cancel scenario building?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[1]);
            // if yes: cancel
            if (choice == JOptionPane.YES_OPTION)
                cancelScenarioBuilding();
        }).start();
    }

    private void cancelScenarioBuilding() {
        scenarioBuildThread.interrupt();
        try {
            scenarioBuildThread.join();
        } catch (InterruptedException ignored) {}
    }

    /*
    |=============|
    | preferences |
    |=============|
    */
    private void showPreferences() {

        /* set enabled */
        boolean hasStreetgraph = streetgraph != null;
        boolean hasScenario    = simulation.getScenario() != null;

        /* general */
        preferences.setEnabled(PrefElement.sliderSpeedup,         newSim || hasScenario);
        preferences.setEnabled(PrefElement.maxVehicleCount,       newSim);
        preferences.setEnabled(PrefElement.seed,                  newSim);
        preferences.setEnabled(PrefElement.metersPerCell,         newSim);

        /* scenario */
        // todo
        preferences.setEnabled(PrefElement.showAreasWhileSimulating, true);
        preferences.setEnabled(PrefElement.scenarioSelection,     newSim);

        /* crossing logic */
        preferences.setEnabled(PrefElement.edgePriority,          newSim);
        preferences.setEnabled(PrefElement.priorityToThe,         newSim);
        preferences.setEnabled(PrefElement.onlyOneVehicle,        newSim);
        preferences.setEnabled(PrefElement.friendlyStandingInJam, newSim);

        /* visualization */
        preferences.setEnabled(PrefElement.style, true);

        /* concurrency */
        preferences.setEnabled(PrefElement.nThreads,            newSim);
        preferences.setEnabled(PrefElement.vehiclesPerRunnable, true);
        preferences.setEnabled(PrefElement.nodesPerThread,      true);

        /* init values */
        preferences.setSettings(config);

        /* show */
        preferences.setVisible(true);
        preferences.toFront();
    }

    private void closePreferences() {
        preferences.setVisible(false);
        preferences.setAllEnabled(false);
    }
}