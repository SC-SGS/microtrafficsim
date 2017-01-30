package microtrafficsim.core.map.style;

import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.vis.opengl.utils.Color;

/**
 * @author Dominic Parga Cacheiro
 */
public interface StyleSheet extends MapStyleSheet, VehicleStyleSheet {

    /*
    |===================|
    | (i) MapStyleSheet |
    |===================|
    */
    /**
     * @return {@link #getBackgroundColor()}
     */
    @Override
    default Color getTileBackgroundColor() {
        return getBackgroundColor();
    }

    /*
    |=======================|
    | (i) VehicleStyleSheet |
    |=======================|
    */
    /**
     * @return {@link #getDefaultVehicleColor()}
     */
    @Override
    default Color getColor(AbstractVehicle vehicle) {
        return getDefaultVehicleColor();
    }
}