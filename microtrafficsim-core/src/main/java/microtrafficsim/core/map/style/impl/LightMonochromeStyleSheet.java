package microtrafficsim.core.map.style.impl;

import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.map.style.BasicStyleSheet;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.utils.logging.EasyMarkableLogger;


/**
 * A monochrome style-sheet for the MapViewer.
 *
 * @author Dominic Parga Cacheiro, Maximilian Luz
 */
public class LightMonochromeStyleSheet extends BasicStyleSheet {
    private final EasyMarkableLogger logger = new EasyMarkableLogger(LightMonochromeStyleSheet.class);

    private final static LineWidthBaseFunction LINE_WIDTH_BASE_FUNCTION = (offset, base, exp1, exp2, zoom) -> {
        if (zoom >= 12)
            return offset + base * (float) Math.pow(exp1, (19 - zoom));
        else if (zoom >= 10)
            return offset + base * (float) Math.pow(exp1, (19 - 12)) + base * (float) Math.pow(exp2, 12 - zoom);
        else
            return offset + base * (float) Math.pow(exp1, (19 - 12)) - base * (float) Math.pow(exp2, 12 - 11);
    };

    private final static Color[] VEHICLE_COLORS = {
            Color.fromRGB(0xa33a3a),
            Color.fromRGB(0x72a33a),
            Color.fromRGB(0x3aa372),
            Color.fromRGB(0x3a72a3),
            Color.fromRGB(0x3a3aa3),
            Color.fromRGB(0x342059),
    };


    @Override
    public Color getBackgroundColor() {
        return Color.fromRGB(0xF8F8F8);
    }

    @Override
    protected Color getStreetOutlineColor(String streetType) {
        switch (streetType) {
            case "motorway":
            case "trunk":
            case "primary":
            case "secondary":
            case "tertiary":
                return Color.fromRGB(0x000000);
            case "unclassified":
            case "residential":
                return Color.fromRGB(0x505050);
            case "road":
                return Color.fromRGB(0x909090);
            case "living_street":
                return Color.fromRGB(0x707070);
            default: // should be never reached
                logger.info("The inline color of " + streetType + " is not defined.");
                return getBackgroundColor();
        }
    }

    @Override
    protected Color getStreetInlineColor(String streetType) {
        switch (streetType) {
            case "motorway":
                return Color.fromRGB(0xC0C0C0);
            case "trunk":
                return Color.fromRGB(0xC8C8C8);
            case "primary":
                return Color.fromRGB(0xD0D0D0);
            case "secondary":
                return Color.fromRGB(0xD8D8D8);
            case "tertiary":
                return Color.fromRGB(0xE0E0E0);
            case "unclassified":
            case "residential":
            case "road":
            case "living_street":
                return Color.fromRGB(0xF0F0F0);
            default: // should be never reached
                logger.info("The inline color of " + streetType + " is not defined.");
                return getBackgroundColor();
        }
    }

    @Override
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

    @Override
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


    @Override
    public Color getColor(Vehicle vehicle) {
        int v = vehicle.getVelocity();

        if (v < 0) return VEHICLE_COLORS[0];
        if (v >= VEHICLE_COLORS.length) return VEHICLE_COLORS[VEHICLE_COLORS.length];

        return VEHICLE_COLORS[v];
    }


    public interface LineWidthBaseFunction {
        float get(float offset, float base, float exp1, float exp2, int zoom);
    }
}