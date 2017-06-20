package microtrafficsim.core.map.style.impl;

import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.map.style.BasicStyleSheet;
import microtrafficsim.core.map.style.VehicleColorSchemes;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.math.MathUtils;
import microtrafficsim.utils.logging.EasyMarkableLogger;


/**
 * A monochrome style-sheet for the MapViewer.
 *
 * @author Dominic Parga Cacheiro, Maximilian Luz
 */
public class DarkMonochromeStyleSheet extends BasicStyleSheet {
    private static final EasyMarkableLogger logger = new EasyMarkableLogger(DarkMonochromeStyleSheet.class);

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

    @Override
    protected float getStreetLaneWidth(String streetType, int zoom) {
        float offset = 0;
        float exp1 = 0;

        switch (streetType) {
            case "motorway":
            case "trunk":
                offset = 45.0f;
                exp1   = 1.4f;
                break;
            case "primary":
            case "secondary":
            case "tertiary":
                offset = 42.5f;
                exp1   = 1.3f;
                break;
            case "unclassified":
            case "residential":
            case "road":
                offset = 40.0f;
                exp1   = 1.2f;
                break;
            case "living_street":
                offset = 37.5f;
                exp1   = 1.15f;
                break;
            default:
                logger.info("The lane width of " + streetType + " is not defined.");
        }

        return LINE_WIDTH_BASE_FUNCTION.get(offset * 12, 20.f, exp1, 0.3f, zoom);
    }

    @Override
    protected float getStreetOutlineWidth(String streetType, int zoom) {
        return LINE_WIDTH_BASE_FUNCTION.get(20.f, 22.5f, 1.5f, 0.3f, zoom);
    }

    @Override
    public Color getColor(Vehicle vehicle) {
        Color[] colors = VehicleColorSchemes.RED_TO_GREEN;
        int v = MathUtils.clamp(vehicle.getVelocity(), 0, colors.length - 1);
        return colors[v];
    }

    public interface LineWidthBaseFunction {
        float get(float offset, float base, float exp1, float exp2, int zoom);
    }
}