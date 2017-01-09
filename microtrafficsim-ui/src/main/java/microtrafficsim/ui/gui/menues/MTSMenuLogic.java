package microtrafficsim.ui.gui.menues;

import com.jogamp.newt.event.KeyEvent;
import microtrafficsim.ui.gui.statemachine.GUIController;
import microtrafficsim.ui.gui.statemachine.GUIEvent;

import javax.swing.*;


/**
 * @author Dominic Parga Cacheiro
 */
public class MTSMenuLogic extends JMenu {
    public final JMenuItem itemEditSim, itemNewSim, itemRunPause, itemRunOneStep;
    private boolean isPaused;

    public MTSMenuLogic() {
        super("Logic");

        itemRunPause = new JMenuItem("Run/Pause");
        add(itemRunPause);

        itemRunOneStep = new JMenuItem("Run one step");
        add(itemRunOneStep);

        itemEditSim = new JMenuItem("Edit simulation parameters...");
        add(itemEditSim);

        itemNewSim = new JMenuItem("New simulation...");
        add(itemNewSim);
    }

    public void simIsPaused(boolean isPaused) {
        this.isPaused = isPaused;
        itemRunPause.setText(isPaused ? "Run" : "Pause");
    }

    public void addActions(GUIController guiController) {
        itemRunPause.addActionListener(e -> guiController.transiate(isPaused ? GUIEvent.RUN_SIM : GUIEvent.PAUSE_SIM));
        itemRunOneStep.addActionListener(e -> guiController.transiate(GUIEvent.RUN_SIM_ONE_STEP));
        itemEditSim.addActionListener(e -> guiController.transiate(GUIEvent.EDIT_SIM));
        itemNewSim.addActionListener(e -> guiController.transiate(GUIEvent.NEW_SIM));

        guiController.addKeyCommand(KeyEvent.EVENT_KEY_RELEASED,
                                    KeyEvent.VK_SPACE,
                                    e -> guiController.transiate(isPaused ? GUIEvent.RUN_SIM : GUIEvent.PAUSE_SIM));

        guiController.addKeyCommand(KeyEvent.EVENT_KEY_RELEASED,
                                    KeyEvent.VK_RIGHT,
                                    e -> guiController.transiate(GUIEvent.RUN_SIM_ONE_STEP));

        guiController.addKeyCommand(KeyEvent.EVENT_KEY_RELEASED,
                                    KeyEvent.VK_COMMA,
                                    e -> guiController.transiate(GUIEvent.EDIT_SIM));

        guiController.addKeyCommand(KeyEvent.EVENT_KEY_RELEASED,
                                    KeyEvent.VK_N,
                                    e -> guiController.transiate(GUIEvent.NEW_SIM));
    }
}
