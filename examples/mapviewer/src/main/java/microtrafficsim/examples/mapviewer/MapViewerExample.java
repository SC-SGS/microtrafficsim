package microtrafficsim.examples.mapviewer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.jogamp.newt.event.KeyEvent;
import microtrafficsim.core.convenience.DefaultParserConfig;
import microtrafficsim.core.convenience.TileBasedMapViewer;
import microtrafficsim.core.map.TileFeatureProvider;
import microtrafficsim.core.map.style.MapStyleSheet;
import microtrafficsim.core.map.tiles.QuadTreeTiledMapSegment;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.serialization.KryoSerializer;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.scenario.areas.ScenarioAreaOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


/**
 * Map viewer example. The map to be displayed can be specified via the command-line options.
 *
 * @author Maximilian Luz
 */
public class MapViewerExample {
    private static Logger logger = LoggerFactory.getLogger(MapViewerExample.class);

    /**
     * The used style sheet, defining style and content of the visualization.
     */
    private static final MapStyleSheet STYLE = new MonochromeStyleSheet();


    private TileBasedMapViewer viewer;
    private OSMParser parser;
    private TileFeatureProvider segment = null;


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
    private void run(File file) throws UnsupportedFeatureException {
        viewer = new TileBasedMapViewer(STYLE);
        viewer.create();

        viewer.addOverlay(0, new ScenarioAreaOverlay(viewer.getProjection()));
        // viewer.addOverlay(1, new TileGridOverlay(viewer.getTilingScheme()));

        /* parse the OSM file asynchronously and update the sources */
        parser = DefaultParserConfig.get(STYLE).build();
        if (file != null)
            asyncParse(file);

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
        File dir = new File("/mnt/data/Development/workspaces/microtrafficsim/osmfiles/");      // TODO: REMOVE
        fc.setCurrentDirectory(dir);

        viewer.addKeyCommand(KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_E, (e) -> {
            int status = fc.showOpenDialog(frame);
            if (status == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();

                if (f.getName().endsWith(".ser")) {
                    asyncLoad(f);
                } else {
                    asyncParse(f);
                }
            }
        });

        viewer.addKeyCommand(KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_W, (e) -> {
            int status = fc.showSaveDialog(frame);
            if (status == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();

                logger.info("writing binary file");
                Kryo kryo = KryoSerializer.create();

                try (Output out = new Output(new FileOutputStream(f))) {
                    kryo.writeClassAndObject(out, segment);
                } catch (Throwable t) {
                    t.printStackTrace();
                    Runtime.getRuntime().halt(1);
                }
                logger.info("finished writing binary file");
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
    private void asyncParse(File file) {
        new Thread(() -> {
            try {
                logger.info("loading OSM file");
                segment = new QuadTreeTiledMapSegment.Generator().generate(
                        parser.parse(file).segment, viewer.getPreferredTilingScheme(), viewer.getPreferredTileGridLevel());
                logger.info("finished reading OSM file");
                viewer.setMap(segment);
                logger.info("finished loading OSM file");
            } catch (Exception e) {
                e.printStackTrace();
                Runtime.getRuntime().halt(1);
            }
        }).start();
    }

    private void asyncLoad(File file) {
        new Thread(() -> {
            Kryo kryo = KryoSerializer.create();

            try (Input in = new Input(new FileInputStream(file))) {
                logger.info("loading binary file");
                segment = (TileFeatureProvider) kryo.readClassAndObject(in);
                logger.info("finished reading binary file");
                viewer.setMap(segment);
                logger.info("finished loading binary file");
            } catch (Throwable t) {
                t.printStackTrace();
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
            new MapViewerExample().run(file);
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
        System.out.println("MicroTrafficSim - OSM MapViewerExample Example.");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("  mapviewer                Run this example without any map-file");
        System.out.println("  mapviewer <file>         Run this example with the specified map-file");
        System.out.println("  mapviewer --help | -h    Show this help message.");
        System.out.println("");
    }
}
