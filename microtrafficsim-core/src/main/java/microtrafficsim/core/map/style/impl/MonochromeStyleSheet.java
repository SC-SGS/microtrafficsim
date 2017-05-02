package microtrafficsim.core.map.style.impl;

import microtrafficsim.core.map.style.BasicStyleSheet;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.utils.logging.EasyMarkableLogger;


/**
 * A monochrome style-sheet for the MapViewer.
 *
 * @author Dominic Parga Cacheiro, Maximilian Luz
 */
public class MonochromeStyleSheet extends BasicStyleSheet {
    private final EasyMarkableLogger logger = new EasyMarkableLogger(MonochromeStyleSheet.class);

    private final static LineWidthBaseFunction LINE_WIDTH_BASE_FUNCTION = (offset, base, exp1, exp2, zoom) -> {
        if (zoom >= 12)
            return offset + base * (float) Math.pow(exp1, (19 - zoom));
        else if (zoom >= 10)
            return offset + base * (float) Math.pow(exp1, (19 - 12)) + base * (float) Math.pow(exp2, 12 - zoom);
        else
            return offset + base * (float) Math.pow(exp1, (19 - 12)) - base * (float) Math.pow(exp2, 12 - 11);
    };


    @Override
    public Color getBackgroundColor() {
        return Color.fromRGB(0x131313);
    }

    @Override
    protected Color getStreetOutlineColor(String streetType) {
        return Color.fromRGB(0x000000);
    }

    @Override
    protected Color getStreetInlineColor(String streetType) {
        switch (streetType) {
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
            case "living_street":
                return Color.fromRGB(0x505050);
            default: // should be never reached
                logger.info("The inline color of " + streetType + " is not defined.");
                return getBackgroundColor();
        }
    }

    protected float getStreetLineWidthOutline(String streetType, int zoom) {
        float offset = 0;
        float exp1 = 0;

        switch (streetType) {
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
            case "living_street":
                offset = 25.f;
                exp1   = 1.10f;
                break;
            default:
                logger.info("The outline line width of " + streetType + " is not defined.");
        }

        return LINE_WIDTH_BASE_FUNCTION.get(offset, 20.f, exp1, 0.75f, zoom);
    }

    protected float getStreetLineWidthInline(String streetType, int zoom) {
        float offset = 0;
        float exp1 = 0;

        switch (streetType) {
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
            case "living_street":
                offset = 20.f;
                exp1   = 1.12f;
                break;
            default:
                logger.info("The inline line width of " + streetType + " is not defined.");
        }

        return LINE_WIDTH_BASE_FUNCTION.get(offset, 20.f, exp1, 0.3f, zoom);
    }


    public interface LineWidthBaseFunction {
        float get(float offset, float base, float exp1, float exp2, int zoom);
    }
}