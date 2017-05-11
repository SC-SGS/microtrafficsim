package microtrafficsim.core.logic.vehicles.machines;

import microtrafficsim.core.entities.vehicle.LogicVehicleEntity;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.logic.vehicles.VehicleStateListener;
import microtrafficsim.core.logic.vehicles.driver.Driver;
import microtrafficsim.core.logic.NagelSchreckenbergException;

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

    void setVehicleInFront(Vehicle vehicleInFront);

    void setVehicleInBack(Vehicle vehicleInBack);

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

    void brake() throws NagelSchreckenbergException;

    void dawdle();

    void move();

    void didMove();


    int getVelocity();

    int getMaxVelocity();
}
