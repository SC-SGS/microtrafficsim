package microtrafficsim.core.map.style.impl;

import microtrafficsim.core.vis.opengl.utils.Color;


/**
 * A dark style-sheet for the MapViewer. It uses the colors defined in {@link LightStyleSheet} inverted.
 *
 * @author Dominic Parga Cacheiro, Maximilian Luz
 */
public class DarkStyleSheet extends LightStyleSheet {

    @Override
    public Color getBackgroundColor() {
        return super.getBackgroundColor().invert();
    }

    @Override
    protected Color getColorOutline(String streetFeatureName) {
        return super.getColorOutline(streetFeatureName).invert();
    }

    @Override
    protected Color getColorInline(String streetFeatureName) {
        return super.getColorInline(streetFeatureName).invert();
    }
}