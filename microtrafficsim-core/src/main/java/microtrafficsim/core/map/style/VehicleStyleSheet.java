package microtrafficsim.core.map.style;

import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.vis.opengl.utils.Color;

/**
 * @author Dominic Parga Cacheiro
 */
public interface VehicleStyleSheet {

    default Color getDefaultVehicleColor() {
        return Color.fromRGBA(0xCC4C1AF0);
    }

    Color getColor(AbstractVehicle vehicle);
}
