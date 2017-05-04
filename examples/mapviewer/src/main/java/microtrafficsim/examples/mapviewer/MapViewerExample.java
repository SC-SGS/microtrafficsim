package microtrafficsim.examples.mapviewer;

import com.jogamp.newt.event.KeyEvent;
import microtrafficsim.core.convenience.DefaultParserConfig;
import microtrafficsim.core.convenience.filechoosing.MapFileChooser;
import microtrafficsim.core.convenience.MapViewer;
import microtrafficsim.core.convenience.TileBasedMapViewer;
import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.exceptions.NotAvailableException;
import microtrafficsim.core.exfmt.extractor.map.QuadTreeTiledMapSegmentExtractor;
import microtrafficsim.core.map.MapSegment;
import microtrafficsim.core.map.SegmentFeatureProvider;
import microtrafficsim.core.map.style.MapStyleSheet;
import microtrafficsim.core.map.tiles.QuadTreeTiledMapSegment;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.serialization.ExchangeFormatSerializer;
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
 * Map viewer example. The map to be displayed can be specified via the command-line options or loaded by pressing
 * {@code E} or {@code Ctrl-O}. A binary file for faster re-loading can be written using {@code W} or {@code Ctrl-S}.
 * Both, the {@code Esc} and {@code Q} key will close this program.
 * <p>
 * This example is loaded with the ScenarioAreaOverlay. For a detailed look at the controls, see
 * <a href="https://github.com/sgs-us/microtrafficsim/wiki/Controls">github.com/sgs-us/microtrafficsim/wiki/Controls</a>
 *
 * @author Maximilian Luz
 */
public class MapViewerExample {
    private static Logger logger = LoggerFactory.getLogger(MapViewerExample.class);

    /**
     * The used style sheet, defining style and content of the visualization.
     */
    private static final MapStyleSheet STYLE = new DarkMonochromeStyleSheet();


    private MapFileChooser filechooser;
    private OSMParser parser;
    private ExchangeFormat exfmt;
    private ExchangeFormatSerializer serializer;

    private JFrame frame;
    private TileBasedMapViewer viewer;

    private String file = null;
    private SegmentFeatureProvider segment = null;

    private Thread loader = null;
    private Future<Void> loading = null;


    /**
     * Set up and run this example.
     * <p>
     * We begin by creating a Serializer, which is responsible for reading and writing binary (serialized) files.
     * <br>
     * Next, we create a Parser and a TileMapViewer. The second class is responsible for the visualization. It not only
     * renders the map but also handles Overlays, that can be added to the map-viewer. The {@code MapStyleSheet}
     * provided to the map-viewer specifies what to display, and how to do that. This style is also used to tell the
     * XML parser what we want to extract from an OpenStreetMap XML file.
     * <br>
     * Once the map-viewer has been created, we can connect it with a JFrame add some key-commands and display it.
     *
     * @param file the initial file to load. If {@code null}, no file will be loaded.
     * @throws UnsupportedFeatureException if not all required OpenGL features are available
     */
    private void run(File file) throws UnsupportedFeatureException {
        filechooser = new MapFileChooser();
        filechooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

        parser = DefaultParserConfig.get(STYLE).build();

        viewer = setUpMapViewer(STYLE);
        frame = setUpFrame(viewer);

        setUpSerializer(viewer);

        show();

        /* Parse the OSM file asynchronously and update the sources */
        if (file != null)
            load(file);
    }

    /**
     * Set up the {@code TileBasedMapViewer}.
     */
    private TileBasedMapViewer setUpMapViewer(MapStyleSheet style) throws UnsupportedFeatureException {
        TileBasedMapViewer viewer = new TileBasedMapViewer(style);
        viewer.create();

        /* Create and add a new ScenarioAreaOverlay */
        viewer.addOverlay(0, new ScenarioAreaOverlay());

        setUpShortcuts(viewer);

        return viewer;
    }

