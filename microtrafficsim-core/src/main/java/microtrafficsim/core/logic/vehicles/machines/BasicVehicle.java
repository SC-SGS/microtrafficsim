package microtrafficsim.core.logic.vehicles.machines;

import microtrafficsim.core.entities.vehicle.VehicleEntity;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.logic.streets.Lane;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.logic.vehicles.VehicleStateListener;
import microtrafficsim.core.logic.vehicles.driver.Driver;
import microtrafficsim.core.map.style.VehicleStyleSheet;
import microtrafficsim.exceptions.core.logic.NagelSchreckenbergException;
import microtrafficsim.math.MathUtils;
import microtrafficsim.utils.hashing.FNVHashBuilder;
import microtrafficsim.utils.strings.builder.LevelStringBuilder;

import java.util.LinkedList;
import java.util.function.Function;

/**
 * @author Dominic Parga Cacheiro
 */
public abstract class BasicVehicle implements Vehicle {

    /* general */
    private final LinkedList<VehicleStateListener> stateListeners;

    /* variable information */
    private Driver driver;
    private Lane lane;

    /* dynamic information */
    private VehicleState state;
    private int          cellPosition;
    private int          velocity;
    private boolean      lastVelocityWasZero;
    private Vehicle      vehicleInFront;
    private Vehicle      vehicleInBack;

    /* fix information */
    public final long                       id;
    private      Function<Integer, Integer> accelerate;

    /* visualization */
    private final VehicleStyleSheet style;

    /* logic vehicle entity */
    private VehicleEntity entity;

    public BasicVehicle(long id, VehicleStyleSheet style) {

        /* general */
        stateListeners = new LinkedList<>();

        /* variable information */
        lane = null;

        /* dynamic information */
        state               = VehicleState.NOT_SPAWNED;
        cellPosition        = -1;
        velocity            = 0;
        lastVelocityWasZero = false;
        vehicleInFront      = null;
        vehicleInBack       = null;

        /* fix information */
        this.id    = id;
        accelerate = createAccelerationFunction();

        /* visualization */
        this.style = style;

        /* logic vehicle entity */
        entity = null;
    }

