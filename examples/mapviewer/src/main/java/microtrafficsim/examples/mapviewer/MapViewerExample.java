package microtrafficsim.examples.mapviewer;

import com.jogamp.newt.event.KeyEvent;
import microtrafficsim.core.convenience.DefaultParserConfig;
import microtrafficsim.core.convenience.TileBasedMapViewer;
import microtrafficsim.core.map.SegmentFeatureProvider;
import microtrafficsim.core.map.style.MapStyleSheet;
import microtrafficsim.core.map.tiles.QuadTreeTiledMapSegment;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.serialization.Container;
import microtrafficsim.core.serialization.Serializer;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.scenario.areas.ScenarioAreaOverlay;
import microtrafficsim.utils.concurrency.interruptsafe.InterruptSafeFutureTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * Map viewer example. The map to be displayed can be specified via the command-line options or loaded by pressing the
 * {@code e} key. A binary file for faster re-loading can be written using the {@code w} key.
 *
 * @author Maximilian Luz
 */
public class MapViewerExample {
    private static Logger logger = LoggerFactory.getLogger(MapViewerExample.class);

    // TODO: update docs

    /**
     * The used style sheet, defining style and content of the visualization.
     */
    private static final MapStyleSheet STYLE = new MonochromeStyleSheet();


    private JFrame frame;
    private TileBasedMapViewer viewer;
    private OSMParser parser;
    private SegmentFeatureProvider segment = null;
    private Future<Void> loading = null;
    private Serializer serializer;


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
        serializer = Serializer.create();

        viewer = new TileBasedMapViewer(STYLE);
        viewer.create();

        viewer.addOverlay(0, new ScenarioAreaOverlay(viewer.getProjection()));

        /* parse the OSM file asynchronously and update the sources */
        parser = DefaultParserConfig.get(STYLE).build();
        if (file != null)
            load(file);

        /* create and initialize the JFrame */
        frame = new JFrame("MicroTrafficSim - MapViewer Example");
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
                load(fc.getSelectedFile());
            }
        });

        viewer.addKeyCommand(KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_W, (e) -> {
            int status = fc.showSaveDialog(frame);
            if (status == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();

                if (f.exists()) {
                    status = JOptionPane.showConfirmDialog(frame,
                            "The selected file already exists. Continue?", "Save File", JOptionPane.OK_CANCEL_OPTION);

                    if (status != JOptionPane.OK_OPTION) {
                        return;
                    }
                }

                store(f);
            }
        });

        /* show frame and start visualization */
        frame.setVisible(true);

        viewer.show();
    }

    private void load(File file) {
        if (this.loading != null) {
            int status = JOptionPane.showConfirmDialog(frame,
                    "Another file is already being loaded. Continue?", "Load File", JOptionPane.OK_CANCEL_OPTION);

            if (status != JOptionPane.OK_OPTION) {
                return;
            }

            loading.cancel(true);
        }

        InterruptSafeFutureTask<Void> loading = new InterruptSafeFutureTask<>(() -> {
            boolean xml = file.getName().endsWith(".osm");
            SegmentFeatureProvider segment;

            try {
                if (xml) {
                    segment = new QuadTreeTiledMapSegment.Generator().generate(parser.parse(file).segment,
                            viewer.getPreferredTilingScheme(), viewer.getPreferredTileGridLevel());
                } else {
                    segment = serializer.read(file).getSegment();
                }
            } catch (InterruptedException e) {
                throw new CancellationException();
            }

            if (Thread.interrupted())
                throw new CancellationException();

            this.segment = segment;
            viewer.setMap(segment);

            return null;
        });

        this.loading = loading;

        new Thread(() -> {
            try {
                logger.info("loading file");
                loading.run();
                loading.get();
                logger.info("finished loading file");
            } catch (InterruptedException | CancellationException e) {
                /* ignore */
            } catch (ExecutionException e) {
                e.printStackTrace();
                Runtime.getRuntime().halt(1);
            } finally {
                this.loading = null;
            }
        }).start();
    }

    private void store(File file) {
        logger.info("writing file");
        try {
            new Container()
                    .setSegment(segment)
                    .write(serializer, file);
        } catch (Throwable t) {
            t.printStackTrace();
            Runtime.getRuntime().halt(1);
        }
        logger.info("finished writing file");
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
