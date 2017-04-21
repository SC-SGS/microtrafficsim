package microtrafficsim.ui.gui.menues;

import com.jogamp.newt.event.KeyEvent;
import microtrafficsim.ui.gui.statemachine.GUIController;
import microtrafficsim.ui.gui.statemachine.GUIEvent;

import javax.swing.*;


/**
 * @author Dominic Parga Cacheiro
 */
public class MTSMenuMap extends JMenu {

    public final JMenuItem itemLoadMap;
    public final JMenuItem itemSaveMap;

    public MTSMenuMap() {
        super("File");

        itemLoadMap = new JMenuItem("Load map...");
        itemSaveMap = new JMenuItem("Save map...");

        add(itemLoadMap);
        add(itemSaveMap);
    }

    public void addActions(GUIController guiController) {
        itemLoadMap.addActionListener(e -> guiController.transiate(GUIEvent.LOAD_MAP));
        guiController.addKeyCommand(KeyEvent.EVENT_KEY_RELEASED,
                                    KeyEvent.VK_M,
                                    e -> guiController.transiate(GUIEvent.LOAD_MAP));

        itemSaveMap.addActionListener(e -> guiController.transiate(GUIEvent.SAVE_MAP));
        guiController.addKeyCommand(KeyEvent.EVENT_KEY_RELEASED,
                KeyEvent.VK_W,
                e -> guiController.transiate(GUIEvent.SAVE_MAP));
    }
}
