package microtrafficsim.ui;

import microtrafficsim.core.map.style.impl.DarkMonochromeStyleSheet;
import microtrafficsim.core.map.style.impl.DarkStyleSheet;
import microtrafficsim.core.map.style.impl.LightMonochromeStyleSheet;
import microtrafficsim.core.map.style.impl.LightStyleSheet;
import microtrafficsim.ui.gui.statemachine.GUIController;
import microtrafficsim.ui.gui.statemachine.GUIEvent;
import microtrafficsim.ui.gui.statemachine.impl.BuildSetup;
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
    public static final File DEFAULT_MAP_FILE = new File("data/Backnang.osm");

    /**********************************************************************************************/
    /* usages */

    private static void printUnknownOption(String unknownOption) {
        printErr("Error: unknown option " + unknownOption);
    }

    private static void printOptionMissing(String arg) {
        printErr("Error: option(s) missing for arg " + arg);
    }

    private static void printErr(String errmsg) {
        System.out.println(errmsg);
        System.out.println("");
        printUsage();
    }

    private static void printUsage() {
        // each line <= 80 characters long
        String usage = "\n"
        + "MicroTrafficSim - GUI Example.\n"
        + "\n"
        + "USAGE\n"
        + "       ./gradlew :microtrafficsim-ui:run [-Dexec.args=\"<args>\"]\n"
        + "\n"
        + "DESCRIPTION\n"
        + "       GUI example for the microscopic traffic simulation.\n"
        + "\n"
        + "       Possible commands are described below.\n"
        + "       - [A] means A is optional (e.g. see USAGE)\n"
        + "       - (A|B) means A or B\n"
        + "       - <a> is kind of a variable called a (e.g. <args>, <path>)\n"
        + "\n"
        + "       -h --help\n"
        + "              Show this help message and exit.\n"
        + "\n"
        + "       -m --map <file>\n"
        + "              Run this example with the specified map-file.\n"
        + "\n"
        + "       -s --style (dark|light)[-monochrome]\n"
        + "              Use the specified style sheet (case-insensitive).\n"
        + "\n"
        + "NOTE\n"
        + "       The window background could flicker until a map is loaded.\n"
        + "\n"
        ;

        System.out.print(usage);
    }

    public static void main(String[] args) throws Exception {
        // LoggingLevel.setEnabledGlobally(true, true, true, true, true);
        LoggingLevel.setEnabledGlobally(false, false, true, true, true);

        /******************************************************************************************/
        /* cmdline parser */

        BuildSetup buildSetup = new BuildSetup();
        buildSetup.config.visualization.style = new DarkMonochromeStyleSheet();

        final File[] mapFile = { null };

        int i = 0;
        String arg;
        String val;
        while (i < args.length) {
            arg = args[i++];

            switch (arg) {
            case "-h":
            case "--help":
                printUsage();
                return;
            case "-s":
            case "--style":
                if (i >= args.length) {
                    printOptionMissing(arg);
                    return;
                }
                val = args[i++].toLowerCase();

                switch (val) {
                case "dark":
                    buildSetup.updateStyle(new DarkStyleSheet());
                    break;
                case "dark-monochrome":
                    buildSetup.updateStyle(new DarkMonochromeStyleSheet());
                    break;
                case "light":
                    buildSetup.updateStyle(new LightStyleSheet());
                    break;
                case "light-monochrome":
                    buildSetup.updateStyle(new LightMonochromeStyleSheet());
                    break;
                default:
                    printUnknownOption(arg + " " + val);
                    return;
                }
                break;
            case "-m":
            case "--map":
                val = args[i++];
                mapFile[0] = new File(val);
                break;
            default:
                printUnknownOption(arg);
                return;
            }
        }

        /******************************************************************************************/
        /* UI options */

        /*
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

        /******************************************************************************************/
        /* run */

        System.setProperty("awt.useSystemAAFontSettings","on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            GUIController controller = new SimulationController(buildSetup);
            if (mapFile[0] != null) {
                controller.transiate(GUIEvent.LOAD_MAP, mapFile[0]);
            } else {
                controller.transiate(GUIEvent.LOAD_MAP_PRIO_TO_THE_RIGHT, DEFAULT_MAP_FILE);
            }
        });
    }
}