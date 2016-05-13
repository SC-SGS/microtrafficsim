package microtrafficsim.ui;

import com.jogamp.newt.event.KeyEvent;
import microtrafficsim.core.simulation.controller.configs.SimulationConfig;
import microtrafficsim.ui.gui.ParameterFrame;
import microtrafficsim.ui.gui.SimulationChef;
import microtrafficsim.ui.scenarios.Scenario;
import microtrafficsim.ui.vis.Example;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

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
        config.msPerTimeStep.set(1000L);
        config.maxVehicleCount = 10000;
        config.seed = 1455374755807L;
        config.multiThreading.nThreads = 8;
        config.crossingLogic.drivingOnTheRight = true;
        config.crossingLogic.edgePriorityEnabled = true;
        config.crossingLogic.priorityToTheRightEnabled = true;
        config.crossingLogic.goWithoutPriorityEnabled = true;
        config.crossingLogic.setOnlyOneVehicle(false);
        config.logger.enabled = false;
        SimulationChef chef = new SimulationChef(
                config,
                (streetgraph, vehicleFactory) -> new Scenario(config, streetgraph, vehicleFactory));

        /* setup JFrame and visualization */
        chef.prepareVisualization(new Example());

        prepareJMenuBar(config, chef);

        /* parse file */
        if (file != null)
            chef.asyncParse(file);

        /* show gui */
        chef.showGui();
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

    private static void prepareJMenuBar(SimulationConfig config, SimulationChef chef) {

        /* setup attributes for JMenuBar */
        JMenuBar menubar = new JMenuBar();
        JMenu menuMap = new JMenu("Map");
        JMenuItem itemOpenMap = new JMenuItem("Open Map...");
        JMenu menuLogic = new JMenu("Logic");
        JMenuItem itemChangeSimParams = new JMenuItem("Change simulation parameters...");
        JMenuItem itemPrepareSim = new JMenuItem("Prepare simulation");
        JMenuItem itemRun = new JMenuItem("Run");

        /* SETUP JMenuBar */
        chef.addJMenuBar(menubar);
        menubar.add(menuMap);
        menubar.add(menuLogic);

        /* ADD JMenuItems */
        menuMap.add(itemOpenMap);
        menuLogic.add(itemChangeSimParams);
        menuLogic.add(itemPrepareSim);
        menuLogic.add(itemRun);

        /* SETUP JMenu "Map" */
        itemOpenMap.addActionListener(e -> {
            File f = Utils.loadMap();
            if (f != null)
                chef.asyncParse(f);
            itemRun.setText("Run");
        });

        /* SETUP JMenu "Logic" */
        final ParameterFrame[] paramFrame = {null};
        // item change simulation parameters
        itemChangeSimParams.addActionListener(e -> {
            if (paramFrame[0] == null) {
                paramFrame[0] = ParameterFrame.create(config);
                paramFrame[0].addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        paramFrame[0] = null;
                    }
                });
            } else
                paramFrame[0].toFront();
        });
        itemChangeSimParams.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, 0));
        chef.addKeyCommand(
                KeyEvent.EVENT_KEY_PRESSED,
                KeyEvent.VK_COMMA,
                e -> {
                    if (paramFrame[0] == null) {
                        paramFrame[0] = ParameterFrame.create(config);
                        paramFrame[0].addWindowListener(new WindowAdapter() {
                            public void windowClosing(WindowEvent e) {
                                paramFrame[0] = null;
                            }
                        });
                    } else
                        paramFrame[0].toFront();
                });
        // item prepare simulation
        itemPrepareSim.addActionListener(e -> chef.asyncPrepareNewSimulation());
        // item run
        itemRun.addActionListener(e -> {
            boolean isPausedNow = chef.asyncRunSimulation();
            ((JMenuItem)e.getSource()).setText(isPausedNow ? "Run" : "Pause");
        });
        itemRun.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
        chef.addKeyCommand(
                KeyEvent.EVENT_KEY_PRESSED,
                KeyEvent.VK_SPACE,
                e -> {
                    boolean isPausedNow = chef.asyncRunSimulation();
                    itemRun.setText(isPausedNow ? "Run" : "Pause");
                });

        /* ADD JMenuBar to JToolBar */
        JToolBar toolbar = new JToolBar("Menu");
        toolbar.add(menubar);
        chef.addJToolBar(toolbar);



        //        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
//        int alt = InputEvent.ALT_DOWN_MASK;
//        itemSimParams.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, ctrl));
//        itemPrepareSim.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, alt));
//        itemRun.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0));
    }
}