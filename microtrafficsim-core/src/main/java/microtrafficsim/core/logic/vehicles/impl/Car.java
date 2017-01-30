package microtrafficsim.core.logic.vehicles.impl;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.map.style.VehicleStyleSheet;
import microtrafficsim.interesting.emotions.Hulk;
import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.logging.EasyMarkableLogger;

import java.util.function.Function;


/**
 * <p>
 * This class represents a simple car of default values:<br>
 * &bull max speed = 5 (Nagel-Schreckenberg-Model; 135 km/h)<br>
 * &bull dawdle factor = 0.2f<br>
 * &bull dash factor = 0f<br>
 * &bull acceleration and dawdle functions as described in the Nagel-Schreckenberg-Model, which can be changed by
 * extending this class<br>
 * &bull implementation of the interface {@link Hulk}. Becoming more angry and calming down is implemented by
 * increasing/decreasing a counter of maximum equal to Integer.MAX_VALUE.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class Car extends AbstractVehicle {

    private static final EasyMarkableLogger logger = new EasyMarkableLogger(Car.class);

    /**
     * After Nagel-Schreckenberg-model; 135 km/h
     */
    public static int maxVelocity = 5;
    private static float dawdleFactor = 0.2f;
    private static float dashFactor   = 0f;

    /**
     * Hulk
     */
    public static int    maxAnger     = Integer.MAX_VALUE;
    private int          anger;
    private int          totalAnger;

    /**
     * Default constructor.
     *
     * @param ID unique ID
     * @param seed seed for {@link Random}, e.g. used for dawdling
     * @param route you only use the vehicle to drive a route
     */
    public Car(long ID, long seed, Route<Node> route, VehicleStyleSheet style) {
        super(ID, seed, route, style);
        anger      = 0;
        totalAnger = 0;
    }

    /**
     * Default constructor.
     *
     * @param ID unique ID
     * @param seed seed for {@link Random}, e.g. used for dawdling
     * @param route you only use the vehicle to drive a route
     * @param spawnDelay This vehicle spawns after this delay
     */
    public Car(long ID, long seed, Route<Node> route, int spawnDelay, VehicleStyleSheet style) {
        super(ID, seed, route, spawnDelay, style);
        anger      = 0;
        totalAnger = 0;
    }

    /*
    |==========|
    | (i) Hulk |
    |==========|
    */
    /**
     * Increases the anger by 1 up to a maximum of {@link #getMaxAnger()}
     */
    @Override
    public void becomeMoreAngry() {
        anger = Math.min(anger + 1, maxAnger);
        totalAnger += 1;
    }

    /**
     * Decreases the anger by 1 down to a minimum of 0.
     */
    @Override
    public void calmDown() {
        anger = Math.max(anger - 1, 0);
    }

    @Override
    public int getAnger() {
        return anger;
    }

    @Override
    public int getTotalAnger() {
        return totalAnger;
    }

    @Override
    public int getMaxAnger() {
        return maxAnger;
    }

    /*
    |=====================|
    | (c) AbstractVehicle |
    |=====================|
    */
    @Override
    protected Function<Integer, Integer> createAccelerationFunction() {
        // 1 - e^(-1s/15s) = 1 - 0,9355 = 0.0645
        //    return v -> (int)(0.0645f * maxVelocity + 0.9355f * v);
        return v -> v + 1;
    }

    @Override
    protected Function<Integer, Integer> createDawdleFunction() {
        // Dawdling only 5km/h
        //    return v -> (v < 5) ? 0 : (v - 5);
        return v -> (v < 1) ? 0 : v - 1;
    }

    @Override
    protected int getMaxVelocity() {
        return maxVelocity;
    }

    @Override
    protected float getDawdleFactor() {
        return dawdleFactor;
    }

    /**
     * Sets the dawdle factor to the given one. Calls {@link #validateDashAndDawdleFactors(float, float)} afterwards.
     *
     * @param dawdleFactor The probability to dawdle in one simulation step (after Nagel-Schreckenberg-model)
     */
    public static void setDawdleFactor(float dawdleFactor) {
        try {
            validateDashAndDawdleFactors(Car.dashFactor, Car.dawdleFactor);
            Car.dawdleFactor = dawdleFactor;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Dawdle factor is set wrong. Thus they are not changed.");
        }
    }

    @Override
    protected float getDashFactor() {
        return dashFactor;
    }

    /**
     * Sets the dash factor to the given one. Calls {@link #validateDashAndDawdleFactors(float, float)} before.
     *
     * @param dashFactor The probability to dash in one simulation step (addition to dawdling in
     *                   Nagel-Schreckenberg-model)
     */
    public static void setDashFactor(float dashFactor) {
        try {
            validateDashAndDawdleFactors(Car.dashFactor, Car.dawdleFactor);
            Car.dashFactor = dashFactor;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Dash factor i set wrong. Thus they are not changed.");
        }
    }

    /**
     * Sets the dawdle and dash factor to the given ones. Calls {@link #validateDashAndDawdleFactors(float, float)}
     * before.
     *
     * @param dashFactor The probability to dash in one simulation step (addition to dawdling in
     *                   Nagel-Schreckenberg-model)
     * @param dawdleFactor The probability to dawdle in one simulation step (after Nagel-Schreckenberg-model)
     */
    public static void setDashAndDawdleFactor(float dashFactor, float dawdleFactor) {
        try {
            validateDashAndDawdleFactors(dashFactor, dawdleFactor);
            Car.dashFactor   = dashFactor;
            Car.dawdleFactor = dawdleFactor;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Dash and dawdle factor are set wrong. Thus they are not changed.");
        }
    }
}