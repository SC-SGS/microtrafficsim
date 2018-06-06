package microtrafficsim.core.map.style;

import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.utils.emotions.Hulk;

/**
 * @author Dominic Parga Cacheiro
 */
public interface VehicleStyleSheet {
    /**
     * @return a default color for all vehicles. Default implementation returns {@code Color.fromRGBA(0xCC4C1AF0)}.
     *
     * @see Color#fromRGBA(int)
     */
    default Color getDefaultVehicleColor() {
        return Color.fromRGBA(0xCC4C1AF0);
    }

    /**
     * <p>
     * This method returns a vehicle color depending on the vehicle itself. This method is called in
     * {@link Vehicle}, so the color is getting updated after every simulation step.
     *
     * <p>
     * The color could be dependant of the vehicle's anger based defined by {@link Hulk}
     *
     * @param vehicle The logic implementation of a vehicle. You can access its current visualization (=> current
     *                color) using {@link Vehicle#getEntity()}.
     *
     * @return The new vehicle color after the current vehicle step.
     */
    Color getColor(Vehicle vehicle);
}
