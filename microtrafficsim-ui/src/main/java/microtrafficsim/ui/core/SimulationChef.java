package microtrafficsim.ui.core;

import com.jogamp.newt.event.KeyEvent;
import microtrafficsim.core.frameworks.vehicle.IVisualizationVehicle;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.simulation.controller.Simulation;
import microtrafficsim.core.simulation.controller.configs.SimulationConfig;
import microtrafficsim.core.simulation.layers.LayerSupplier;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.VisualizationPanel;
import microtrafficsim.core.vis.VisualizerConfig;
import microtrafficsim.core.vis.input.KeyCommand;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.map.segments.SegmentLayerProvider;
import microtrafficsim.core.vis.segmentbased.SegmentBasedVisualization;
import microtrafficsim.core.vis.simulation.SpriteBasedVehicleOverlay;
import microtrafficsim.ui.preferences.ComponentId;
import microtrafficsim.ui.preferences.impl.*;
import microtrafficsim.ui.utils.Utils;

import javax.swing.*;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public class SimulationChef {

    public static final boolean PRINT_FRAME_STATS = false;

    public static final int WINDOW_WIDTH = 1600;
    public static final int WINDOW_HEIGHT = 900;
    public static final int MSAA = 0;
    public static final int NUM_SEGMENT_WORKERS = 2;

    // general
    SimulationConfig config;
    private BusyClosure sync; // synchronized
    private PrefFrame preferences;
    private final Object lock_preferences = new Object();
    // visualization
    private LayerSupplier layerSupplier;
    private VisualizationPanel vpanel;
    private SegmentBasedVisualization visualization;
    private SpriteBasedVehicleOverlay overlay;
    private OSMParser parser;
    // vehicle simulation
    private SimulationConstructor simulationConstructor;
    private Simulation sim;
    private StreetGraph streetgraph;
    // jframe
    private JFrame jframe;
    private JPanel topPanel;

    /**
     * This interface gives the opportunity to call the constructor of {@link SimulationChef} with a parameter, that
     * is the constructor of the used Simulation.
     */
    public interface SimulationConstructor {
        Simulation instantiate(SimulationConfig config,
                               StreetGraph streetgraph,
                               Supplier<IVisualizationVehicle> vehicleFactory);
    }
    public interface LayerSupplierConstructor {
        LayerSupplier instantiate();
    }
    private interface BusyClosure {
        /**
         * @return previous value (unchanged if true);
         */
        boolean getBusy();
        void getUnbusy();
    }

    public SimulationChef(SimulationConstructor simulationConstructor,
                          LayerSupplierConstructor layerSupplierConstructor) {
        this(
                simulationConstructor,
                layerSupplierConstructor,
                "MicroTrafficSim - OSM MapViewer Example");
    }

    public SimulationChef(SimulationConstructor simulationConstructor,
                          LayerSupplierConstructor layerSupplierConstructor,
                          String title) {

        super();
        jframe = new JFrame(title);
        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        jframe.add(topPanel, BorderLayout.NORTH);

        config = new SimulationConfig();
        this.simulationConstructor = simulationConstructor;
        this.layerSupplier = layerSupplierConstructor.instantiate();

        /* is busy */
        class BusyClosureImpl implements BusyClosure {

            private boolean isBusy;

            @Override
            public synchronized boolean getBusy() {
                if (!isBusy) {
                    boolean old = isBusy;
                    isBusy = true;
                    return false;
                }
                return true;
            }

            @Override
            public synchronized void getUnbusy() {
                isBusy = false;
            }
        }
        sync = new BusyClosureImpl();
    }

    private boolean isVisible() {
        return vpanel != null;
    }

    /*
    |=================|
    | general actions |
    |=================|
    */
    public void createAndShow() {
        createAndShow(null);
    }

    public void createAndShow(File file) {

        if(!sync.getBusy()) {

            /* setup JFrame and visualization */
            prepareVisualization();

            JToolBar toolbar = new JToolBar("Menu");
            toolbar.add(new MTSMenuBar(this).create());
            addToTopBar(toolbar);

            /* parse file */
            if (file != null)
                asyncParse(file);

            if (!isVisible()) {

                /* create and initialize the VisualizationPanel and JFrame */
                try {
                    vpanel = createVisualizationPanel(visualization);
                } catch (UnsupportedFeatureException e) {
                    e.printStackTrace();
                    Runtime.getRuntime().halt(0);
                    return;
                }
                jframe.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
                jframe.add(vpanel, BorderLayout.CENTER);

                /*
                 * Note: JOGL automatically calls glViewport, we need to make sure that this
                 * function is not called with a height or width of 0! Otherwise the program
                 * crashes.
                 */
                jframe.setMinimumSize(new Dimension(100, 100));

                // on close: stop the visualization and exit
                jframe.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        exit();
                    }
                });

                /* show frame and start visualization */
                jframe.setLocationRelativeTo(null); // center on screen; close to setVisible
                jframe.setVisible(true);
                vpanel.start();

                if (PRINT_FRAME_STATS)
                    visualization.getRenderContext().getAnimator().setUpdateFPSFrames(60, System.out);

            }
            sync.getUnbusy();
        }
    }

    private void exit() {

        if (vpanel != null)
            vpanel.stop();
        System.exit(0);
    }

    /*
    |=========|
    | parsing |
    |=========|
    */
    void asyncParse(File file) {

        if (isVisible())
            if (!sync.getBusy()) {

                new Thread(() -> {

                    try {
                        jframe.setTitle("Parsing new map, please wait...");

                        overlay.setSimulation(null);
                        sim = null;

                        /* parsing */
                        OSMParser.Result result = parser.parse(file);
                        streetgraph = result.streetgraph;
                        Utils.setFeatureProvider(layerSupplier.getLayerDefinitions(), result.segment);
                        visualization.getVisualizer().resetView();
                        jframe.setTitle("MicroTrafficSim - " + file.getName());
                    } catch (XMLStreamException | IOException e) {
                        e.printStackTrace();
                        Runtime.getRuntime().halt(1);
                    }

                    sync.getUnbusy();
                }).start();
            }
    }

    /*
    |===============|
    | visualization |
    |===============|
    */
    private void prepareVisualization() {

        final Projection projection = config.visualization().projection().get();

        /* set up visualization style and sources */
        SegmentLayerProvider provider = layerSupplier.getSegmentLayerProvider(
                projection,
                layerSupplier.getLayerDefinitions());

		/* create the visualizer */
        visualization = createVisualization(provider);

		/* parse the OSM file asynchronously and update the sources */
        parser = layerSupplier.getParser(config);

        /* create the visualization overlay */
        overlay = new SpriteBasedVehicleOverlay(projection);
        visualization.putOverlay(0, overlay);
    }

    private SegmentBasedVisualization createVisualization(SegmentLayerProvider provider) {
        SegmentBasedVisualization vis = new SegmentBasedVisualization(
                WINDOW_WIDTH,
                WINDOW_HEIGHT,
                provider,
                NUM_SEGMENT_WORKERS);

        vis.getKeyController().addKeyCommand(
                KeyEvent.EVENT_KEY_PRESSED,
                KeyEvent.VK_F12,
                e -> Utils.asyncScreenshot(vis.getRenderContext()));

        vis.getKeyController().addKeyCommand(
                KeyEvent.EVENT_KEY_PRESSED,
                KeyEvent.VK_ESCAPE,
                e -> Runtime.getRuntime().halt(0));

        vis.getRenderContext().setUncaughtExceptionHandler(new Utils.DebugExceptionHandler());

        return vis;
    }

    private VisualizationPanel createVisualizationPanel(SegmentBasedVisualization vis)
            throws UnsupportedFeatureException {
        VisualizerConfig config = vis.getDefaultConfig();

        if (MSAA > 1) {
            config.glcapabilities.setSampleBuffers(true);
            config.glcapabilities.setNumSamples(MSAA);
        }

        return new VisualizationPanel(vis, config);
    }

    public void addKeyCommand(short event, short vk, KeyCommand command) {
        visualization.getKeyController().addKeyCommand(event, vk, command);
    }

    /*
    |========|
    | JFrame |
    |========|
    */
    private void addToTopBar(JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(component);
    }

    /*
    |====================|
    | vehicle simulation |
    |====================|
    */
    /**
     * Toggles simulation between run and pause.
     *
     * @return new state == paused
     */
    boolean asyncRunSimulation() {

        if (sim != null)
            if (sim.isPaused()) {
                sim.run();
            } else {
                sim.cancel();
                return true;
            }
        else
            asyncCreateNewSimulation();

        return config.speedup().get() == 0;
    }

    /**
     * Toggles simulation between run and pause.
     *
     * @return new state == paused
     */
    boolean asyncRunOneStep() {
        if (sim != null) {
            if (!sim.isPaused())
                sim.cancel();
            new Thread(() -> sim.runOneStep()).start();
        }
        else
            asyncCreateNewSimulation();
        return true;
    }

    void asyncCreateNewSimulation() {
        if (isVisible() && (streetgraph != null)) {
            if (!sync.getBusy())
                showPreferences(true);
        } else
            JOptionPane.showMessageDialog(null,
                    "A map is needed for a simulation.\n" +
                            "For loading a map, please go to\n" +
                            "Map -> Open map...\n");
    }

    /*
    |=============|
    | preferences |
    |=============|
    */
    private boolean forNewSimulation;
    void showPreferences(boolean forNewSimulation) {

        synchronized (lock_preferences) {
            this.forNewSimulation = forNewSimulation;
            if (preferences != null) {
                for (ComponentId componentId : ComponentId.values()) {
                    boolean isEnabled = (sim == null) || forNewSimulation || componentId.isEnabledDuringSimulating();
                    preferences.setEnabled(isEnabled, componentId);
                }
                preferences.toFront();
                return;
            }

            preferences = new PrefFrame();
            SwingUtilities.invokeLater(() -> {
                /* add sub panels */
                preferences.addPrefPanel(new PanelGeneral(config));
                preferences.addPrefPanel(new PanelCrossingLogic(config));
                preferences.addPrefPanel(new PanelVisualization(config));
                preferences.addPrefPanel(new PanelConcurrency(config));

                /* create/init */
                preferences.create();
                preferences.initSettings();

                for (ComponentId componentId : ComponentId.values()) {
                    boolean isEnabled = (sim == null) || forNewSimulation || componentId.isEnabledDuringSimulating();
                    isEnabled &= !componentId.isAlwaysDisabled();
                    preferences.setEnabled(isEnabled, componentId);
                }

                preferences.addListener(newConfig -> {

                    if (!this.forNewSimulation) {
                        config.reset(newConfig);
                        sync.getUnbusy();
                    } else {
                        new Thread(() -> {
                            /* reset */
                            config.reset(newConfig);
                            streetgraph.reset();
                            overlay.setSimulation(null);
                            sim = null;

                            String oldTitle = jframe.getTitle();
                            jframe.setTitle("Calculating vehicle routes 0%");

                            /* create the simulation */
                            sim = simulationConstructor.instantiate(config, streetgraph, overlay.getVehicleFactory());
                            overlay.setSimulation(sim);

                            /* initialize the simulation */
                            sim.prepare(currentInPercent
                                    -> jframe.setTitle("Calculating vehicle routes " + currentInPercent + "%"));
                            sim.runOneStep();

                            jframe.setTitle(oldTitle);
                            sync.getUnbusy();
                        }).start();
                    }
                });
                preferences.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        super.windowClosed(e);
                        preferences = null;
                        sync.getUnbusy();
                    }
                });
                preferences.pack();
                preferences.setLocationRelativeTo(null); // center on screen; close to setVisible
                preferences.setVisible(true);
            });
        }
    }
}