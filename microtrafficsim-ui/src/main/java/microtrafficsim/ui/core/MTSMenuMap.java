package microtrafficsim.ui.core;

import microtrafficsim.ui.utils.Utils;

import javax.swing.*;
import java.io.File;

/**
 * @author Dominic Parga Cacheiro
 */
class MTSMenuMap extends JMenu {

    final JMenuItem itemOpenMap;

    public MTSMenuMap() {

        super("Map");

        itemOpenMap = new JMenuItem("Open map...");
        add(itemOpenMap);
    }

    public void addActions(MTSMenuBar menubar) {

        itemOpenMap.addActionListener(e -> {
            File f = Utils.loadMap();
            if (f != null)
                menubar.chef.asyncParse(f);
            menubar.menuLogic.itemRun.setText("Run");
        });
    }
}
