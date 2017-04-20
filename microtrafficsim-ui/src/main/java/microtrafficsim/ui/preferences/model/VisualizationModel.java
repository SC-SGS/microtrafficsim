package microtrafficsim.ui.preferences.model;

import microtrafficsim.core.map.style.StyleSheet;
import microtrafficsim.core.map.style.impl.DarkStyleSheet;
import microtrafficsim.core.map.style.impl.LightStyleSheet;
import microtrafficsim.core.map.style.impl.MonochromeStyleSheet;

import java.util.ArrayList;

/**
 * @author Dominic Parga Cacheiro
 */
public class VisualizationModel extends PreferencesModel {

    private final ArrayList<Class<? extends StyleSheet>> styleSheets;

    public VisualizationModel() {
        super("Visualization");

        styleSheets = new ArrayList<>();
        styleSheets.add(DarkStyleSheet.class);
        styleSheets.add(LightStyleSheet.class);
        styleSheets.add(MonochromeStyleSheet.class);
    }


    public ArrayList<Class<? extends StyleSheet>> getStyleSheets() {
        return new ArrayList<>(styleSheets);
    }

    public StyleSheet instantiate(int selectedIndex) {
        try {
            return styleSheets.get(selectedIndex).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }
}