package microtrafficsim.core.logic.vehicles.machines;

import microtrafficsim.core.entities.vehicle.VehicleEntity;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.logic.vehicles.VehicleStateListener;
import microtrafficsim.core.logic.vehicles.driver.Driver;
import microtrafficsim.core.map.style.VehicleStyleSheet;
import microtrafficsim.math.MathUtils;
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
    private DirectedEdge.Lane lane;

    /* dynamic information */
    private VehicleState state;
    private int          cellPosition;
    private int          velocity;
    private boolean      lastVelocityWasZero;

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
        LevelStringBuilder strBuilder = new LevelStringBuilder()
                .setDefaultLevelSeparator()
                .setDefaultLevelSubString();
        strBuilder.appendln("<" + getClass().getSimpleName() + ">").incLevel();
        {
            strBuilder
                    .appendln("id = " + id)
                    .appendln("state = " + state)
                    .appendln("cell position = " + cellPosition)
                    .appendln("velocity v = " + velocity)
                    .appendln("maximum  v = " + getMaxVelocity())
                    .appendln("last v was zero = " + lastVelocityWasZero);
            if (driver != null)
                strBuilder.appendln(driver);
            if (lane != null) {
                strBuilder.appendln(lane);
                strBuilder.appendln("");
                strBuilder.appendln("-- infos from next node --");
                strBuilder.appendln("permission = " + lane.getEdge().getDestination().permissionToCross(this));
                strBuilder.appendln("node.id = " + lane.getEdge().getDestination().getId());
            }
        }
        strBuilder.decLevel().appendln("</" + getClass().getSimpleName() + ">");
        return strBuilder.toString();
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


    private void leaveCurrentRoad() {
        lane.getDestination().unregisterVehicle(this);
        lane.removeVehicle(this);

        // -1 * distance to end of road
        cellPosition = cellPosition - lane.getLength();
    }

    private void enterNextRoad() {
        enterNextRoad(lane.getDestination().getLeavingLane(lane, driver.popRoute()));
    }

    private void enterNextRoad(DirectedEdge.Lane nextLane) {
        lane = nextLane;
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

    private void changeToOuterLane() {
        lane.removeVehicle(this);
        lane = lane.getOuterLane();
        lane.insertVehicle(this, cellPosition);
    }

    private void changeToInnerLane() {
        lane.removeVehicle(this);
        lane = lane.getInnerLane();
        lane.insertVehicle(this, cellPosition);
    }

    /*
    |=============|
    | (i) Vehicle |
    |=============|
    */
    @Override
    public void registerInGraph() {
        if (!driver.getRoute().isEmpty())
            driver.getRoute().getOrigin().registerVehicle(this);
    }

    /**
     * If this vehicle is not spawned yet, this method checks if the route is
     * empty. If yes, the vehicle will despawn instantly. If no, it has to
     * check, if it can cross the node.
     */
    @Override
    public void spawn() {
        if (driver.getTravellingTime() >= 0) {
            if (!driver.getRoute().isEmpty()) {
                if (!driver.getRoute().getOrigin().permissionToCross(this)) {
                    velocity = 0;
                } else {    // allowed to spawn
                    if (driver.peekRoute().getLane(0).getMaxInsertionIndex() < 0) {
                        velocity = 0;
                    } else {
                        velocity = 1;
                        driver.getRoute().getOrigin().unregisterVehicle(this);
                        enterNextRoad(driver.popRoute().getLane(0));
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
    public void changeLane() {
        // todo multilane
        if (!lane.isOutermost()) {
            boolean willChangeLane = true;
            int threshold = 1;

            Vehicle outerVehicle = lane.getOuterVehicle(this);
            if (outerVehicle != null)
                if (cellPosition - outerVehicle.getCellPosition() <= threshold)
                    willChangeLane = false;

            if (willChangeLane)
                changeToOuterLane();
        }
    }

    @Override
    public void brake() {
        Vehicle vehicleInFront = lane.getVehicleInFront(this);
        if (vehicleInFront != null) {
            // brake for front vehicle
            int distance = vehicleInFront.getCellPosition() - cellPosition;
            velocity     = Math.min(velocity, distance - 1);
        } else {    // this vehicle is first in lane
            int distance = lane.getEdge().getLength() - cellPosition;
            // Would cross node?
            if (velocity >= distance) {
                boolean brakeForEndOfRoad = true;

                if (!driver.getRoute().isEmpty()) {
                    if (lane.getDestination().permissionToCross(this)) {
                        // if next road has vehicles => brake for this
                        // else => brake for end of next road
                        DirectedEdge.Lane nextLane = lane.getDestination().getLeavingLane(lane, driver.peekRoute());
                        if (nextLane != null) { // not correct lane
                            int maxInsertionIndex = nextLane.getMaxInsertionIndex();
                            velocity = Math.min(velocity, distance + maxInsertionIndex);
                            brakeForEndOfRoad = false;
                        }
                    }
                }

                if (brakeForEndOfRoad)
                    velocity = Math.min(velocity, distance - 1);
            }
        }

        assert velocity >= 0 : "Velocity < 0 in braking. Actual = " + velocity;
    }

    @Override
    public void dawdle() {
        if (velocity > 0) {
            int newVelocity = driver.dawdle(velocity);

            assert newVelocity <= velocity
                    : "v_after_dawdling=" + newVelocity + " > " + velocity + "=v_before_dawdling";

            velocity = newVelocity;
            velocity = MathUtils.clamp(velocity, 0, getMaxVelocity());
        }
    }

    @Override
    public void move() {
        int distance = lane.getEdge().getLength() - getCellPosition();
        // Will cross node?
        if (velocity >= distance) {
            leaveCurrentRoad();
            if (driver.getRoute().isEmpty())
                despawn();
            else
                enterNextRoad();
        } else {
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

    @Override
    public void didMove() {
        didOneSimulationStep();

        if (!driver.getRoute().isEmpty()) {
            if (lane.hasVehicleInFront(this)) {
                lane.getDestination().unregisterVehicle(this);
            } else {
                int distance = lane.getLength() - cellPosition;
                if (getMaxVelocity() >= distance)
                    lane.getDestination().registerVehicle(this);
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
    public int getVelocity() {
        return velocity;
    }

    @Override
    public Driver getDriver() {
        return driver;
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
    public DirectedEdge.Lane getLane() {
        return lane;
    }
}
