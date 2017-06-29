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
    public double getStreetLaneWidth(int zoom) {
        final int z = Math.max(zoom, 11);
        final double s = zoom > 11 ? 1 : Math.pow(1.95, 11 - zoom);

        return (30.0 + 5.0 * Math.pow(1.75, (19 - z))) * s;
    }

    @Override
    protected double getStreetOutlineWidth(String streetType, int zoom) {
        return 1.0 * Math.pow(1.75, 19 - zoom);
    }

    @Override
    public Color getColor(Vehicle vehicle) {
        Color[] colors = VehicleColorSchemes.RED_TO_GREEN;
        int v = MathUtils.clamp(vehicle.getVelocity(), 0, colors.length - 1);
        return colors[v];
    }
}