package microtrafficsim.ui.preferences.model;

import java.util.ArrayList;

import microtrafficsim.core.map.style.StyleSheet;
import microtrafficsim.core.map.style.impl.DarkMonochromeStyleSheet;
import microtrafficsim.core.map.style.impl.DarkStyleSheet;
import microtrafficsim.core.map.style.impl.LightMonochromeStyleSheet;
import microtrafficsim.core.map.style.impl.LightStyleSheet;

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
        styleSheets.add(DarkMonochromeStyleSheet.class);
        styleSheets.add(LightMonochromeStyleSheet.class);
    }


    public ArrayList<Class<? extends StyleSheet>> getStyleSheets() {
        return new ArrayList<>(styleSheets);
    }

    public StyleSheet instantiate(int selectedIndex) {
        try {
            return styleSheets.get(selectedIndex).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
