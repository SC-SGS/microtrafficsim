package microtrafficsim.ui.gui;


import microtrafficsim.core.frameworks.vehicle.IVisualizationVehicle;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.simulation.Simulation;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.input.KeyCommand;
import microtrafficsim.core.vis.simulation.SpriteBasedVehicleOverlay;
import microtrafficsim.core.vis.simulation.VehicleOverlay;
import microtrafficsim.ui.preferences.IncorrectSettingsException;
import microtrafficsim.ui.preferences.PrefElement;
import microtrafficsim.ui.preferences.impl.PreferencesFrame;
import microtrafficsim.ui.vis.MapViewer;
import microtrafficsim.ui.vis.TileBasedMapViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public class SimulationController implements GUIController {

  private static Logger logger = LoggerFactory.getLogger(SimulationController.class);

  // general
  private GUIState state, previousState;
  private StreetGraph streetgraph;
  // logic
  private final SimulationConfig config;
  private final SimulationConstructor simulationConstructor;
  private Simulation simulation;
  // visualization
  private MapViewer mapviewer;
  private VehicleOverlay overlay;
  private File currentDirectory;
  // frame/gui
  private final JFrame frame;
  private final MTSMenuBar menubar;
  private final ReentrantLock lock_gui;
  // preferences
  private PreferencesFrame preferences;

  public SimulationController(SimulationConstructor simulationConstructor,
                              MapViewer mapviewer) {
    this(   simulationConstructor,
            mapviewer,
            "MicroTrafficSim - GUI Example");
  }

  public SimulationController(SimulationConstructor simulationConstructor,
                              MapViewer mapviewer,
                              String title) {
    super();
    // general
    state = GUIState.RAW;
    previousState = null;
    // logic
    config = new SimulationConfig();
    this.simulationConstructor = simulationConstructor;
    // visualization
    this.mapviewer = mapviewer;
    overlay = new SpriteBasedVehicleOverlay(TileBasedMapViewer.PROJECTION);
    currentDirectory = new File(System.getProperty("user.dir"));
    // frame/gui
    frame = new JFrame(title);
    menubar = new MTSMenuBar();
    lock_gui = new ReentrantLock();
  }

  private void setState(GUIState state) {
    previousState = this.state;
    this.state = state;
  }

  /*
  |===================|
  | (i) GUIController |
  |===================|
  */
  @Override
  public void addKeyCommand(short event, short vk, KeyCommand command) {
    mapviewer.addKeyCommand(event, vk, command);
  }

  @Override
  public void transiate(GUIEvent event) {
    transiate(event, null);
  }

  @Override
  public void transiate(GUIEvent event, File file) {
    logger.debug("GUIState before transiate = GUIState." + state);
    logger.debug("GUIEvent called           = GUIEvent." + event);
    switch (event) {
      case CREATE:
      case LOAD_MAP:
        // map some states on other states for easier handling later
        if (state == GUIState.RAW) {
          create();
          show();
          setState(GUIState.INIT);
        }
        switch (state) {
          case SIM_RUN:
            pauseSim();
            setState(GUIState.SIM_PAUSE);
          case SIM_PAUSE:
          case INIT:
          case MAP: {
            // Load map without file? => have to ask user
            if (file == null && event == GUIEvent.LOAD_MAP) {
              file = askForMapFile();
            }
            // if user don't want to load a file => stop here
            if (file != null) {
              switch (state) {
                case INIT:
                case MAP:
                  setState(GUIState.PARSING);
                  break;
                case SIM_PAUSE:
                  setState(GUIState.PARSING_SIM_PAUSE);
                  break;
              }
              asyncParseAndShow(file);
            }
          }
        }
        updateMenuBar();
        break;
      case DID_PARSE:
        switch (state) {
          case PARSING:
          case PARSING_SIM_RUN:
          case PARSING_SIM_PAUSE:
            state = GUIState.MAP;
        }
        updateMenuBar();
        break;
      case NEW_SIM:
        switch (state) {
          case SIM_RUN:
            pauseSim();
            setState(GUIState.SIM_PAUSE);
          case SIM_PAUSE:
          case MAP:
            setState(GUIState.SIM_NEW);
            showPreferences();
        }
        updateMenuBar();
        break;
      case ACCEPT:
        switch (state) {
          case SIM_EDIT:
            if (updateSimulationConfig()) {
              closePreferences();
              setState(GUIState.SIM_PAUSE);
            }
            break;
          case SIM_NEW:
            if (updateSimulationConfig()) {
              closePreferences();
              cleanupSimulation();
              startNewSimulation();
              setState(GUIState.SIM_PAUSE);
            }
        }
        updateMenuBar();
        break;
      case CANCEL:
        switch (state) {
          case SIM_EDIT:
          case SIM_NEW:
            closePreferences();
            setState(previousState);
        }
        updateMenuBar();
        break;
      case EDIT_SIM:
        switch (state) {
          case SIM_RUN:
            pauseSim();
            setState(GUIState.SIM_PAUSE);
          case SIM_PAUSE:
            setState(GUIState.SIM_EDIT);
            showPreferences();
        }
        updateMenuBar();
        break;
      case RUN_SIM:
        if (lock_gui.tryLock()) {
          switch (state) {
            case SIM_PAUSE:
              runSim();
              setState(GUIState.SIM_RUN);
              break;
            case PARSING_SIM_PAUSE:
              runSim();
              setState(GUIState.PARSING_SIM_RUN);
          }
          updateMenuBar();
          lock_gui.unlock();
        }
        break;
      case RUN_SIM_ONE_STEP:
        if (lock_gui.tryLock()) {
          switch (state) {
            case SIM_RUN:
              pauseSim();
              setState(GUIState.SIM_PAUSE);
            case SIM_PAUSE:
              runSimOneStep();
              break;
            case PARSING_SIM_RUN:
              pauseSim();
              setState(GUIState.PARSING_SIM_PAUSE);
            case PARSING_SIM_PAUSE:
              runSimOneStep();
          }
          updateMenuBar();
          lock_gui.unlock();
        }
        break;
      case PAUSE_SIM:
        if (lock_gui.tryLock()) {
          switch (state) {
            case SIM_RUN:
              pauseSim();
              setState(GUIState.SIM_PAUSE);
              break;
            case PARSING_SIM_RUN:
              pauseSim();
              setState(GUIState.PARSING_SIM_PAUSE);
          }
          updateMenuBar();
          lock_gui.unlock();
        }
        break;
      case EXIT:
        exit();
    }

    logger.debug("GUIState after transiate  = GUIState." + state);
  }

  private void updateMenuBar() {
    switch (state) {
      case RAW: /*---------------------------------------------*/
      case PARSING: /*-----------------------------------------*/
      case SIM_NEW: /*-----------------------------------------*/
      case SIM_EDIT: /*----------------------------------------*/
        menubar.menuMap.setEnabled(                     false);
        menubar.menuMap.itemLoadMap.setEnabled(         false);

        menubar.menuLogic.setEnabled(                   false);
        menubar.menuLogic.itemRunPause.setEnabled(      false);
        menubar.menuLogic.itemRunOneStep.setEnabled(    false);
        menubar.menuLogic.itemEditSim.setEnabled(       false);
        menubar.menuLogic.itemNewSim.setEnabled(        false);
        break;
      case INIT: /*--------------------------------------------*/
        menubar.menuMap.setEnabled(                     true);
        menubar.menuMap.itemLoadMap.setEnabled(         true);

        menubar.menuLogic.setEnabled(                   false);
        menubar.menuLogic.itemRunPause.setEnabled(      false);
        menubar.menuLogic.itemRunOneStep.setEnabled(    false);
        menubar.menuLogic.itemEditSim.setEnabled(       false);
        menubar.menuLogic.itemNewSim.setEnabled(        false);
        break;
      case PARSING_SIM_RUN: /*---------------------------------*/
      case PARSING_SIM_PAUSE: /*-------------------------------*/
        menubar.menuMap.setEnabled(                     false);
        menubar.menuMap.itemLoadMap.setEnabled(         false);

        menubar.menuLogic.setEnabled(                   true);
        menubar.menuLogic.itemRunPause.setEnabled(      true);
        menubar.menuLogic.itemRunOneStep.setEnabled(    true);
        menubar.menuLogic.itemEditSim.setEnabled(       false);
        menubar.menuLogic.itemNewSim.setEnabled(        false);
        break;
      case MAP: /*---------------------------------------------*/
        menubar.menuMap.setEnabled(                     true);
        menubar.menuMap.itemLoadMap.setEnabled(         true);

        menubar.menuLogic.setEnabled(                   true);
        menubar.menuLogic.itemRunPause.setEnabled(      false);
        menubar.menuLogic.itemRunOneStep.setEnabled(    false);
        menubar.menuLogic.itemEditSim.setEnabled(       false);
        menubar.menuLogic.itemNewSim.setEnabled(        true);
        break;
      case SIM_PAUSE: /*---------------------------------------*/
      case SIM_RUN: /*-----------------------------------------*/
        menubar.menuMap.setEnabled(                     true);
        menubar.menuMap.itemLoadMap.setEnabled(         true);

        menubar.menuLogic.setEnabled(                   true);
        menubar.menuLogic.itemRunPause.setEnabled(      true);
        menubar.menuLogic.itemRunOneStep.setEnabled(    true);
        menubar.menuLogic.itemEditSim.setEnabled(       true);
        menubar.menuLogic.itemNewSim.setEnabled(        true);
        break;
    }
  }

  /*
  |=========|
  | general |
  |=========|
  */
  private void create() {
    try {
      mapviewer.create(config);
    } catch (UnsupportedFeatureException e) {
      e.printStackTrace();
    }

    /* create preferences */
    preferences = new PreferencesFrame(this);
    preferences.create();
    preferences.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    preferences.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentHidden(ComponentEvent e) {
        transiate(GUIEvent.CANCEL);
      }
    });
    preferences.pack();
    preferences.setLocationRelativeTo(null); // center on screen; close to setVisible
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
    frame.setSize(TileBasedMapViewer.INITIAL_WINDOW_WIDTH, TileBasedMapViewer.INITIAL_WINDOW_HEIGHT);
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
  }

  private void show() {
    frame.setLocationRelativeTo(null); // center on screen; close to setVisible
    frame.setVisible(true);
    mapviewer.show();
  }

  private void exit() {
    mapviewer.stop();
    System.exit(0);
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

  private void startNewSimulation() {
    String oldTitle = frame.getTitle();
    frame.setTitle("Calculating vehicle routes 0%");

    /* create the simulation */
    simulation = simulationConstructor.instantiate(config, streetgraph, overlay.getVehicleFactory());
    overlay.setSimulation(simulation);

    /* initialize the simulation */
    simulation.prepare(currentInPercent -> frame.setTitle("Calculating vehicle routes " + currentInPercent + "%"));
    simulation.runOneStep();
    frame.setTitle(oldTitle);
  }

  private void cleanupSimulation() {
    if (streetgraph != null)
      streetgraph.reset();
    overlay.setSimulation(null);
    simulation = null;
    menubar.menuLogic.simIsPaused(true);
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
      public boolean accept(File f) {
        if (f.isDirectory()) return true;

        String extension = null;

        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1)
          extension = s.substring(i+1).toLowerCase();

        if (extension == null) return false;

        switch (extension) {
          case "osm":		return true;
          default:		return false;
        }
      }
    });

    int action = chooser.showOpenDialog(null);

    currentDirectory = chooser.getCurrentDirectory();
    if (action == JFileChooser.APPROVE_OPTION)
      return chooser.getSelectedFile();

    return null;
  }

  private void asyncParseAndShow(File file) {
    new Thread(() -> {
      String oldTitle = frame.getTitle();
      frame.setTitle("Parsing new map, please wait...");

      try {
      /* parse file and create tiled provider */
        OSMParser.Result result = mapviewer.parse(file);

        lock_gui.lock();
        transiate(GUIEvent.DID_PARSE);
        lock_gui.unlock(); // todo N = new sim and resetView centers view
        cleanupSimulation();
        streetgraph = result.streetgraph;

        mapviewer.changeMap(result);
        frame.setTitle("MicroTrafficSim - " + file.getName());
      } catch (XMLStreamException | IOException | InterruptedException e) {
        lock_gui.lock();
        transiate(GUIEvent.DID_PARSE);
        lock_gui.unlock();
        frame.setTitle(oldTitle);
        e.printStackTrace();
        Runtime.getRuntime().halt(1);
      }
    }).start();
  }

  /*
  |=============|
  | preferences |
  |=============|
  */
  private void showPreferences() {
    boolean newSim = state == GUIState.SIM_NEW;

    /* set enabled */
    // general
    preferences.setEnabled(PrefElement.sliderSpeedup,                   PrefElement.sliderSpeedup.isEnabled());
    preferences.setEnabled(PrefElement.ageForPause,                     PrefElement.ageForPause.isEnabled());
    preferences.setEnabled(PrefElement.maxVehicleCount,       newSim && PrefElement.maxVehicleCount.isEnabled());
    preferences.setEnabled(PrefElement.seed,                  newSim && PrefElement.seed.isEnabled());
    preferences.setEnabled(PrefElement.metersPerCell,         newSim &&  PrefElement.metersPerCell.isEnabled());
    // crossing logic
    preferences.setEnabled(PrefElement.edgePriority,          newSim && PrefElement.edgePriority.isEnabled());
    preferences.setEnabled(PrefElement.priorityToThe,         newSim && PrefElement.priorityToThe.isEnabled());
    preferences.setEnabled(PrefElement.onlyOneVehicle,        newSim && PrefElement.onlyOneVehicle.isEnabled());
    preferences.setEnabled(PrefElement.friendlyStandingInJam, newSim && PrefElement.friendlyStandingInJam.isEnabled());
    // visualization
    preferences.setEnabled(PrefElement.projection,                      PrefElement.projection.isEnabled());
    // concurrency
    preferences.setEnabled(PrefElement.nThreads,              newSim && PrefElement.nThreads.isEnabled());
    preferences.setEnabled(PrefElement.vehiclesPerRunnable,             PrefElement.vehiclesPerRunnable.isEnabled());
    preferences.setEnabled(PrefElement.nodesPerThread,                  PrefElement.nodesPerThread.isEnabled());

    /* init values */
    preferences.setSettings(config);

    /* show */
    preferences.setVisible(true);
    preferences.toFront();
  }

  private boolean updateSimulationConfig() {
    try {
      config.update(preferences.getCorrectSettings());
      return true;
    } catch (IncorrectSettingsException e) {
      JOptionPane.showMessageDialog(null,
              e.getMessage(),
              "Error: wrong preferences values",
              JOptionPane.ERROR_MESSAGE);
      return false;
    }
  }

  private void closePreferences() {
    preferences.setVisible(false);
    preferences.setAllEnabled(false);
  }

  /*
  |=======|
  | stuff |
  |=======|
  */
  /**
   * This interface gives the opportunity to call the constructor of {@link SimulationController} with a parameter, that
   * is the constructor of the used Simulation.
   */
  public interface SimulationConstructor {
    Simulation instantiate(SimulationConfig config,
                           StreetGraph streetgraph,
                           Supplier<IVisualizationVehicle> vehicleFactory);
  }
}