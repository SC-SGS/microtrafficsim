package microtrafficsim.core.logic.vehicles.impl;

import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.logic.vehicles.VehicleStateListener;
import microtrafficsim.core.simulation.configs.SimulationConfig;

import java.util.Random;
import java.util.function.Function;


/**
 * <p>
 * This class represents a simple car of default values:<br>
 * &bull max speed = 5 (Nagel-Schreckenberg-Model; 135 km/h)<br>
 * &bull dawdle factor = 0.2f<br>
 * &bull dash factor = 0f<br>
 * &bull acceleration and dawdle functions as described in the Nagel-Schreckenberg-Model, which can be changed by
 * extending this class<br>
 * &bull implementation of the interface {@link microtrafficsim.interesting.emotions.Hulk}. Becoming more angry and
 * calming down is implemented by increasing/decreasing a counter of maximum equal to Integer.MAX_VALUE.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class Car extends AbstractVehicle {

    // AbstractVehicle
    public static int maxVelocity = 5;

    // Hulk
    public static int    maxAnger     = Integer.MAX_VALUE;
    private static float dawdleFactor = 0.2f;
    private static float dashFactor   = 0f;
    private int          anger;
    private int          totalAnger;

    public Car(long ID, long seed, VehicleStateListener stateListener,
               Route<Node> route) {
        super(ID, seed, stateListener, route);
        anger      = 0;
        totalAnger = 0;
    }

    public Car(long ID, long seed, VehicleStateListener stateListener,
               Route<Node> route, int spawnDelay) {
        super(ID, seed, stateListener, route, spawnDelay);
        anger      = 0;
        totalAnger = 0;
    }

    /*
    |==========|
    | (i) Hulk |
    |==========|
    */
    @Override
    public void becomeMoreAngry() {
        anger = Math.min(anger + 1, maxAnger);
        totalAnger += 1;
    }

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

    public static void setDawdleFactor(float dawdleFactor) {
        Car.dawdleFactor = dawdleFactor;
        try {
            validateDashAndDawdleFactors(Car.dashFactor, Car.dawdleFactor);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    protected float getDashFactor() {
        return dashFactor;
    }

    public static void setDashFactor(float dashFactor) {
        Car.dashFactor = dashFactor;
        try {
            validateDashAndDawdleFactors(Car.dashFactor, Car.dawdleFactor);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void setDashAndDawdleFactor(float dashFactor, float dawdleFactor) {
        Car.dashFactor   = dashFactor;
        Car.dawdleFactor = dawdleFactor;
        try {
            validateDashAndDawdleFactors(Car.dashFactor, Car.dawdleFactor);
        } catch (Exception e) { e.printStackTrace(); }
    }
}