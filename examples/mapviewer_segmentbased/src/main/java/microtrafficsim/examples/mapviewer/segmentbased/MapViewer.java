package microtrafficsim.examples.mapviewer.segmentbased;

import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.vis.VisualizationPanel;
import microtrafficsim.core.vis.Visualizer;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.map.segments.SegmentLayerProvider;
import microtrafficsim.core.vis.segmentbased.SegmentBasedVisualization;

import javax.swing.*;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Set;


public class MapViewer {
    public static void main(String[] args) throws Exception {
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
            file = new File(Example.DEFAULT_OSM_XML);
        }

        show(new MercatorProjection(256), file);    // tiles will be 512x512 pixel
    }

    private static void printUsage() {
        System.out.println("MicroTrafficSim - OSM MapViewer Example.");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("  mapviewer                Run this example with the default map-file");
        System.out.println("  mapviewer <file>         Run this example with the specified map-file");
        System.out.println("  mapviewer --help | -h    Show this help message.");
        System.out.println("");
    }


    private static void show(Projection projection, File file) throws Exception {

        /* set up visualization style and sources */
        Set<LayerDefinition> layers   = Example.getLayerDefinitions();
        SegmentLayerProvider provider = Example.getSegmentLayerProvider(projection, layers);

        /* create the visualizer */
        SegmentBasedVisualization visualization = Example.createVisualization(provider);

        /* parse the OSM file asynchronously and update the sources */
        OSMParser parser = Example.getParser();
        asyncParse(parser, file, layers, visualization.getVisualizer());

        /* create and initialize the VisualizationPanel and JFrame */
        VisualizationPanel vpanel = Example.createVisualizationPanel(visualization);
        JFrame             frame  = new JFrame("MicroTrafficSim - OSM MapViewer Example");
        frame.setSize(Example.WINDOW_WIDTH, Example.WINDOW_HEIGHT);
        frame.add(vpanel);

        /*
         * Note: JOGL automatically calls glViewport, we need to make sure that this
         * function is not called with a height or width of 0! Otherwise the program
         * crashes.
         */
        frame.setMinimumSize(new Dimension(100, 100));

        // on close: stop the visualization and exit
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                vpanel.stop();
                System.exit(0);
            }
        });

        /* show frame and start visualization */
        frame.setVisible(true);
        vpanel.start();

        if (Example.PRINT_FRAME_STATS)
            visualization.getRenderContext().getAnimator().setUpdateFPSFrames(60, System.out);
    }

    private static void asyncParse(OSMParser parser, File file, Set<LayerDefinition> layers, Visualizer vis) {
        new Thread(() -> {
            try {
                OSMParser.Result result = parser.parse(file);
                Utils.setFeatureProvider(layers, result.segment);
                vis.resetView();
            } catch (XMLStreamException | IOException e) {
                e.printStackTrace();
                Runtime.getRuntime().halt(1);
            }
        }).start();
    }
}
