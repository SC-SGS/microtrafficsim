package microtrafficsim.core.logic.vehicles.impl;

import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.logic.vehicles.VehicleStateListener;
import microtrafficsim.utils.id.LongIDGenerator;

/**
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class Car extends AbstractVehicle {

    // AbstractVehicle
	public static int maxVelocity = 6;
	private static float dawdleFactor = 0f;
	private static float dashFactor = 0f;
    // Hulk
    public static int maxAnger = Integer.MAX_VALUE;
    private int anger;
    private int totalAnger;

    public Car(LongIDGenerator longIDGenerator, VehicleStateListener stateListener, Route route) {
        super(longIDGenerator, stateListener, route);
        anger = 0;
        totalAnger = 0;
    }

    public Car(LongIDGenerator longIDGenerator, VehicleStateListener stateListener, Route route, int spawnDelay) {
        super(longIDGenerator, stateListener, route, spawnDelay);
        anger = 0;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	@Override
	protected float getDashFactor() {
		return dashFactor;
	}

    public static void setDashFactor(float dashFactor) {
        Car.dashFactor = dashFactor;
        try {
            validateDashAndDawdleFactors(Car.dashFactor, Car.dawdleFactor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setDashAndDawdleFactor(float dashFactor, float dawdleFactor) {
        Car.dashFactor = dashFactor;
        Car.dawdleFactor = dawdleFactor;
        try {
            validateDashAndDawdleFactors(Car.dashFactor, Car.dawdleFactor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}