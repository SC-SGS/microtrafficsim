package microtrafficsim.ui;

import microtrafficsim.core.simulation.controller.configs.SimulationConfig;
import microtrafficsim.ui.gui.SimulationChef;
import microtrafficsim.ui.vis.Example;

import java.io.File;
import java.util.Random;

/**
 * @author Dominic Parga Cacheiro
 */
public class Main {

    public static void main(String[] args) throws Exception {

        /* handle input arguments */
        File file = null;
        if (args.length == 1) {
            switch(args[0]) {
                case "-h":
                case "--help":
                    printUsage();
                    return;

                default:
                    file = new File(args[0]);
            }
        }

        /* setup config */
        SimulationConfig config = new SimulationConfig();
        config.maxVehicleCount = 10;
        config.seed = 1455374755807L;
        config.multiThreading.nThreads = 8;
        config.crossingLogic.drivingOnTheRight = true;
        config.crossingLogic.edgePriorityEnabled = true;
        config.crossingLogic.priorityToTheRightEnabled = true;
        config.crossingLogic.goWithoutPriorityEnabled = true;
        config.crossingLogic.setOnlyOneVehicle(false);
        config.logger.enabled = false;
        SimulationChef chef = new SimulationChef(config);

        /* setup JFrame and visualization */
        chef.prepareVisualization(new Example());

        /* setup gui */
        chef.addJMenuBar();
        chef.addJMenuItem(
                "Map",
                "Open Map...",
                e -> {
                    File f = Utils.loadMap();
                    if (f != null)
                        chef.asyncParse(f);
                });
        chef.addJMenuItem(
                "Logic",
                "Change simulation parameters...",
                e -> {
                    System.out.println("bla");
                });
        chef.addJMenuItem(
                "Logic",
                "Run",
                e -> {
                    chef.startSimulation();
                }
        );

        /* parse file */
        if (file != null)
            chef.asyncParse(file);

        /* show gui */
        chef.showGui();
    }

    private static void printUsage() {
        System.out.println("");
        System.out.println("MicroTrafficSim - OSM MapViewer Example.");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("  microtrafficsim                Run this example with the default map-file");
        System.out.println("                                 Default name: map.osm");
        System.out.println("  microtrafficsim <file>         Run this example with the specified map-file");
        System.out.println("  microtrafficsim --help | -h    Show this help message.");
        System.out.println("");
    }
}