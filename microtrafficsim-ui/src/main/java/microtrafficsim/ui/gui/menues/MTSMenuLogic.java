package microtrafficsim.ui.gui.menues;

import com.jogamp.newt.event.KeyEvent;
import microtrafficsim.ui.gui.statemachine.GUIController;
import microtrafficsim.ui.gui.statemachine.GUIEvent;

import javax.swing.*;


/**
 * @author Dominic Parga Cacheiro
 */
public class MTSMenuLogic extends JMenu {
    public final JMenuItem itemRunPause;
    public final JMenuItem itemRunOneStep;
    public final JMenuItem itemNewSim;
    public final JMenuItem itemEditSim;
    public final JMenuItem itemChangeAreaSelection;
    public final JMenuItem itemLoadRoutes;
    public final JMenuItem itemSaveRoutes;
    public final JMenuItem itemLoadAreas;
    public final JMenuItem itemSaveAreas;
    private boolean isPaused;

    public MTSMenuLogic() {
        super("Logic");

        itemRunPause = new JMenuItem("Run/Pause");
        add(itemRunPause);

        itemRunOneStep = new JMenuItem("Run one step");
        add(itemRunOneStep);

        addSeparator();

        itemEditSim = new JMenuItem("Edit simulation parameters...");
        add(itemEditSim);

        itemNewSim = new JMenuItem("New simulation...");
        add(itemNewSim);

        addSeparator();

        itemChangeAreaSelection = new JMenuItem("Change selected areas...");
        add(itemChangeAreaSelection);

        // todo add defined scenario areas to the map

        addSeparator();

        itemLoadRoutes = new JMenuItem("Load simulation routes...");
        add(itemLoadRoutes);

        itemSaveRoutes = new JMenuItem("Save simulation routes...");
        add(itemSaveRoutes);

        itemLoadAreas = new JMenuItem("Load simulation areas...");
        add(itemLoadAreas);

        itemSaveAreas = new JMenuItem("Save simulation areas...");
        add(itemSaveAreas);
    }

    public void simIsPaused(boolean isPaused) {
        this.isPaused = isPaused;
        itemRunPause.setText(isPaused ? "Run" : "Pause");
    }

    public void addActions(GUIController guiController) {
        itemRunPause.addActionListener(e -> guiController.transiate(isPaused ? GUIEvent.RUN_SIM : GUIEvent.PAUSE_SIM));
        itemRunOneStep.addActionListener(e -> guiController.transiate(GUIEvent.RUN_SIM_ONE_STEP));
        itemEditSim.addActionListener(e -> guiController.transiate(GUIEvent.EDIT_SCENARIO));
        itemNewSim.addActionListener(e -> guiController.transiate(GUIEvent.NEW_SCENARIO));
        itemChangeAreaSelection.addActionListener(e -> guiController.transiate(GUIEvent.CHANGE_AREA_SELECTION));
        itemLoadRoutes.addActionListener(e -> guiController.transiate(GUIEvent.LOAD_ROUTES));
        itemSaveRoutes.addActionListener(e -> guiController.transiate(GUIEvent.SAVE_ROUTES));
        itemLoadAreas.addActionListener(e -> guiController.transiate(GUIEvent.LOAD_AREAS));
        itemSaveAreas.addActionListener(e -> guiController.transiate(GUIEvent.SAVE_AREAS));

        /* run/pause */
        guiController.addKeyCommand(KeyEvent.EVENT_KEY_RELEASED,
                                    KeyEvent.VK_SPACE,
                                    e -> guiController.transiate(isPaused ? GUIEvent.RUN_SIM : GUIEvent.PAUSE_SIM));
        /* run one step */
        guiController.addKeyCommand(KeyEvent.EVENT_KEY_RELEASED,
                                    KeyEvent.VK_RIGHT,
                                    e -> guiController.transiate(GUIEvent.RUN_SIM_ONE_STEP));
        /* edit scenario */
        guiController.addKeyCommand(KeyEvent.EVENT_KEY_RELEASED,
                                    KeyEvent.VK_COMMA,
                                    e -> guiController.transiate(GUIEvent.EDIT_SCENARIO));
        /* create new scenario */
        guiController.addKeyCommand(KeyEvent.EVENT_KEY_RELEASED,
                                    KeyEvent.VK_N,
                                    e -> guiController.transiate(GUIEvent.NEW_SCENARIO));
        /* change area selection */
        guiController.addKeyCommand(KeyEvent.EVENT_KEY_RELEASED,
                                    KeyEvent.VK_A,
                                    e -> guiController.transiate(GUIEvent.CHANGE_AREA_SELECTION));
    }
}
