package microtrafficsim.examples.mapviewer;

import com.jogamp.newt.event.KeyEvent;
import microtrafficsim.core.convenience.DefaultParserConfig;
import microtrafficsim.core.convenience.MapViewer;
import microtrafficsim.core.convenience.TileBasedMapViewer;
import microtrafficsim.core.map.style.MapStyleSheet;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.scenario.areas.ScenarioAreaOverlay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;


/**
 * Map viewer example. The map to be displayed can be specified via the command-line options.
 *
 * @author Maximilian Luz
 */
public class Main {

    /**
     * The used style sheet, defining style and content of the visualization.
     */
    private static final MapStyleSheet STYLE = new MonochromeStyleSheet();


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
    private static void run(File file) throws UnsupportedFeatureException {
        TileBasedMapViewer viewer = new TileBasedMapViewer(STYLE);
        viewer.create();

        viewer.addOverlay(0, new ScenarioAreaOverlay(viewer.getProjection()));
        // viewer.addOverlay(1, new TileGridOverlay(viewer.getTilingScheme()));

        /* parse the OSM file asynchronously and update the sources */
        OSMParser parser = DefaultParserConfig.get(STYLE).build();
        if (file != null)
            asyncParse(viewer, parser, file);

        /* create and initialize the JFrame */
        JFrame frame = new JFrame("MicroTrafficSim - OSM MapViewer Example");
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
                viewer.destroy();
                frame.dispose();
                System.exit(0);
            }
        });

        JFileChooser fc = new JFileChooser();
        viewer.addKeyCommand(KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_E, (e) -> {
            int status = fc.showOpenDialog(frame);
            if (status == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                asyncParse(viewer, parser, f);
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
    private static void asyncParse(MapViewer viewer, OSMParser parser, File file) {
        new Thread(() -> {
            try {
                viewer.changeMap(parser.parse(file));
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
        File file = null;

        if (args.length == 1) {
            switch (args[0]) {
            case "-h":
            case "--help":
                printUsage();
                return;

            default:
                file = new File(args[0]);
            }
        }

        try {
            run(file);
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
        System.out.println("  mapviewer                Run this example without any map-file");
        System.out.println("  mapviewer <file>         Run this example with the specified map-file");
        System.out.println("  mapviewer --help | -h    Show this help message.");
        System.out.println("");
    }
}
