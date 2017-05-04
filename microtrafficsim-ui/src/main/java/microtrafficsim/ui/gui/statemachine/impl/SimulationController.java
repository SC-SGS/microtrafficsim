package microtrafficsim.ui.gui.statemachine.impl;

import microtrafficsim.core.convenience.DefaultParserConfig;
import microtrafficsim.core.convenience.TileBasedMapViewer;
import microtrafficsim.core.convenience.filechoosing.ConfigFileChooser;
import microtrafficsim.core.convenience.filechoosing.MTSFileChooser;
import microtrafficsim.core.convenience.filechoosing.MapfileChooser;
import microtrafficsim.core.convenience.filechoosing.ScenarioFileChooser;
import microtrafficsim.core.convenience.utils.FileFilters;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.base.ScenarioMetaInfo;
import microtrafficsim.core.exfmt.exceptions.NotAvailableException;
import microtrafficsim.core.exfmt.extractor.map.QuadTreeTiledMapSegmentExtractor;
import microtrafficsim.core.exfmt.extractor.simulation.AreaScenarioExtractor;
import microtrafficsim.core.exfmt.extractor.streetgraph.StreetGraphExtractor;
import microtrafficsim.core.exfmt.injector.simulation.AreaScenarioInjector;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streetgraph.StreetGraph;
import microtrafficsim.core.map.MapSegment;
import microtrafficsim.core.map.TileFeatureProvider;
import microtrafficsim.core.map.tiles.QuadTreeTiledMapSegment;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.serialization.ExchangeFormatSerializer;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.scenarios.impl.AreaScenario;
import microtrafficsim.core.simulation.scenarios.impl.EndOfTheWorldScenario;
import microtrafficsim.core.simulation.scenarios.impl.RandomRouteScenario;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.input.KeyCommand;
import microtrafficsim.core.vis.scenario.areas.ScenarioAreaOverlay;
import microtrafficsim.core.vis.simulation.VehicleOverlay;
import microtrafficsim.ui.gui.menues.MTSMenuBar;
import microtrafficsim.ui.gui.statemachine.GUIController;
import microtrafficsim.ui.gui.statemachine.GUIEvent;
import microtrafficsim.ui.gui.utils.FrameTitle;
import microtrafficsim.ui.preferences.IncorrectSettingsException;
import microtrafficsim.ui.preferences.model.PrefElement;
import microtrafficsim.ui.preferences.view.PreferencesFrame;
import microtrafficsim.utils.collections.Composite;
import microtrafficsim.utils.functional.Procedure;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import microtrafficsim.utils.strings.WrappedString;
import org.slf4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
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
    private final ReentrantLock lockTransition;

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
    private final TileBasedMapViewer mapviewer;
    private final VehicleOverlay     overlay;
    private ScenarioAreaOverlay      scenarioAreaOverlay;
    private OSMParser                parser;
    private ExchangeFormat           exfmt;
    private ExchangeFormatSerializer serializer;

    private Graph               streetgraph;

    /* simulation */
    private Simulation      simulation;
    private ScenarioBuilder scenarioBuilder;

    /* gui */
    private final JFrame                    frame;
    private final MTSMenuBar                menubar;
    private final PreferencesFrame          preferences;
    private final Composite<MTSFileChooser> fileChoosers;

    public SimulationController() {
        this(new BuildSetup());
    }

    public SimulationController(BuildSetup buildSetup) {

        /* multithreading */
        isExecutingUserTask = new AtomicBoolean(false);
        lockTransition = new ReentrantLock();
        isParsing           = new AtomicBoolean(false);
        isBuildingScenario  = new AtomicBoolean(false);

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
        frame            = new JFrame(FrameTitle.DEFAULT.get());
        menubar          = new MTSMenuBar();
        preferences      = new PreferencesFrame(this);


        /* file chooser */
        fileChoosers = new Composite<>();
        fileChoosers.set(MapfileChooser.class,      new MapfileChooser());
        fileChoosers.set(ScenarioFileChooser.class, new ScenarioFileChooser());
        fileChoosers.set(ConfigFileChooser.class, new ConfigFileChooser());
        for (MTSFileChooser chooser : fileChoosers.getAll().values()) {
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        }

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


        /* create exchange format and serializer */
        serializer = ExchangeFormatSerializer.create();
        exfmt = ExchangeFormat.getDefault();

        exfmt.getConfig().set(QuadTreeTiledMapSegmentExtractor.Config.getDefault(
                mapviewer.getPreferredTilingScheme(), mapviewer.getPreferredTileGridLevel()));

        exfmt.getConfig().set(new StreetGraphExtractor.Config(config));


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
        SwingUtilities.invokeLater(() -> scenarioAreaOverlay.setEnabled(false, false, false));
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
        frame.add(mapviewer.getVisualizationPanel(), BorderLayout.CENTER);


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
        if (!lockTransition.tryLock())
            return;

        switch (event) {
            /* map */
            case LOAD_MAP:
                transitionLoadMap(file);
                break;
            case SAVE_MAP:
                transitionSaveMap();
                break;
            /* simulation */
            case RUN_SIM:
                transitionRunSim();
                break;
            case RUN_SIM_ONE_STEP:
                transitionRunSimOneStep();
                break;
            case PAUSE_SIM:
                transitionPauseSim();
                break;
            case NEW_SCENARIO:
                transitionNewScenario();
                break;
            case EDIT_SCENARIO:
                transitionEditScenario();
                break;
            case CHANGE_AREA_SELECTION:
                transitionChangeAreaSelection();
                break;
            /* load/save scenario */
            case LOAD_CONFIG:
                transitionLoadConfig();
                break;
            case SAVE_CONFIG:
                transitionSaveConfig();
                break;
            case LOAD_ROUTES:
                transitionLoadRoutes();
                break;
            case SAVE_ROUTES:
                transitionSaveRoutes();
                break;
            case LOAD_AREAS:
                transitionLoadAreas();
                break;
            case SAVE_AREAS:
                transitionSaveAreas();
                break;
            /* preferences */
            case ACCEPT_PREFS:
                transitionAcceptPreferences();
                break;
            case CANCEL_PREFS:
                transitionCancelPreferences();
                break;
        }

        lockTransition.unlock();
    }


    /* map */
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

                File loadedFile = file == null ? askForOpenMapfile() : file;
                if (isFileOkayForLoading(loadedFile))
                    loadAndShowMap(loadedFile);

                isParsing.set(false);
                updateMenuBar();
                isExecutingUserTask.set(false);
            });
            parsingThread.start();
        }
    }

    private void transitionSaveMap() {
        if (isExecutingUserTask.compareAndSet(false, true)) {
            new Thread(() -> {
                closePreferences();
                pauseSim();

                File file = askForSaveMapfile();
                if (isFileOkayForSaving(file))
                    saveMap(file);

                updateMenuBar();
                isExecutingUserTask.set(false);
            }).start();
        }
    }


    /* simulation */
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

    private void transitionNewScenario() {
        if (isBuildingScenario.get()) {
            askUserToCancelScenarioBuilding();
        } else if (isExecutingUserTask.compareAndSet(false, true)) { // unlock after accept/cancel
            new Thread(() -> {
                if (streetgraph != null) {
                    pauseSim();
                    newSim = true;
                    showPreferences();

                    updateMenuBar();
                } else
                    isExecutingUserTask.set(false);
            }).start();
        }
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

    private void transitionChangeAreaSelection() {
        /* goal in this method: enable scenario area overlay */

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
                        if (choice == JOptionPane.YES_OPTION) {
                            pauseSim();
                            simulation.removeCurrentScenario();
                        } else
                            enableScenarioAreaOverlay = false;
                    }

                    scenarioAreaOverlay.setEventsEnabled(enableScenarioAreaOverlay);
                }

                isExecutingUserTask.set(false);
            }).start();
        }
    }


    /* load/save scenario */
    private void transitionLoadConfig() {
        // todo
        preferences.requestAnExecutionThread(() -> {

        });
    }

    private void transitionSaveConfig() {
        preferences.requestAnExecutionThread(() -> {
            File file = askForSaveConfigfile();
            if (isFileOkayForSaving(file))
                saveConfig(file);

            preferences.toFront();
            updateMenuBar();
            preferences.hasFinishedProcedureExecution();
        }).start();
    }

    private void transitionLoadRoutes() {
        // todo
    }

    private void transitionSaveRoutes() {
        // todo
    }

    private void transitionLoadAreas() {
        // todo
    }

    private void transitionSaveAreas() {
        // todo
    }


    /* preferences */
    private void transitionAcceptPreferences() {
        Thread thread = preferences.requestAnExecutionThread(() -> {
            if (!isBuildingScenario.compareAndSet(false, true)) {
                return;
            }


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
            preferences.hasFinishedProcedureExecution();
        });

        if (thread != null) {
            scenarioBuildThread = thread;
            scenarioBuildThread.start();
        }
    }

    private void transitionCancelPreferences() {
        preferences.requestAnExecutionThread(() -> {
            closePreferences();
            updateMenuBar();

            isExecutingUserTask.set(false);
            preferences.hasFinishedProcedureExecution();
        }).start();
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
    private void rememberCurrentFrameTitleIn(WrappedString cache) {
        SwingUtilities.invokeLater(() -> cache.set(frame.getTitle()));
    }

    private void updateFrameTitle(FrameTitle type, File file) {
        SwingUtilities.invokeLater(() -> frame.setTitle(type.get(file)));
    }

    private void updateFrameTitle(WrappedString newTitle) {
        SwingUtilities.invokeLater(() -> frame.setTitle(newTitle.get()));
    }

    private void updateMenuBar() {
        if (!isCreated) return;

        boolean hasStreetgraph = streetgraph != null;
        boolean hasScenario    = simulation.getScenario() != null;
        boolean hasAreas       = !scenarioAreaOverlay.getAreas().isEmpty();

        menubar.menuMap.setEnabled(true);
        menubar.menuMap.itemLoadMap.setEnabled(true);
        menubar.menuMap.itemSaveMap.setEnabled(hasStreetgraph);

        menubar.menuLogic.setEnabled(true);
        menubar.menuLogic.itemRunPause.setEnabled(hasStreetgraph && hasScenario);
        menubar.menuLogic.itemRunOneStep.setEnabled(hasStreetgraph && hasScenario);
        menubar.menuLogic.itemEditSim.setEnabled(true);
        menubar.menuLogic.itemNewSim.setEnabled(hasStreetgraph);
        menubar.menuLogic.itemChangeAreaSelection.setEnabled(hasStreetgraph);

        menubar.menuLogic.itemLoadRoutes.setEnabled(hasStreetgraph);
        menubar.menuLogic.itemSaveRoutes.setEnabled(hasStreetgraph && hasScenario);
        menubar.menuLogic.itemLoadAreas.setEnabled(hasStreetgraph);
        menubar.menuLogic.itemSaveAreas.setEnabled(hasStreetgraph && hasAreas);
    }


    /*
    |================|
    | map and parser |
    |================|
    */
    private File askForOpenMapfile() {
        return askForOpenFile(fileChoosers.get(MapfileChooser.class));
    }

    private File askForSaveMapfile() {
        MapfileChooser chooser = fileChoosers.get(MapfileChooser.class);

        String filename = chooser.getSelectedFile().getName();
        filename = filename.substring(0, filename.lastIndexOf('.'));
        filename += "." + FileFilters.MAP_EXFMT_POSTFIX;

        return askForSaveFile(chooser, filename);
    }

    private void loadAndShowMap(File file) {
        /* update frame title and remember old one */
        WrappedString cachedTitle = new WrappedString();
        rememberCurrentFrameTitleIn(cachedTitle);
        updateFrameTitle(FrameTitle.LOADING, file);
        Procedure setNewFrameTitle = () -> updateFrameTitle(cachedTitle);


        /* parse/load map */
        boolean success = false;
        if (FileFilters.MAP_OSM_XML.accept(file))
            success = parseAndUpdate(file);
        else if (FileFilters.MAP_EXFMT.accept(file)) {
            success = loadAndUpdate(file);
        }


        /* show map */
        if (success) {
            simulation.removeCurrentScenario();
            overlay.setEnabled(true);
            scenarioAreaOverlay.setEnabled(true, true, false);

            setNewFrameTitle = () -> updateFrameTitle(FrameTitle.DEFAULT, file);
        } else {
            JOptionPane.showMessageDialog(
                    frame,
                    "The chosen file '" + file.getName() + "' has a wrong format.\n" +
                            "Therefore it could be neither loaded nor parsed.\n" +
                            "Please make sure this file exists and is a valid OSM XML or MTS binary file.",
                    "Error: wrong map-file format",
                    JOptionPane.ERROR_MESSAGE);
        }

        setNewFrameTitle.invoke();
    }

    /**
     * Parses the given file and updates all attributes influenced by the file, e.g. the graph
     *
     * @return true, if parsing was successful; false otherwise
     */
    private boolean parseAndUpdate(File file) {
        updateFrameTitle(FrameTitle.PARSING, file);

        OSMParser.Result result = null;

        try {
            /* parse file and create tiled provider */
            result = parser.parse(file);
        } catch (InterruptedException e) {
            logger.info("Parsing interrupted by user");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (result != null) {
            if (result.streetgraph != null) {
                try {
                    mapviewer.setMap(result.segment);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                streetgraph = result.streetgraph;

                return true;
            }
        }

        return false;
    }

    /**
     * Loads the given file and updates all attributes influenced by the file, e.g. the graph
     *
     * @return true, if loading was successful; false otherwise
     */
    private boolean loadAndUpdate(File file) {
        ExchangeFormat.Manipulator manipulator = null;

        try {
            manipulator = exfmt.manipulator(serializer.read(file));
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (manipulator != null) {
            try {
                try {
                    TileFeatureProvider newMap = manipulator.extract(QuadTreeTiledMapSegment.class);
                    mapviewer.setMap(newMap);
                } catch (NotAvailableException e) { // thrown when no TileGrid available
                    MapSegment segment = manipulator.extract(MapSegment.class);
                    mapviewer.setMap(segment);
                }

                streetgraph = manipulator.extract(StreetGraph.class);

                return true;
            } catch (InterruptedException e) {
                logger.info("Loading interrupted");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return false;
    }

    private void cancelParsing() {
        parsingThread.interrupt();
        try {
            parsingThread.join();
        } catch (InterruptedException ignored) {}
    }

    private void saveMap(File file) {
        /* update frame title and remember old one */
        WrappedString cachedTitle = new WrappedString();
        rememberCurrentFrameTitleIn(cachedTitle);
        updateFrameTitle(FrameTitle.SAVING, file);
        Procedure setNewFrameTitle = () -> updateFrameTitle(cachedTitle);


        try {
            serializer.write(file, exfmt.manipulator()
                    .inject(mapviewer.getMap())
                    .inject(streetgraph)
                    .getContainer());

            showSavingSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            showSavingFailure(file);
        }

        setNewFrameTitle.invoke();
    }


    /*
    |==================|
    | load/save config |
    |==================|
    */
    private File askForOpenConfigfile() {
        return askForOpenFile(fileChoosers.get(ConfigFileChooser.class));
    }

    private File askForSaveConfigfile() {
        return askForSaveFile(
                fileChoosers.get(ConfigFileChooser.class),
                "New config file." + FileFilters.CONFIG_POSTFIX
        );
    }

    private void loadConfig(File file) {
        // todo
    }

    private void saveConfig(File file) {
        // todo
        SimulationConfig config = new SimulationConfig(this.config);

        try {
            serializer.write(file, exfmt.manipulator()
                    .inject(config)
                    .getContainer());
            showSavingSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            showSavingFailure(file);
        }
    }


    /*
    |====================|
    | load/save scenario |
    |====================|
    */
    private File askForScenarioSaveFile() {
        return askForSaveFile(
                fileChoosers.get(ScenarioFileChooser.class),
                "New scenario." + FileFilters.SCENARIO_POSTFIX
        );
    }

    private void loadScenario(File file) {
        /* update frame title and remember old one */
        WrappedString cachedTitle = new WrappedString();
        rememberCurrentFrameTitleIn(cachedTitle);
        updateFrameTitle(FrameTitle.LOADING, file);

        WrappedString tmpTitle = new WrappedString("Loading new scenario");
        updateFrameTitle(tmpTitle);


        /* update streetgraph */
        // mapviewer.createParser(config) is not needed because the mapviewer gets the final config-reference
        streetgraph.reset();
        streetgraph.setSeed(config.seed);


        /* prepare exfmt config */
        AreaScenarioExtractor.Config asecfg = new AreaScenarioExtractor.Config();
        asecfg.loadRoutes = askUserForDecision(
                "Do you like to load the routes as well?",
                "Route storing");
        asecfg.graph = streetgraph;
        asecfg.config = config;

        asecfg.scenarioBuilder = scenarioBuilder;
        asecfg.progressListener = currentInPercent -> {
            tmpTitle.set("Assigning vehicle routes " + currentInPercent + "%");
            updateFrameTitle(tmpTitle);
        };
        exfmt.getConfig().set(asecfg);


        /* load */
        ExchangeFormat.Manipulator manipulator = null;
        try {
            manipulator = exfmt.manipulator(serializer.read(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ScenarioMetaInfo scmeta = null;
        if (manipulator != null) {
            try {
                scmeta = manipulator.extract(ScenarioMetaInfo.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        boolean stillLoadScenario = scmeta != null;
        if (stillLoadScenario) {
            // check if streetgraphGUID equals scmeta.getGUID()
            // if not issue warning due to possible incompatibility
            // and prompt to cancel
            if (!streetgraph.getGUID().equals(scmeta.getGraphGUID())) {
                stillLoadScenario = askUserForDecision(
                        "The graph used in the scenario is different to the current one.\n" +
                                "Do you want to continue?",
                        "Inconsistent scenario meta information");
            }
        }

        if (stillLoadScenario) {
            AreaScenario newScenario = null;
            try {
                newScenario = manipulator.extract(AreaScenario.class);
            } catch (Exception e) {
                e.printStackTrace();
            }


            if (newScenario != null) {
                /* create new scenario */
                tmpTitle.set("Assigning vehicle routes 0%");
                updateFrameTitle(tmpTitle);


                /* remove old scenario */
                simulation.removeCurrentScenario();
                scenarioAreaOverlay.setEventsEnabled(false);
                scenarioAreaOverlay.setPropertiesVisible(false);


                /* update area overlay */
                scenarioAreaOverlay.removeAllAreas();
                newScenario.getAreas().stream()
                        .map(area -> area.getProjectedArea(mapviewer.getProjection(), area.getType()))
                        .forEach(scenarioAreaOverlay::add);

                /* initialize the scenario */
                simulation.setAndInitPreparedScenario(newScenario);
                simulation.runOneStep();
            }
        }


        /* finish creation */
        updateFrameTitle(cachedTitle);
    }

    private void saveScenario(File file) {
        /* update frame title and remember old one */
        WrappedString cachedTitle = new WrappedString();
        rememberCurrentFrameTitleIn(cachedTitle);
        updateFrameTitle(FrameTitle.SAVING, file);
        Procedure setNewFrameTitle = () -> updateFrameTitle(cachedTitle);


        AreaScenario scenario = (AreaScenario) simulation.getScenario();
        AreaScenarioInjector.Config asicfg = new AreaScenarioInjector.Config();
        asicfg.storeRoutes = askUserForDecision(
                "Do you like to store the routes as well?\n" +
                        "\n" +
                        "Attention! The routes would be stored\n" +
                        "in the current simulation state,\n" +
                        "not fresh calculated!",
                "Route storing");
        exfmt.getConfig().set(asicfg);
        try {
            serializer.write(file, exfmt.manipulator().inject(scenario).getContainer());

            showSavingSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            showSavingFailure(file);
        }


        setNewFrameTitle.invoke();
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

        WrappedString cachedTitle = new WrappedString();
        rememberCurrentFrameTitleIn(cachedTitle);

        WrappedString tmpTitle = new WrappedString("Starting new scenario");
        updateFrameTitle(tmpTitle);


        /* update streetgraph */
        // mapviewer.createParser(config) is not needed because the mapviewer gets the final config-reference
        streetgraph.reset();
        streetgraph.setSeed(config.seed);


        /* create new scenario */
        tmpTitle.set("Calculating vehicle routes 0%");
        updateFrameTitle(tmpTitle);


        /* remove old scenario */
        simulation.removeCurrentScenario();
        scenarioAreaOverlay.setEventsEnabled(false);
        scenarioAreaOverlay.setPropertiesVisible(false);


        /* create and prepare new scenario */
        AreaScenario scenario;
        if (config.scenario.selectedClass.getObj() == AreaScenario.class) {
            scenario = new AreaScenario(config.seed, config, streetgraph);
            /* get areas from overlay */
            scenarioAreaOverlay.getAreas().stream()
                    .map(area -> area.getUnprojectedArea(mapviewer.getProjection()))
                    .forEach(scenario::addArea);
            scenario.refillNodeLists();
        } else if (config.scenario.selectedClass.getObj() == EndOfTheWorldScenario.class) {
            scenario = new EndOfTheWorldScenario(config.seed, config, streetgraph);
        } else {
            if (config.scenario.selectedClass.getObj() != RandomRouteScenario.class)
                logger.error(
                        "Chosen scenario could not be found. " +
                        RandomRouteScenario.class.getSimpleName() + " is used instead.");
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
                    currentInPercent -> {
                        tmpTitle.set("Calculating vehicle routes " + currentInPercent + "%");
                        updateFrameTitle(tmpTitle);
                    });


            /* initialize the scenario */
            simulation.setAndInitPreparedScenario(scenario);
            simulation.runOneStep();
        } catch (InterruptedException ignored) {
            logger.info("Scenario building interrupted by user");
            scenarioAreaOverlay.setEnabled(true, true, true);
        }


        /* finish creation */
        updateFrameTitle(cachedTitle);
    }

    private void updateScenario() {
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
        preferences.setEnabled(PrefElement.sliderSpeedup,   true);
        preferences.setEnabled(PrefElement.maxVehicleCount, newSim);
        preferences.setEnabled(PrefElement.seed,            newSim);
        preferences.setEnabled(PrefElement.metersPerCell,   newSim);

        /* scenario */
        preferences.setEnabled(PrefElement.showAreasWhileSimulating,  true);
        preferences.setEnabled(PrefElement.nodesAreWeightedUniformly, newSim);
        preferences.setEnabled(PrefElement.scenarioSelection,         newSim);

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


    /*
    |=======|
    | utils |
    |=======|
    */
    private File askForOpenFile(MTSFileChooser chooser) {
        int action = chooser.showOpenDialog(frame);
        if (action == JFileChooser.APPROVE_OPTION)
            return chooser.getSelectedFile();

        return null;
    }

    private File askForSaveFile(MTSFileChooser chooser, String defaultFilename) {
        chooser.setSelectedFile(new File(defaultFilename));

        int action = chooser.showSaveDialog(frame);
        if (action == JFileChooser.APPROVE_OPTION)
            return chooser.getSelectedFile();

        return null;
    }

    private boolean isFileOkayForLoading(File file) {
        return file != null;
    }

    private boolean isFileOkayForSaving(File file) {
        if (file == null)
            return false;
        if (file.exists()) {
            return askUserForDecision(
                    "The selected file already exists. Continue?",
                    "Save File");
        }
        return true;
    }

    private boolean askUserForDecision(String msg, String title) {
        int status = JOptionPane.showConfirmDialog(frame, msg, title, JOptionPane.OK_CANCEL_OPTION);

        return status == JOptionPane.OK_OPTION;
    }

    private void showSavingSuccess() {
        JOptionPane.showMessageDialog(frame,
                "File saved.",
                "Saving file successful",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSavingFailure(File file) {
        JOptionPane.showMessageDialog(frame,
                "Failed to save file: '" + file.getPath() + "'",
                "Error saving file",
                JOptionPane.ERROR_MESSAGE);
    }
}
