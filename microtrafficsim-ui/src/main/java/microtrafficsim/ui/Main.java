package microtrafficsim.ui;

import microtrafficsim.ui.core.SimulationChef;
import microtrafficsim.ui.scenarios.Scenario;
import microtrafficsim.ui.vis.Example;

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
            file = null;

        SwingUtilities.invokeLater(() -> {
            SimulationChef chef = new SimulationChef(Scenario::new, Example::new);
            chef.createAndShow(file);
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