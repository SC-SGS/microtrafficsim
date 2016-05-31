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

        itemRun = new JMenuItem();
        updateTextItemRun(false);
        add(itemRun);

        itemRunOneStep = new JMenuItem("Run one step");
        add(itemRunOneStep);
    }

    public void addActions(MTSMenuBar menubar) {

        itemChangeSimParams.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> menubar.chef.showPreferences(false));
        });
        menubar.chef.addKeyCommand(
                KeyEvent.EVENT_KEY_PRESSED,
                KeyEvent.VK_COMMA,
                e -> menubar.chef.showPreferences(false)
        );
        itemPrepareSim.addActionListener(e -> {
            menubar.chef.asyncCreateNewSimulation();
            updateTextItemRun(false);
        });
        itemRun.addActionListener(e -> {
            boolean isPausedNow = menubar.chef.asyncRunSimulation();
            updateTextItemRun(!isPausedNow);
        });
        menubar.chef.addKeyCommand(
                KeyEvent.EVENT_KEY_PRESSED,
                KeyEvent.VK_SPACE,
                e -> {
                    boolean isPausedNow = menubar.chef.asyncRunSimulation();
                    updateTextItemRun(!isPausedNow);
                });
        itemRunOneStep.addActionListener(e -> {
            menubar.chef.asyncRunOneStep();
            updateTextItemRun(false);
        });
        menubar.chef.addKeyCommand(
                KeyEvent.EVENT_KEY_PRESSED,
                KeyEvent.VK_RIGHT,
                e -> {
                    menubar.chef.asyncRunOneStep();
                    updateTextItemRun(false);
                });
    }

    void updateTextItemRun(boolean isRunning) {
        itemRun.setText(isRunning ? "Pause" : "Run");
    }
}
