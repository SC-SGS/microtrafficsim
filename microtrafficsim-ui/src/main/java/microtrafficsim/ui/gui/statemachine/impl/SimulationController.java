package microtrafficsim.ui.gui.statemachine.impl;

import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.mapviewer.MapViewer;
import microtrafficsim.core.mapviewer.TileBasedMapViewer;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.core.impl.VehicleSimulation;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.input.KeyCommand;
import microtrafficsim.core.vis.simulation.SpriteBasedVehicleOverlay;
import microtrafficsim.core.vis.simulation.VehicleOverlay;
import microtrafficsim.ui.gui.menues.MTSMenuBar;
import microtrafficsim.ui.gui.statemachine.GUIController;
import microtrafficsim.ui.gui.statemachine.GUIEvent;
import microtrafficsim.ui.preferences.IncorrectSettingsException;
import microtrafficsim.ui.preferences.PrefElement;
import microtrafficsim.ui.preferences.impl.PreferencesFrame;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Controls user input and represents an interface between visualization/parsing, simulation and GUI.
 *
 * @author Dominic Parga Cacheiro
 */
public class SimulationController implements GUIController {

    private static final Logger logger = new EasyMarkableLogger(SimulationController.class);

    /* multithreading */
    private final ReentrantLock       lock_parsing;

    private boolean arePrefsActive;
    private boolean newSim;
    /* state marker */
    private boolean isCreated;
    private boolean isTransiating;

    /* general */
    private final SimulationConfig config;

    /* visualization and parsing */
    private final MapViewer      mapviewer;
    private final VehicleOverlay overlay;
    private File                 currentDirectory;
    private StreetGraph          streetgraph;

    /* simulation */
    private Simulation simulation;

    /* gui */
    private final JFrame     frame;
    private final MTSMenuBar menubar;
    private final PreferencesFrame preferences;

    public SimulationController() {
        this("MicroTrafficSim - GUI Example");
    }

    public SimulationController(String frameTitle) {

        /* multithreading */
        lock_parsing = new ReentrantLock();

        /* state marker */
        isCreated      = false;
        arePrefsActive = false;
        newSim         = false;
        isTransiating = false;

        /* general */
        config           = new SimulationConfig();

        /* visualization and parsing */
        mapviewer        = new TileBasedMapViewer();
        overlay          = new SpriteBasedVehicleOverlay(mapviewer.getProjection());
        currentDirectory = new File(System.getProperty("user.dir"));

        /* simulation */
        simulation = new VehicleSimulation();

        /* gui */
        frame       = new JFrame(frameTitle);
        menubar     = new MTSMenuBar();
        preferences = new PreferencesFrame(this);
    }

    /*
    |===================|
    | (i) GUIController |
    |===================|
    */
    @Override
    public void transiate(GUIEvent event, final File file) {
        logger.debug("GUIEvent called = GUIEvent." + event);

        switch (event) {
            case CREATE:
                create();
                show();
                if (file == null)
                    break;
            case LOAD_MAP:
                subtransiate(() -> {
                    lock_parsing.lock();

                    closePreferences();
                    pauseSim();

                    File loadedFile = file == null ? askForMapFile() : file;
                    if (loadedFile != null)
                        parseAndShow(loadedFile);

                    // did parse
                    isTransiating = false;
                    lock_parsing.unlock();
                });
                break;
            case NEW_SIM:
                subtransiate(() -> {
                    if (streetgraph != null) {
                        pauseSim();
                        showPreferences();
                        updateMenuBar();
                    } else {
                        isTransiating = false;
                    }
                });
                break;
            case ACCEPT:
                break;
            case CANCEL:
                closePreferences();
                updateMenuBar();
                break;
            case EDIT_SIM:
                break;
            case RUN_SIM:
                break;
            case RUN_SIM_ONE_STEP:
                break;
            case PAUSE_SIM:
                break;
            case EXIT:
                exit();
                break;
        }

        userTasks.add(this::updateMenuBar);
    }

    /**
     *
     * @param runnable
     * @param locked If true, this method is called using {@link ReentrantLock#tryLock()}
     */
    private void subtransiate(Runnable runnable, boolean locked) {
        // prevent task spamming
        if (lock_parsing.tryLock()) {

            // only one thread should run
            if (!isTransiating) {
                isTransiating = true;
                new Thread(runnable).start();
            }

            lock_parsing.unlock();
        }
    }

    @Override
    public void addKeyCommand(short event, short vk, KeyCommand command) {
        mapviewer.addKeyCommand(event, vk, command);
    }

