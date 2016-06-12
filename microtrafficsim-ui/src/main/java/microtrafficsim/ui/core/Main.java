package microtrafficsim.ui.core;

import microtrafficsim.ui.gui.SimulationController;
import microtrafficsim.ui.gui.GUIController;
import microtrafficsim.ui.gui.GUIEvent;
import microtrafficsim.ui.scenarios.Scenario;
import microtrafficsim.ui.vis.TileBasedMapViewer;

import javax.swing.*;
import java.io.File;

/**
 * @author Dominic Parga Cacheiro
 */
public class Main {

  public static void main(String[] args) throws Exception {

        /* handle input arguments */
    final File file;
    if (args.length == 1) {
      switch(args[0]) {
        case "-h":
        case "--help":
          printUsage();
          return;

        default:
          file = new File(args[0]);
      }
    } else
      file = new File("/Users/Dominic/Documents/Studium/Bachelor_of_Disaster/microtrafficsim/maps/Hier_wohnt_Dominic.osm");

    SwingUtilities.invokeLater(() -> {
      GUIController controller = new SimulationController(
              Scenario::new,
              new TileBasedMapViewer());
      controller.transiate(GUIEvent.CREATE, file);
    });
  }

  private static void printUsage() {
    System.out.println("");
    System.out.println("MicroTrafficSim - GUI.");
    System.out.println("");
    System.out.println("usage:");
    System.out.println("  microtrafficsim                Run this example without a map");
    System.out.println("  microtrafficsim <file>         Run this example with the specified map-file");
    System.out.println("  microtrafficsim --help | -h    Show this help message.");
    System.out.println("");
  }
}