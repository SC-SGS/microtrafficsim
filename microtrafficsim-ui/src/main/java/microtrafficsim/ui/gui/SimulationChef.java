package microtrafficsim.ui.gui;

import com.jogamp.newt.event.KeyEvent;
import microtrafficsim.core.frameworks.vehicle.IVisualizationVehicle;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.simulation.controller.Simulation;
import microtrafficsim.core.simulation.controller.configs.SimulationConfig;
import microtrafficsim.core.simulation.layers.LayerSupplier;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.VisualizationPanel;
import microtrafficsim.core.vis.VisualizerConfig;
import microtrafficsim.core.vis.input.KeyCommand;
import microtrafficsim.core.vis.map.segments.SegmentLayerProvider;
import microtrafficsim.core.vis.segmentbased.SegmentBasedVisualization;
import microtrafficsim.core.vis.simulation.SpriteBasedVehicleOverlay;
import microtrafficsim.ui.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public class SimulationChef {

    private static Logger logger = LoggerFactory.getLogger(SimulationChef.class);
//    public static final String DEFAULT_OSM_XML = "map.osm"; TODO delete?

    public static final boolean PRINT_FRAME_STATS = false;

    public static final int WINDOW_WIDTH = 1600;
    public static final int WINDOW_HEIGHT = 900;
    public static final int MSAA = 0;
    public static final int NUM_SEGMENT_WORKERS = 2;

    // general
    private SimulationConfig config;
    private BusyClosure sync; // synchronized
    // visualization
    private VisualizationPanel vpanel;
    private SegmentBasedVisualization visualization;
    private SpriteBasedVehicleOverlay overlay;
    private OSMParser parser;
    private Set<LayerDefinition> layers;
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
        Simulation instantiate(StreetGraph streetgraph, Supplier<IVisualizationVehicle> vehicleFactory);
    }
    private interface BusyClosure {
        /**
         * @return previous value;
         */
        boolean getBusy();
        void getUnbusy();
    }

    public SimulationChef(SimulationConfig config, SimulationConstructor simulationConstructor) {
        this(config, simulationConstructor, "MicroTrafficSim - OSM MapViewer Example");
    }

    public SimulationChef(SimulationConfig config, SimulationConstructor simulationConstructor, String title) {

        super();
        jframe = new JFrame(title);
        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        jframe.add(topPanel, BorderLayout.NORTH);
        this.config = config;
        this.simulationConstructor = simulationConstructor;

        /* is busy */
        class BusyClosureImpl implements BusyClosure {

            private boolean isBusy;

            @Override
            public synchronized boolean getBusy() {
                boolean old = isBusy;
                isBusy = true;
                return old;
            }

            @Override
            public synchronized void getUnbusy() {
                isBusy = false;
            }
        }
        sync = new BusyClosureImpl();
    }

    private boolean showsGui() {
        return vpanel != null;
    }

    /*
    |=================|
    | general actions |
    |=================|
    */
    public void showGui() {

        if (!showsGui()) {

            sync.getBusy();

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
    public void asyncParse(File file) {

        if (showsGui())
            if (!sync.getBusy()) {

                new Thread(() -> {

                    try {
                        jframe.setTitle("Parsing new map, please wait...");

                        /* reset */
                        overlay.setSimulation(null);
                        sim = null;

                        /* parsing */
                        OSMParser.Result result = parser.parse(file);
                        streetgraph = result.streetgraph;
                        Utils.setFeatureProvider(layers, result.segment);
                        visualization.getVisualizer().resetView();
                        jframe.setTitle("MicroTrafficSim - " + file.getName()); // TODO
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
    public void prepareVisualization(LayerSupplier layerSupplier) {

        /* set up visualization style and sources */
        layers = layerSupplier.getLayerDefinitions();
        SegmentLayerProvider provider = layerSupplier.getSegmentLayerProvider(config.visualization.projection, layers);

		/* create the visualizer */
        visualization = createVisualization(provider);

		/* parse the OSM file asynchronously and update the sources */
        parser = layerSupplier.getParser(config);

        /* create the visualization overlay */
        overlay = new SpriteBasedVehicleOverlay(config.visualization.projection);
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
    public void addJMenuBar(JMenuBar menubar) {
        menubar.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(menubar);
    }

    public void addJToolBar(JToolBar toolbar) {
        toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(toolbar);
    }

    /*
    |====================|
    | vehicle simulation |
    |====================|
    */
    public void asyncPrepareNewSimulation() {

        simulate(true);
    }

    /**
     * Toggles simulation between run and pause.
     *
     * @return new state == paused
     */
    public boolean asyncRunSimulation() {

        if (sim != null)
            if (sim.isPaused()) {
                sim.run();
                return false;
            } else {
                sim.cancel();
                return true;
            }
        else
            simulate(false);
            return false;
    }

    private void simulate(boolean shouldPreparing) {

        if (showsGui())
            if (streetgraph != null)
                if (!sync.getBusy()) {
                    new Thread(() -> {

                        /* reset */
                        streetgraph.reset();
                        overlay.setSimulation(null);
                        sim = null;

                        String oldTitle = jframe.getTitle();
                        jframe.setTitle("Calculating vehicle routes 0%");

                        /* create the simulation */
                        sim = simulationConstructor.instantiate(streetgraph, overlay.getVehicleFactory());
                        overlay.setSimulation(sim);

                        /* initialize the simulation */
                        sim.prepare(currentInPercent
                                -> jframe.setTitle("Calculating vehicle routes " + currentInPercent + "%"));
                        if (shouldPreparing)
                            sim.runOneStep();
                        else
                            sim.run();

                        jframe.setTitle(oldTitle);
                        sync.getUnbusy();
                    }).start();
            }
    }
}
