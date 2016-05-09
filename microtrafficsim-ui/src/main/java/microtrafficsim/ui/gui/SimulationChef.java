package microtrafficsim.ui.gui;

import com.jogamp.newt.event.KeyEvent;
import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.simulation.controller.configs.SimulationConfig;
import microtrafficsim.core.simulation.layers.LayerSupplier;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.VisualizationPanel;
import microtrafficsim.core.vis.VisualizerConfig;
import microtrafficsim.core.vis.map.segments.SegmentLayerProvider;
import microtrafficsim.core.vis.segmentbased.SegmentBasedVisualization;
import microtrafficsim.ui.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Set;

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
    private LayerSupplier layerSupplier;
    // visualization
    private VisualizationPanel vpanel;
    SegmentBasedVisualization visualization;
    private OSMParser parser;
    private Set<LayerDefinition> layers;
    // jframe
    private JFrame jframe;
    private JMenuBar menubar;

    public SimulationChef(SimulationConfig config, LayerSupplier layerSupplier) {
        this(config, layerSupplier, "MicroTrafficSim - OSM MapViewer Example");
    }

    public SimulationChef(SimulationConfig config, LayerSupplier layerSupplier, String title) {
        super();
        jframe = new JFrame(title);
        this.config = config;
        this.layerSupplier = layerSupplier;
    }

    /*
    |=================|
    | general actions |
    |=================|
    */
    public void prepareSimulation() throws UnsupportedFeatureException {

        prepareVisualization();
        prepareFrame();
    }

    public void showGui() {
        /* show frame and start visualization */
        jframe.setVisible(true);
        vpanel.start();

        if (PRINT_FRAME_STATS)
            visualization.getRenderContext().getAnimator().setUpdateFPSFrames(60, System.out);
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
        new Thread(() -> {
            try {
                jframe.setTitle("Parsing new map, please wait...");
                OSMParser.Result result = parser.parse(file);
                Utils.setFeatureProvider(layers, result.segment);
                visualization.getVisualizer().resetView();
                jframe.setTitle("MicroTrafficSim - " + file.getName()); // TODO
            } catch (XMLStreamException | IOException e) {
                e.printStackTrace();
                Runtime.getRuntime().halt(1);
            }
        }).start();
    }

    /*
    |===============|
    | visualization |
    |===============|
    */
    private void prepareVisualization() throws UnsupportedFeatureException {

        /* set up visualization style and sources */
        layers = layerSupplier.getLayerDefinitions();
        SegmentLayerProvider provider = layerSupplier.getSegmentLayerProvider(config.visualization.projection, layers);

		/* create the visualizer */
        visualization = createVisualization(provider);
        vpanel = createVisualizationPanel(visualization);

		/* parse the OSM file asynchronously and update the sources */
        parser = layerSupplier.getParser();
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

    /*
    |========|
    | JFrame |
    |========|
    */
    private void prepareFrame() {

        /* create and initialize the VisualizationPanel and JFrame */
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
    }

    public void addJMenuBar() {

        if (menubar == null) {
            menubar = new JMenuBar();
            jframe.add(menubar, BorderLayout.NORTH);
        }
    }

    /**
     * This method adds an item of given name to the menubar to the appropriate menu. If this menu does not exist, a new
     * one is created.
     *
     * @param menuname Name of the menu containing the item
     * @param itemname Name of the item
     * @param l This action is executed, when the item is clicked
     */
    public void addJMenuItem(String menuname, String itemname, ActionListener l) {

        JMenu menu = null;
        // check if menuname already exists
        for (int i = 0; i < menubar.getMenuCount(); i++) {
            JMenu menui = menubar.getMenu(i);
            if (menui.getText().equals(menuname))
                menu = menui;
        }

        if (menu == null) {
            menu = new JMenu(menuname);
            menubar.add(menu);
        }

        JMenuItem item = new JMenuItem(itemname);
        item.addActionListener(l);
        menu.add(item);
    }
}
