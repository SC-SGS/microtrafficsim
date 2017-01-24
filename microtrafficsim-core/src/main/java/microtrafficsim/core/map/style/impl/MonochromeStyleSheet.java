package microtrafficsim.core.map.style.impl;

import microtrafficsim.core.map.style.BasicStyleSheet;
import microtrafficsim.core.vis.opengl.utils.Color;


/**
 * A monochrome style-sheet for the MapViewer.
 *
 * @author Dominic Parga Cacheiro, Maximilian Luz
 */
public class MonochromeStyleSheet extends BasicStyleSheet {

    /**
     * @author Maximilian Luz
     */
    public interface LineWidthBaseFunction {
        float get(float offset, float base, float exp1, float exp2, int zoom);
    }
    private LineWidthBaseFunction lineWidthBase;

    @Override
    protected void initStyleCreation(String[] streetFeatureNames) {

        super.initStyleCreation(streetFeatureNames);

        lineWidthBase = (offset, base, exp1, exp2, zoom) -> {
            if (zoom >= 12)
                return offset + base * (float) Math.pow(exp1, (19 - zoom));
            else if (zoom >= 10)
                return offset + base * (float) Math.pow(exp1, (19 - 12)) + base * (float) Math.pow(exp2, 12 - zoom);
            else
                return offset + base * (float) Math.pow(exp1, (19 - 12)) - base * (float) Math.pow(exp2, 12 - 11);
        };
    }

    @Override
    public Color getBackgroundColor() {
        return Color.fromRGB(0x131313);
    }

    @Override
    protected Color getColorOutline(String streetFeatureName) {
        return Color.fromRGB(0x000000);
    }

    @Override
    protected Color getColorInline(String streetFeatureName) {

        switch (streetFeatureName) {
            case "motorway":
                return Color.fromRGB(0xA0A0A0);
            case "trunk":
                return Color.fromRGB(0x969696);
            case "primary":
                return Color.fromRGB(0x8C8C8C);
            case "secondary":
                return Color.fromRGB(0x828282);
            case "tertiary":
                return Color.fromRGB(0x787878);
            case "unclassified":
                return Color.fromRGB(0x6E6E6E);
            case "residential":
                return Color.fromRGB(0x646464);
            case "road":
                return Color.fromRGB(0x5A5A5A);
            case "living-street":
                return Color.fromRGB(0x505050);
            default: // should be never reached
                return getBackgroundColor();
        }
    }

    protected float getLineWidthOutline(String streetFeatureName, int zoom) {

        float offset = 0;
        float exp1 = 0;

        switch (streetFeatureName) {
            case "motorway":
            case "trunk":
                offset = 50.f;
                exp1   = 1.30f;
                break;
            case "primary":
            case "secondary":
            case "tertiary":
                offset = 40.f;
                exp1   = 1.20f;
                break;
            case "unclassified":
            case "residential":
            case "road":
                offset = 30.f;
                exp1   = 1.15f;
                break;
            case "living-street":
                offset = 25.f;
                exp1   = 1.10f;
                break;
        }

        return lineWidthBase.get(offset, 20.f, exp1, 0.75f, zoom);
    }

    protected float getLineWidthInline(String streetFeatureName, int zoom) {

        float offset = 0;
        float exp1 = 0;

        switch (streetFeatureName) {
            case "motorway":
            case "trunk":
                offset = 45.f;
                exp1   = 1.32f;
                break;
            case "primary":
            case "secondary":
            case "tertiary":
                offset = 35.f;
                exp1   = 1.22f;
                break;
            case "unclassified":
            case "residential":
            case "road":
                offset = 25.f;
                exp1   = 1.17f;
                break;
            case "living-street":
                offset = 20.f;
                exp1   = 1.12f;
                break;
        }

        return lineWidthBase.get(offset, 20.f, exp1, 0.3f, zoom);
    }
}