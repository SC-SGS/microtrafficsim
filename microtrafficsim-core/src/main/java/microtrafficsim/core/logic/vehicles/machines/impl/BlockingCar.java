package microtrafficsim.core.logic.vehicles.machines.impl;

import microtrafficsim.core.map.style.VehicleStyleSheet;


/**
 * This class extends the default {@code Car} by being able to stand if a boolean is set to true.
 *
 * @author Dominic Parga Cacheiro
 */
public class BlockingCar extends Car {

    private boolean blocking;

    /**
     * Default constructor.
     *
     * @see Car#Car(long, VehicleStyleSheet)
     */
    public BlockingCar(long id, VehicleStyleSheet style) {
        super(id, style);
        blocking = false;
    }

    public BlockingCar(long id, int maxVelocity, VehicleStyleSheet style) {
        super(id, maxVelocity, style);
    }

    /**
     * As the method name says, it toggles the block mode of this car. If blocking is true (false), it becomes false
     * (true). Blocking means, the vehicle is standing.
     */
    public void toggleBlockMode() {
        blocking = !blocking;
    }

    /**
     * As the method name says, it sets the block mode of this car. Blocking (<=> value == true) means, the vehicle is
     * standing.
     */
    public void setBlockMode(boolean value) {
        blocking = value;
    }

    /**
     * @return True, if this car is just standing in blocking mode; false otherwise
     */
    public boolean isBlocking() {
        return blocking;
    }

    /**
     * Addition to super-description: while blocking is true, the max velocity is set to 0.
     */
    @Override
    public int getMaxVelocity() {
        return blocking ? 0 : super.getMaxVelocity();
    }
}