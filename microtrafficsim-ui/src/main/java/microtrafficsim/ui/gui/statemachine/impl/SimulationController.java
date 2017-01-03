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
import microtrafficsim.ui.preferences.impl.PreferencesFrame;
import microtrafficsim.utils.concurrency.executorservices.OrderedTaskExecutor;
import microtrafficsim.utils.concurrency.executorservices.impl.SingleThreadedOrderedTaskExecutor;
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
    private final OrderedTaskExecutor userTasks;
    private final ReentrantLock lock_parsing;

    /* state marker */
    private boolean isCreated;

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
        userTasks    = new SingleThreadedOrderedTaskExecutor();
        lock_parsing = new ReentrantLock();

        /* state marker */
        isCreated = false;

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
                userTasks.add(() -> {
                    create();
                    show();
                });
                if (file == null)
                    break;
            case LOAD_MAP:
                userTasks.add(() -> {
                    if (lock_parsing.tryLock()){
                        pauseSim();
                        File loadedFile = file;
                        if (loadedFile == null)
                            loadedFile = askForMapFile();
                        if (loadedFile != null) {
                            parseAndShow(loadedFile);
                        } else {
                            lock_parsing.unlock();
                        }
                    }
                });
                break;
            case DID_PARSE:
                userTasks.add(() -> {
                    lock_parsing.unlock();
                });
                break;
            case NEW_SIM:
                break;
            case ACCEPT:
                break;
            case CANCEL:
                userTasks.add(() -> {
                    lock_parsing.unlock();
                });
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
                userTasks.add(this::exit);
                break;
        }

        userTasks.add(this::updateMenuBar);
    }

    @Override
    public void addKeyCommand(short event, short vk, KeyCommand command) {
        mapviewer.addKeyCommand(event, vk, command);
    }

    private void updateMenuBar() {
        if (!isCreated) return;

        menubar.menuMap.setEnabled(true);
        menubar.menuMap.itemLoadMap.setEnabled(true);

        menubar.menuLogic.setEnabled(false);
        menubar.menuLogic.itemRunPause.setEnabled(false);
        menubar.menuLogic.itemRunOneStep.setEnabled(false);
        menubar.menuLogic.itemEditSim.setEnabled(false);
        menubar.menuLogic.itemNewSim.setEnabled(false);
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

        int action = chooser.showDialog(null, "Öffnen");
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
        transiate(GUIEvent.DID_PARSE);
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
}
