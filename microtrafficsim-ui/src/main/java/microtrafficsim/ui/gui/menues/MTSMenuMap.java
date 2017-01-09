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

    public MTSMenuMap() {
        super("Map");
        itemLoadMap = new JMenuItem("Load map...");
        add(itemLoadMap);
    }

    public void addActions(GUIController guiController) {
        itemLoadMap.addActionListener(e -> guiController.transiate(GUIEvent.LOAD_MAP));
        guiController.addKeyCommand(KeyEvent.EVENT_KEY_RELEASED,
                                    KeyEvent.VK_M,
                                    e -> guiController.transiate(GUIEvent.LOAD_MAP));
    }
}
