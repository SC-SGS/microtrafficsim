package microtrafficsim.examples.mapviewer;

import com.jogamp.newt.event.KeyEvent;
import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.map.layers.LayerSource;
import microtrafficsim.core.map.style.StyleSheet;
import microtrafficsim.core.map.tiles.QuadTreeTiledMapSegment;
import microtrafficsim.core.map.tiles.QuadTreeTilingScheme;
import microtrafficsim.core.mapviewer.MapViewer;
import microtrafficsim.core.mapviewer.TileBasedMapViewer;
import microtrafficsim.core.mapviewer.utils.Utils;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.VisualizationPanel;
import microtrafficsim.core.vis.Visualizer;
import microtrafficsim.core.vis.VisualizerConfig;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.map.tiles.PreRenderedTileProvider;
import microtrafficsim.core.vis.map.tiles.TileProvider;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerGenerator;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerSource;
import microtrafficsim.core.vis.map.tiles.layers.LayeredTileMap;
import microtrafficsim.core.vis.map.tiles.layers.TileLayerProvider;
import microtrafficsim.core.vis.scenario.ScenarioOverlay;
import microtrafficsim.core.vis.tilebased.TileBasedVisualization;
import microtrafficsim.osm.parser.features.FeatureGenerator;
import microtrafficsim.osm.parser.features.streets.StreetComponent;
import microtrafficsim.osm.parser.features.streets.StreetComponentFactory;
import microtrafficsim.core.parser.processing.sanitizer.SanitizerWayComponent;
import microtrafficsim.core.parser.processing.sanitizer.SanitizerWayComponentFactory;
import microtrafficsim.osm.parser.relations.restriction.RestrictionRelationFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collection;


/**
 * Map viewer example. The map to be displayed can be specified via the command-line options.
 *
 * @author Maximilian Luz
 */
public class Main {

    /**
     * The used style sheet, defining style and content of the visualization.
     */
    private static final StyleSheet STYLE = new MonochromeStyleSheet();


    /**
     * Set up this example. Layer definitions describe the visual layers
     * to be rendered and are used to create a layer provider. With this
     * provider a tile provider is created, capable of returning drawable
     * tiles. These tiles are rendered using the visualization object. A
     * parser is created to parse the specified file (asynchronously) and
     * update the layers (respectively their sources).
     *
     * @param file the file to parse
     * @throws UnsupportedFeatureException if not all required OpenGL features are available
     */
    private static void show(File file) throws UnsupportedFeatureException {
        MapViewer viewer = new TileBasedMapViewer(STYLE);
        viewer.create();

        viewer.addOverlay(0, new ScenarioOverlay(viewer.getProjection()));

        /* parse the OSM file asynchronously and update the sources */
        asyncParse(viewer, file);

        /* create and initialize the JFrame */
        JFrame frame = new JFrame("MicroTrafficSim - OSM Main Example");
        frame.setSize(viewer.getInitialWindowWidth(), viewer.getInitialWindowHeight());
        frame.add(viewer.getVisualizationPanel());

        /*
         * Note: JOGL automatically calls glViewport, we need to make sure that this
         * function is not called with a height or width of 0! Otherwise the program
         * crashes.
         */
        frame.setMinimumSize(new Dimension(100, 100));

        /* on close: stop the visualization and exit */
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                viewer.stop();
                System.exit(0);
            }
        });

        /* show frame and start visualization */
        frame.setVisible(true);

        viewer.show();
    }

    /**
     * Asynchronously parses the given file with the given parser, updates
     * the specified layers and resets the view of the visualizer.
     *
     * @param file   the file to be parsed
     *               layers are set to the parsed result
     */
    private static void asyncParse(MapViewer viewer, File file) {
        new Thread(() -> {
            try {
                viewer.changeMap(viewer.parse(file));
            } catch (Exception e) {
                e.printStackTrace();
                Runtime.getRuntime().halt(1);
            }
        }).start();
    }


    /**
     * Main method, runs this example.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        File file;

        if (args.length == 1) {
            switch (args[0]) {
            case "-h":
            case "--help":
                printUsage();
                return;

            default:
                file = new File(args[0]);
            }
        } else {
            file = new File("map.processing");
        }

        try {
            show(file);
        } catch (UnsupportedFeatureException e) {
            System.out.println("It seems that your PC does not meet the requirements for this software.");
            System.out.println("Please make sure that your graphics driver is up to date.");
            System.out.println();
            System.out.println("The following OpenGL features are required:");
            for (String feature : e.getMissingFeatures())
                System.out.println("\t" + feature);

            System.exit(1);
        }
    }

    /**
     * Prints the usage of this example.
     */
    private static void printUsage() {
        System.out.println("MicroTrafficSim - OSM Main Example.");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("  mapviewer                Run this example with the default map-file");
        System.out.println("  mapviewer <file>         Run this example with the specified map-file");
        System.out.println("  mapviewer --help | -h    Show this help message.");
        System.out.println("");
    }
}
