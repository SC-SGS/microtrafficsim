package microtrafficsim.ui;

import microtrafficsim.ui.gui.statemachine.GUIController;
import microtrafficsim.ui.gui.statemachine.GUIEvent;
import microtrafficsim.ui.gui.statemachine.impl.SimulationController;
import microtrafficsim.utils.logging.LoggingLevel;

import javax.swing.*;
import java.io.File;


/**
 * Just contains the main-method starting an instance of {@link SimulationController}.
 *
 * @author Maximilian Luz, Dominic Parga Cacheiro
 */
public class Main {
//    public static final File OPTIONAL_MAP_FILE = new File("/Users/Dominic/Documents/Studium/Bachelor_of_Disaster/microtrafficsim/simulation_files/maps/Backnang.osm");
    public static final File OPTIONAL_MAP_FILE = null;


    public static void main(String[] args) throws Exception {
//        LoggingLevel.setEnabledGlobally(false, true, true, true, true);
        LoggingLevel.setEnabledGlobally(false, false, false, false, false);

        /* handle input arguments */
        final File file;
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
            file = OPTIONAL_MAP_FILE;
        }

        /*
        --------------
        | UI options
        --------------
        // General
        PrefElement.sliderSpeedup.setEnabled(true);
        PrefElement.maxVehicleCount.setEnabled(false);
        PrefElement.seed.setEnabled(true);
        PrefElement.metersPerCell.setEnabled(false);
        // Visualization
        // crossing logic
        PrefElement.edgePriority.setEnabled(true);
        PrefElement.priorityToThe.setEnabled(true);
        PrefElement.onlyOneVehicle.setEnabled(true);
        PrefElement.friendlyStandingInJam.setEnabled(true);
        // concurrency
        PrefElement.nThreads.setEnabled(false);
        PrefElement.vehiclesPerRunnable.setEnabled(false);
        PrefElement.nodesPerThread.setEnabled(false);
        */

        System.setProperty("awt.useSystemAAFontSettings","on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            GUIController controller = new SimulationController();
            if (file != null)
                controller.transiate(GUIEvent.LOAD_MAP, file);
        });
    }

    private static void printUsage() {
        System.out.println("");
        System.out.println("MicroTrafficSim - GUI Example.");
        System.out.println("");
        System.out.println("usage:");
        System.out.println("  microtrafficsim                Run this example without a map");
        System.out.println("  microtrafficsim <file>         Run this example with the specified map-file");
        System.out.println("  microtrafficsim --help | -h    Show this help message.");
        System.out.println("");
    }
}