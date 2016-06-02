package microtrafficsim.ui.core;

import com.jogamp.newt.event.KeyEvent;

import javax.swing.*;

/**
 * @author Dominic Parga Cacheiro
 */
class MTSMenuLogic extends JMenu {

    JMenuItem itemChangeSimParams, itemPrepareSim, itemRun, itemRunOneStep;

    public MTSMenuLogic() {
        super("Logic");

        itemChangeSimParams = new JMenuItem("Change simulation parameters...");
        add(itemChangeSimParams);

        itemPrepareSim = new JMenuItem("Prepare simulation");
        add(itemPrepareSim);

        itemRun = new JMenuItem("Run/Pause");
        add(itemRun);

        itemRunOneStep = new JMenuItem("Run one step");
        add(itemRunOneStep);
    }

    public void addActions(MTSMenuBar menubar) {

        itemChangeSimParams.addActionListener(e -> menubar.chef.showPreferences());
        menubar.chef.addKeyCommand(
                KeyEvent.EVENT_KEY_PRESSED,
                KeyEvent.VK_COMMA,
                e -> menubar.chef.showPreferences()
        );
        itemPrepareSim.addActionListener(e -> menubar.chef.createNewSimulation());
        itemRun.addActionListener(e -> menubar.chef.runSimulation());
        menubar.chef.addKeyCommand(
                KeyEvent.EVENT_KEY_PRESSED,
                KeyEvent.VK_SPACE,
                e -> menubar.chef.runSimulation());
        itemRunOneStep.addActionListener(e -> menubar.chef.runOneStep());
        menubar.chef.addKeyCommand(
                KeyEvent.EVENT_KEY_PRESSED,
                KeyEvent.VK_RIGHT,
                e -> menubar.chef.runOneStep());
    }
}
