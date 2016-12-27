package microtrafficsim.ui.gui.menues;

import javax.swing.JMenuBar;


/**
 * @author Dominic Parga Cacheiro
 */
public class MTSMenuBar extends JMenuBar {
    public final MTSMenuMap   menuMap;
    public final MTSMenuLogic menuLogic;

    public MTSMenuBar() {
        menuMap = new MTSMenuMap();
        add(menuMap);
        menuLogic = new MTSMenuLogic();
        add(menuLogic);
    }
}