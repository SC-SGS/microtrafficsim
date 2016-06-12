package microtrafficsim.examples.simulation;

import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.simulation.Simulation;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.VisualizationPanel;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.map.segments.SegmentLayerProvider;
import microtrafficsim.core.vis.segmentbased.SegmentBasedVisualization;
import microtrafficsim.core.vis.simulation.SpriteBasedVehicleOverlay;
import microtrafficsim.examples.simulation.scenarios.Scenario;

import javax.swing.*;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Set;


public class Main {
	
	private static final String DEFAULT_OSM_XML = "/Users/Dominic/Documents/Studium/Bachelor_of_Disaster/microtrafficsim/maps/Stuttgart.osm";
	private static final boolean PRINT_FRAME_STATS = false;

	public static void main(String[] args) throws Exception {
		File file;
		
		if (args.length == 1) {
			switch(args[0]) {
			case "-h":
			case "--help":
				printUsage();
				return;
			default:
				file = new File(args[0]);
			}
		} else {
			file = new File(DEFAULT_OSM_XML);
		}
		
		show(new MercatorProjection(), file);
	}
	
	public static void printUsage() {
		System.out.println("MicroTrafficSim - Simulation Example");
		System.out.println("");
		System.out.println("Usage:");
		System.out.println("  simulation                Run this example with the default map-file");
		System.out.println("  simulation <file>         Run this example with the specified map-file");
		System.out.println("  simulation --help | -h    Show this help message.");
		System.out.println("");
	}
	
	
	private static void show(Projection projection, File file) throws Exception {
		
		/* create configuration for scenarios */
		SimulationConfig config = new SimulationConfig();
		Example.initSimulationConfig(config);
		
		/* set up visualization style and sources */
		Set<LayerDefinition> layers = Example.getLayerDefinitions();
		SegmentLayerProvider provider = Example.getSegmentLayerProvider(projection, layers);
		
		/* parse the OSM file */
		OSMParser parser = Example.getParser(config);
		OSMParser.Result result;
		
		try {
			result = parser.parse(file);
		} catch (XMLStreamException | IOException e) {
			e.printStackTrace();
			return;
		}
		
		Utils.setFeatureProvider(layers, result.segment);
		
		/* create the visualization overlay */
		// ShaderBasedVehicleOverlay overlay = new ShaderBasedVehicleOverlay(projection);
		SpriteBasedVehicleOverlay overlay = new SpriteBasedVehicleOverlay(projection);
		
		/* create the simulation */
		Simulation sim = new Scenario(config, result.streetgraph, overlay.getVehicleFactory());
		overlay.setSimulation(sim);
		
		/* create and display the frame */
		SwingUtilities.invokeLater(() -> {

			/* create the actual visualizer */
			SegmentBasedVisualization visualization = Example.createVisualization(provider, sim);
			visualization.putOverlay(0, overlay);

			VisualizationPanel vpanel;

			try {
				vpanel = Example.createVisualizationPanel(visualization);
			} catch (UnsupportedFeatureException e) {
				e.printStackTrace();
                Runtime.getRuntime().halt(0);
                return;
			}

			/* create and initialize the JFrame */
			JFrame frame = new JFrame("MicroTrafficSim - Simulation Example");
			frame.setSize(Example.WINDOW_WIDTH, Example.WINDOW_HEIGHT);
			frame.add(vpanel);

			/*
			 * Note: JOGL automatically calls glViewport, we need to make
			 * sure that this function is not called with a height or width
			 * of 0! Otherwise the program crashes.
			 */
			frame.setMinimumSize(new Dimension(100, 100));

			// on close: stop the visualization and exit
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					vpanel.stop();
					System.exit(0);
				}
			});

			/* show the frame and start the render-loop */
			frame.setVisible(true);
			vpanel.start();

			if (PRINT_FRAME_STATS)
				visualization.getRenderContext().getAnimator().setUpdateFPSFrames(60, System.out);
		});
		
		/* initialize the simulation */
		sim.prepare();
		sim.runOneStep();
	}
}