package microtrafficsim.ui.core;

import javax.swing.*;
import java.awt.*;

/**
 * @author Dominic Parga Cacheiro
 */
public class MTSMenuBar extends JMenuBar {

    final SimulationChef chef;
    final MTSMenuMap menuMap;
    final MTSMenuLogic menuLogic;

    public MTSMenuBar(SimulationChef chef) {

        this.chef = chef;
        menuMap = new MTSMenuMap();
        add(menuMap);
        menuLogic = new MTSMenuLogic();
        add(menuLogic);
    }

    public MTSMenuBar create() {

        menuMap.addActions(this);
        menuLogic.addActions(this);

        return this;
    }
}
