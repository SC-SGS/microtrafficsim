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
public class LightMonochromeStyleSheet extends BasicStyleSheet {
    private static final EasyMarkableLogger logger = new EasyMarkableLogger(LightMonochromeStyleSheet.class);

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
    protected float getStreetLaneWidth(String streetType, int zoom) {
        final int z = Math.max(zoom, 11);
        final double s = zoom > 11 ? 1 : Math.pow(1.95, 11 - zoom);

        return (float) ((30.0 + 5.0 * Math.pow(1.75, (19 - z))) * s);
    }

    @Override
    protected float getStreetOutlineWidth(String streetType, int zoom) {
        return (float) (1.0 * Math.pow(1.75, 19 - zoom));
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