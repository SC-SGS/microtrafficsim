package microtrafficsim.ui.gui;

import javax.swing.JMenuBar;


/**
 * @author Dominic Parga Cacheiro
 */
public class MTSMenuBar extends JMenuBar {
    final MTSMenuMap menuMap;
    final MTSMenuLogic menuLogic;

    public MTSMenuBar() {
        menuMap = new MTSMenuMap();
        add(menuMap);
        menuLogic = new MTSMenuLogic();
        add(menuLogic);
    }
}