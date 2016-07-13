package microtrafficsim.ui.gui;

import com.jogamp.newt.event.KeyEvent;

import javax.swing.*;


/**
 * @author Dominic Parga Cacheiro
 */
class MTSMenuMap extends JMenu {

    final JMenuItem itemLoadMap;

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
