package microtrafficsim.core.logic.vehicles.machines;

import microtrafficsim.core.entities.vehicle.LogicVehicleEntity;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.logic.vehicles.VehicleStateListener;
import microtrafficsim.core.logic.vehicles.driver.Driver;

/**
 * @see Driver
 *
 * @author Dominic Parga Cacheiro
 */
public interface Vehicle extends LogicVehicleEntity {
    /*
    |============|
    | attributes |
    |============|
    */
    long getId();

    int getCellPosition();

    Driver getDriver();

    void setDriver(Driver driver);


    /*
    |===========|
    | lifecycle |
    |===========|
    */
    void registerInGraph();

    void spawn();

    void despawn();

    VehicleState getState();

    void setState(VehicleState state);

    void addStateListener(VehicleStateListener listener);


    /*
    |===========================|
    | Nagel-Schreckenberg-model |
    |===========================|
    */
    void accelerate();

    void willChangeLane();

    void changeLane();

    void brake();

    void dawdle();

    void move();

    void didMove();


    int getVelocity();

    int getMaxVelocity();



    enum LaneChangeDirection {
        OUTER, INNER, NONE
    }
}
