package microtrafficsim.core.logic.vehicles.machines.impl;

import microtrafficsim.core.logic.vehicles.machines.BasicVehicle;
import microtrafficsim.core.map.style.VehicleStyleSheet;
import microtrafficsim.utils.logging.EasyMarkableLogger;

import java.util.function.Function;

/**
 * <p>
 * This class represents a simple car of default values:<br>
 * <ul>
 *     <li>{@code max velocity} (Nagel-Schreckenberg-Model: 5 <=> 135 km/h)
 *     <li>{@code dawdle factor}
 *     <li>acceleration and dawdle functions as described in the Nagel-Schreckenberg-Model, which can be changed by
 *     extending this class
 * </ul>
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class Car extends BasicVehicle {

    private static final EasyMarkableLogger logger = new EasyMarkableLogger(Car.class);

    /**
     * After Nagel-Schreckenberg-model; default is 135 km/h
     */
    private final int maxVelocity;

    /**
     * Calls {@link #Car(long, int, VehicleStyleSheet) Car(id, 5, style)}
     */
    public Car(long id, VehicleStyleSheet style) {
        this(id, 5, style);
    }

    /**
     * Default constructor.
     *
     * @param id          unique id
     * @param maxVelocity the 'physical' max velocity of this vehicle independent of crossing logic etc.
     * @param style       the vehicle's visualization style. Can be null.
     */
    public Car(long id, int maxVelocity, VehicleStyleSheet style) {
        super(id, style);
        this.maxVelocity = maxVelocity;
    }

    /*
    |==================|
    | (c) BasicVehicle |
    |==================|
    */
    @Override
    protected Function<Integer, Integer> createAccelerationFunction() {
        // 1 - e^(-1s/15s) = 1 - 0,9355 = 0.0645
        //    return v -> (int)(0.0645f * maxVelocity + 0.9355f * v);
        return v -> v + 1;
    }

    @Override
    public int getMaxVelocity() {
        return Math.min(maxVelocity, getDriver().getMaxVelocity());
    }
}
