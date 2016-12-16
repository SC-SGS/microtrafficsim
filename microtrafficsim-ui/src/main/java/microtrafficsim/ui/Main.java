package microtrafficsim.ui;

import microtrafficsim.build.BuildSetup;
import microtrafficsim.core.simulation.scenarios.oldimpl.RandomRouteScenario;
import microtrafficsim.ui.gui.GUIController;
import microtrafficsim.ui.gui.GUIEvent;
import microtrafficsim.ui.gui.SimulationController;
import microtrafficsim.ui.vis.TileBasedMapViewer;

import javax.swing.*;
import java.io.File;


/**
 * @author Dominic Parga Cacheiro
 */
public class Main {

    public static void main(String[] args) throws Exception {

        BuildSetup.init();

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
        } else
            file = null;

        /*
        // General
        PrefElement.sliderSpeedup.setEnabled(true);
        PrefElement.ageForPause.setEnabled(false);
        PrefElement.maxVehicleCount.setEnabled(false);
        PrefElement.seed.setEnabled(true);
        PrefElement.metersPerCell.setEnabled(false);
        // Visualization
        PrefElement.projection.setEnabled(false);
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

        SwingUtilities.invokeLater(() -> {
            GUIController controller = new SimulationController(RandomRouteScenario::new, new TileBasedMapViewer());
            controller.transiate(GUIEvent.CREATE, file);
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