    @Override
    public String toString() {
        LevelStringBuilder strBuilder = new LevelStringBuilder();
        strBuilder.appendln("<vehicle>");
        strBuilder.incLevel();
        strBuilder
                .appendln("id = " + id)
                .appendln("state = " + state)
                .appendln("cell position = " + cellPosition)
                .appendln("velocity v = " + velocity)
                .appendln("maximum  v = " + getMaxVelocity())
                .appendln("last v was zero = " + lastVelocityWasZero)
                .appendln("front vehicle = " + (vehicleInFront != null ? vehicleInFront.getId() : "null"))
                .appendln("back  vehicle = " + (vehicleInBack != null ? vehicleInBack.getId() : "null"));
        if (driver != null)
            strBuilder.append(driver);
        if (lane != null) {
            strBuilder.append(lane);
            strBuilder.appendln("");
            strBuilder.appendln("-- infos from next node --");
            strBuilder.appendln("permission = " + lane.getAssociatedEdge().getDestination().permissionToCross(this));
            strBuilder.appendln("node.id = " + lane.getAssociatedEdge().getDestination().id);
        }
        strBuilder.decLevel();
        strBuilder.appendln("<\\vehicle>");
        return strBuilder.toString();
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder().add(id).getHash();
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    /*
    |==========|
    | children |
    |==========|
    */
    protected abstract Function<Integer, Integer> createAccelerationFunction();

    /*
    |============|
    | simulation |
    |============|
    */
    private void didOneSimulationStep() {

        // anger
        if (velocity == 0) {
            if (lastVelocityWasZero) driver.becomeMoreAngry();
            lastVelocityWasZero = true;
        } else {
            if (!lastVelocityWasZero) driver.calmDown();
            lastVelocityWasZero = false;
        }

        // age
        driver.incTravellingTime();

        // color
        if (entity.getVisualization() != null)
            entity.getVisualization().setBaseColor(style.getColor(this));
    }

    private synchronized void addVehicleInFront(Vehicle vehicle) {
        this.vehicleInFront   = vehicle;
        vehicle.setVehicleInBack(this);
    }

    private synchronized void removeVehicleInBack() {
        if (vehicleInBack != null) {
            vehicleInBack.setVehicleInFront(vehicleInFront);
            vehicleInBack = null;
        }
    }

    private void leaveCurrentRoad() {
        lane.getAssociatedEdge().getDestination().unregisterVehicle(this);
        lane.lock.lock();
            lane.removeVehicle(this);
            removeVehicleInBack();
        lane.lock.unlock();

        // -1 * distance to end of road
        cellPosition = cellPosition - lane.getAssociatedEdge().getLength();
    }

    private void enterNextRoad() {
        lane = driver.popRoute().getLane(0);
        lane.lock.lock();
            Vehicle lastVehicle = lane.getLastVehicle();
            if (lastVehicle != null) { addVehicleInFront(lastVehicle); }
        lane.lock.unlock();
        cellPosition = cellPosition + velocity;
        lane.insertVehicle(this, cellPosition);
        if (entity.getVisualization() != null)
            entity.getVisualization().updatePosition();
    }

    private void drive() {
        lane.moveVehicle(this, velocity);
        cellPosition = cellPosition + velocity;
        if (entity.getVisualization() != null)
            entity.getVisualization().updatePosition();
    }

    /*
    |=============|
    | (i) Vehicle |
    |=============|
    */
    @Override
    public void registerInGraph() {
        if (!driver.getRoute().isEmpty())
            driver.getRoute().getStart().registerVehicle(this);
    }

    /**
     * If this vehicle is not spawned yet, this method checks if the route is
     * empty. If yes, the vehicle will despawn instantly. If no, it has to
     * check, if it can cross the node.
     */
    @Override
    public void spawn() {
        if (state == VehicleState.NOT_SPAWNED) {
            if (driver.getTravellingTime() >= 0) {
                if (!driver.getRoute().isEmpty()) {
                    if (!driver.getRoute().getStart().permissionToCross(this)) {
                        velocity = 0;
                    } else {    // allowed to spawn
                        if (driver.peekRoute().getLane(0).getMaxInsertionIndex() < 0) {
                            velocity = 0;
                        } else {
                            velocity = 1;
                            driver.getRoute().getStart().unregisterVehicle(this);
                            enterNextRoad();
                            setState(VehicleState.SPAWNED);
                        }
                    }
                } else {    // route is empty
                    velocity = 0;

                    despawn();
                    return;
                }
            }
            didOneSimulationStep();
        }
    }

    @Override
    public void despawn() {
        lane = null;
        setState(VehicleState.DESPAWNED);
    }

    @Override
    public VehicleState getState() {
        return state;
    }

    @Override
    public void setState(VehicleState state) {
        this.state = state;
        for (VehicleStateListener listener : stateListeners)
            listener.stateChanged(this);
    }

    @Override
    public void addStateListener(VehicleStateListener listener) {
        if (listener != null)
            stateListeners.add(listener);
    }

    @Override
    public void accelerate() {
        int vVehicle = accelerate.apply(velocity);
        int vDriver = driver.accelerate(velocity);
        velocity = Math.min(vVehicle, vDriver);
        velocity = MathUtils.clamp(velocity, 0, getMaxVelocity());
    }

    @Override
    public void brake() throws NagelSchreckenbergException {
        if (state == VehicleState.SPAWNED) {
            if (vehicleInFront != null) {
                // brake for front vehicle
                int distance = vehicleInFront.getCellPosition() - cellPosition;
                velocity     = Math.min(velocity, distance - 1);
            } else {    // this vehicle is first in lane
                DirectedEdge edge     = lane.getAssociatedEdge();
                int          distance = edge.getLength() - cellPosition;
                // Would cross node?
                if (velocity >= distance)
                    if (driver.getRoute().isEmpty()) {
                        // brake for end of road
                        velocity = Math.min(velocity, distance - 1);
                    } else {
                        if (edge.getDestination().permissionToCross(this)) {
                            // if next road has vehicles => brake for this
                            // else => brake for end of next road
                            int maxInsertionIndex = driver.peekRoute().getLane(0).getMaxInsertionIndex();
                            velocity              = Math.min(velocity, distance + maxInsertionIndex);
                        } else {
                            // brake for end of road
                            velocity = Math.min(velocity, distance - 1);
                        }
                    }
            }

            // brake for edges max velocity
//            velocity = Math.min(velocity, lane.getAssociatedEdge().getMaxVelocity());
        }

        if (velocity < 0)
            throw NagelSchreckenbergException.velocityLessThanZero(NagelSchreckenbergException.Step.brake, velocity);
    }

    @Override
    public void dawdle() {
        if (velocity > 0 && state == VehicleState.SPAWNED) {
            int newVelocity = driver.dawdle(velocity);
            if (newVelocity > velocity) {
                try {
                    throw new NagelSchreckenbergException(
                            NagelSchreckenbergException.Step.dawdle,
                            "v_after_dawdling=" + newVelocity + " > " + velocity + "=v_before_dawdling, which is not " +
                            "allowed.");
                } catch (NagelSchreckenbergException e) {
                    e.printStackTrace();
                }
            }
            velocity = newVelocity;
            velocity = MathUtils.clamp(velocity, 0, getMaxVelocity());
        }
    }

    @Override
    public void move() {
        if (state == VehicleState.SPAWNED) {
            DirectedEdge edge     = lane.getAssociatedEdge();
            int          distance = edge.getLength() - getCellPosition();
            // Will cross node?
            if (velocity >= distance)
                if (!driver.getRoute().isEmpty()) {
                    leaveCurrentRoad();
                    enterNextRoad();
                } else {
                    leaveCurrentRoad();
                    despawn();
                }
            else {
                // if standing at the end of the road
                // and route is empty
                // => despawn
                if (velocity == 0 && distance == 1 && driver.getRoute().isEmpty()) {
                    leaveCurrentRoad();
                    despawn();
                } else {
                    drive();
                }
            }
        }
    }

    @Override
    public void didMove() {
        if (state == VehicleState.SPAWNED) {
            didOneSimulationStep();

            if (!driver.getRoute().isEmpty()) {
                int distance    = lane.getAssociatedEdge().getLength() - cellPosition;
                int maxVelocity = Math.min(getMaxVelocity(), lane.getAssociatedEdge().getMaxVelocity());
                if (maxVelocity >= distance && vehicleInFront == null)
                    lane.getAssociatedEdge().getDestination().registerVehicle(this);
            }
        }
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public int getCellPosition() {
        return cellPosition;
    }

    @Override
    public Driver getDriver() {
        return driver;
    }

    @Override
    public void setVehicleInFront(Vehicle vehicleInFront) {
        this.vehicleInFront = vehicleInFront;
    }

    @Override
    public void setVehicleInBack(Vehicle vehicleInBack) {
        this.vehicleInBack = vehicleInBack;
    }

    /*
    |========================|
    | (i) LogicVehicleEntity |
    |========================|
    */
    @Override
    public VehicleEntity getEntity() {
        return entity;
    }

    @Override
    public void setEntity(VehicleEntity entity) {
        this.entity = entity;
    }

    @Override
    public DirectedEdge getDirectedEdge() {
        if (lane == null) return null;
        return lane.getAssociatedEdge();
    }

    @Override
    public Lane getLane() {
        return lane;
    }
}