    private void updateMenuBar() {
        if (!isCreated) return;

        boolean isPaused = simulation.isPaused();

        menubar.menuMap.setEnabled(true);
        menubar.menuMap.itemLoadMap.setEnabled(true);

        menubar.menuLogic.setEnabled(               !arePrefsActive && streetgraph != null);
        menubar.menuLogic.itemRunPause.setEnabled(  !arePrefsActive && streetgraph != null && !isPaused);
        menubar.menuLogic.itemRunOneStep.setEnabled(!arePrefsActive && streetgraph != null &&  isPaused);
        menubar.menuLogic.itemEditSim.setEnabled(   !arePrefsActive && streetgraph != null);
        menubar.menuLogic.itemNewSim.setEnabled(    !arePrefsActive && streetgraph != null);
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

        /* create preferences */
        preferences.create();
        preferences.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        preferences.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                transiate(GUIEvent.CANCEL);
            }
        });
        preferences.pack();
        preferences.setLocationRelativeTo(null);    // center on screen; close to setVisible
        preferences.setVisible(false);

        mapviewer.addOverlay(0, overlay);

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

        /* on close: stop the visualization and exit */
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exit();
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

    private void exit() {
        mapviewer.stop();
        System.exit(0);
    }

    /*
    |================|
    | map and parser |
    |================|
    */
    private File askForMapFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(currentDirectory);
        chooser.setFileFilter(new FileFilter() {

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

        int action = chooser.showDialog(null, "Open");
        currentDirectory = chooser.getCurrentDirectory();
        if (action == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }

        return null;
    }

    private void parseAndShow(File file) {
        String cachedTitle = frame.getTitle();
        frame.setTitle("Parsing new map, please wait...");

        OSMParser.Result result = null;

        try {
            /* parse file and create tiled provider */
            result = mapviewer.parse(file);
        } catch (Exception e) {
            e.printStackTrace();
            Runtime.getRuntime().halt(1);
        }

        if (result != null) {
            if (result.streetgraph != null) {
                cleanupSimulation();
                streetgraph = result.streetgraph;

                try {
                    mapviewer.changeMap(result);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                cachedTitle = "MicroTrafficSim - " + file.getName();
            }
        }

        frame.setTitle(cachedTitle);
    }

    /*
    |============|
    | simulation |
    |============|
    */
    private void pauseSim() {
        menubar.menuLogic.simIsPaused(true);
        simulation.cancel();
    }

    /**
     * You can call this method to cleanup the simulation, which means resetting the streetgraph and removing all
     * scenario data.
     */
    private void cleanupSimulation() {
        if (streetgraph != null) streetgraph.reset();
        simulation.setAndInitScenario(null);
        menubar.menuLogic.simIsPaused(true);
    }

    /*
    |=============|
    | preferences |
    |=============|
    */
    private void showPreferences() {

        /* set enabled */
        // general
        preferences.setEnabled(PrefElement.sliderSpeedup, PrefElement.sliderSpeedup.isEnabled());
        preferences.setEnabled(PrefElement.maxVehicleCount, newSim && PrefElement.maxVehicleCount.isEnabled());
        preferences.setEnabled(PrefElement.seed, newSim && PrefElement.seed.isEnabled());
        preferences.setEnabled(PrefElement.metersPerCell, newSim && PrefElement.metersPerCell.isEnabled());
        // crossing logic
        preferences.setEnabled(PrefElement.edgePriority, newSim && PrefElement.edgePriority.isEnabled());
        preferences.setEnabled(PrefElement.priorityToThe, newSim && PrefElement.priorityToThe.isEnabled());
        preferences.setEnabled(PrefElement.onlyOneVehicle, newSim && PrefElement.onlyOneVehicle.isEnabled());
        preferences.setEnabled(PrefElement.friendlyStandingInJam,
                newSim && PrefElement.friendlyStandingInJam.isEnabled());
        // visualization
        // concurrency
        preferences.setEnabled(PrefElement.nThreads, newSim && PrefElement.nThreads.isEnabled());
        preferences.setEnabled(PrefElement.vehiclesPerRunnable, PrefElement.vehiclesPerRunnable.isEnabled());
        preferences.setEnabled(PrefElement.nodesPerThread, PrefElement.nodesPerThread.isEnabled());

        /* init values */
        preferences.setSettings(config);

        /* show */
        preferences.setVisible(true);
        preferences.toFront();

        /* state marker */
        arePrefsActive = true;
    }

    /**
     * @return True if the settings were correct; false otherwise
     */
    private boolean updateSimulationConfig() {
        try {
            config.update(preferences.getCorrectSettings());
            return true;
        } catch (IncorrectSettingsException e) {
            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage(),
                    "Error: wrong preferences values",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void closePreferences() {
        preferences.setVisible(false);
        preferences.setAllEnabled(false);

        arePrefsActive = false;
    }
}