    /**
     * Set up the application window.
     */
    private JFrame setUpFrame(MapViewer viewer) {
        /* create and initialize the JFrame */
        JFrame frame = new JFrame(getDefaultFrameTitle());
        frame.setSize(viewer.getInitialWindowWidth(), viewer.getInitialWindowHeight());
        frame.add(viewer.getVisualizationPanel());

        /*
         * Note: JOGL automatically calls glViewport, we need to make sure that this
         * function is not called with a height or width of 0! Otherwise the program
         * crashes.
         */
        frame.setMinimumSize(new Dimension(100, 100));

        /* On close: stop the visualization and exit */
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });

        return frame;
    }

    private void setUpSerializer(TileBasedMapViewer viewer) {
        serializer = ExchangeFormatSerializer.create();
        exfmt = ExchangeFormat.getDefault();
        exfmt.getConfig().set(QuadTreeTiledMapSegmentExtractor.Config.getDefault(
                viewer.getPreferredTilingScheme(), viewer.getPreferredTileGridLevel()));
    }

    /**
     * Show frame and start visualization.
     */
    private void show() {
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        viewer.show();
    }

    /**
     * Safely terminate the application.
     */
    private void shutdown() {
        if (loading != null) {
            loading.cancel(true);
            loading = null;
        }

        if (loader != null) {
            try {
                loader.join();
                loader = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        SwingUtilities.invokeLater(() -> {
            viewer.destroy();
            frame.dispose();
            System.exit(0);
        });
    }


    /**
     * Set up the keyboard-shortcuts.
     */
    private void setUpShortcuts(TileBasedMapViewer viewer) {
        /* Use <Esc> and <Q> as shortcuts to stop and exit */
        viewer.addKeyCommand(KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_ESCAPE, (e) -> shutdown());
        viewer.addKeyCommand(KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_Q, (e) -> shutdown());

        /* Use <C> and as shortcut to reset the view */
        viewer.addKeyCommand(KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_C, (e) -> viewer.resetView());

        /* Use <W> and <Ctrl-S> as shortcuts to save the current map to a binary file */
        viewer.addKeyCommand(KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_W, (e) -> store());
        viewer.addKeyCommand(KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_S, (e) -> {
            if (e.isControlDown()) {
                store();
            }
        });

        /* Use <E> and <Ctrl-O> as shortcuts to load binary or osm (xml) file */
        viewer.addKeyCommand(KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_E, (e) -> load());
        viewer.addKeyCommand(KeyEvent.EVENT_KEY_PRESSED, KeyEvent.VK_O, (e) -> {
            if (e.isControlDown()) {
                load();
            }
        });
    }


    /**
     * Open a JFileChooser to let the user load a binary or xml file.
     */
    private void load() {
        int status = filechooser.showOpenDialog(frame);
        if (status == JFileChooser.APPROVE_OPTION) {
            load(filechooser.getSelectedFile());
        }
    }

    /**
     * Load the specified file (asynchronously), abort a previous in-flight load-operation if there is any.
     * @param file the file to load.
     */
    private void load(File file) {
        {   /* cancel loading, if map is loading */
            Future<Void> loading = this.loading;
            if (loading != null) {
                int status = JOptionPane.showConfirmDialog(frame,
                        "Another file is already being loaded. Continue?", "Load File", JOptionPane.OK_CANCEL_OPTION);

                if (status != JOptionPane.OK_OPTION) {
                    return;
                }

                loading.cancel(true);

                /* wait until the task has been fully cancelled, required to set the frame-title correctly */
                try {
                    loader.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        /* create new load-task */
        InterruptSafeFutureTask<Void> loading = new InterruptSafeFutureTask<>(() -> {
            SwingUtilities.invokeLater(() ->
                    frame.setTitle(getDefaultFrameTitle() + " - [Loading: " + file.getPath() + "]"));

            /* parse file */
            boolean xml = file.getName().endsWith(".osm");
            SegmentFeatureProvider segment;

            try {
                if (xml) {
                    QuadTreeTiledMapSegment.Generator tiler = new QuadTreeTiledMapSegment.Generator();
                    TilingScheme scheme = viewer.getPreferredTilingScheme();

                    OSMParser.Result result = parser.parse(file);
                    segment = tiler.generate(result.segment, scheme, viewer.getPreferredTileGridLevel());
                } else {
                    ExchangeFormat.Manipulator xmp = exfmt.manipulator(serializer.read(file));
                    try {
                        segment = xmp.extract(QuadTreeTiledMapSegment.class);
                    } catch (NotAvailableException e) {     // thrown when no TileGrid available
                        segment = xmp.extract(MapSegment.class);
                    }
                }
            } catch (InterruptedException e) {
                throw new CancellationException();
            }

            if (Thread.interrupted())
                throw new CancellationException();

            /* set segment */
            this.segment = segment;
            this.file = file.getPath();
            viewer.setMap(segment);

            return null;
        });

        this.loading = loading;

        /* execute load-task */
        loader = new Thread(() -> {
            try {
                logger.info("loading file");
                loading.run();
                loading.get();
                logger.info("finished loading file");
            } catch (InterruptedException | CancellationException e) {
                /* ignore */
            } catch (ExecutionException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame,
                        "Failed to load file: '" + file.getPath() + "'.\n"
                                + "Please make sure this file exists and is a valid OSM XML or MTS binary file.",
                        "Error loading file", JOptionPane.ERROR_MESSAGE);
            } finally {
                SwingUtilities.invokeLater(() -> frame.setTitle(getDefaultFrameTitle()));
                this.loading = null;
            }
        });
        loader.start();
    }

    /**
     * Open a JFileChooser to let the user store a binary file of the map being displayed.
     */
    private void store() {
        int status = filechooser.showSaveDialog(frame);
        if (status == JFileChooser.APPROVE_OPTION) {
            File f = filechooser.getSelectedFile();

            if (f.exists()) {
                status = JOptionPane.showConfirmDialog(frame,
                        "The selected file already exists. Continue?", "Save File", JOptionPane.OK_CANCEL_OPTION);

                if (status != JOptionPane.OK_OPTION) {
                    return;
                }
            }

            store(f);
        }
    }

    /**
     * Store the current map in the specified file (synchronously).
     * @param file the file to write the current map to.
     */
    private void store(File file) {
        logger.info("writing file");

        try {
            SwingUtilities.invokeAndWait(() ->
                frame.setTitle(getDefaultFrameTitle() + " - [Saving: " + file.getPath() + "]"));

            Container container = exfmt.manipulator()
                    .inject(segment)
                    .getContainer();

            serializer.write(file, container);

        } catch (Throwable t) {
            t.printStackTrace();
            Runtime.getRuntime().halt(1);

        } finally {
            SwingUtilities.invokeLater(() -> frame.setTitle(getDefaultFrameTitle()));
        }
        logger.info("finished writing file");
    }


    /**
     * Return the default window title.
     * @return the default frame title.
     */
    private String getDefaultFrameTitle() {
        StringBuilder title = new StringBuilder("MicroTrafficSim - Map Viewer Example");

        if (file != null) {
            title.append(" - [").append(file).append("]");
        }

        return title.toString();
    }


    /**
     * Main method, runs this example.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        File file;

        if (args.length == 0) {
            file = null;
        } else if (args.length == 1) {
            switch (args[0]) {
            case "-h":
            case "--help":
                printUsage();
                return;

            default:
                file = new File(args[0]);
            }
        } else {
            printUsage();
            return;
        }

        System.setProperty("awt.useSystemAAFontSettings","on");
        System.setProperty("swing.aatext", "true");

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
        System.out.println("MicroTrafficSim - Map Viewer Example.");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("  mapviewer                Run this example without any map-file");
        System.out.println("  mapviewer <file>         Run this example with the specified map-file");
        System.out.println("  mapviewer --help | -h    Show this help message.");
        System.out.println("");
    }
}
