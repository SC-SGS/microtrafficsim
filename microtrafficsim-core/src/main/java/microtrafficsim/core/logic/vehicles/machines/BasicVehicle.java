package microtrafficsim.core.logic.vehicles.machines;

import java.util.LinkedList;
import java.util.function.Function;

import org.slf4j.Logger;

import microtrafficsim.core.entities.vehicle.VehicleEntity;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.logic.vehicles.VehicleStateListener;
import microtrafficsim.core.logic.vehicles.driver.Driver;
import microtrafficsim.core.map.style.VehicleStyleSheet;
import microtrafficsim.math.MathUtils;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import microtrafficsim.utils.strings.builder.LevelStringBuilder;

/**
 * @author Dominic Parga Cacheiro
 */
public abstract class BasicVehicle implements Vehicle {
    public static final Logger logger = new EasyMarkableLogger(BasicVehicle.class);


    /* general */
    private final LinkedList<VehicleStateListener> stateListeners;

    /* variable information */
    private Driver driver;
    private DirectedEdge.Lane lane;
    private int outermostTurningLaneIndex;
    private boolean laneIsCorrect;
    private LaneChangeDirection laneChangeDirection;

    /* dynamic information */
    private VehicleState state;
    private int          cellPosition;
    private int          velocity;
    private boolean      lastVelocityIsZero;

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
        laneChangeDirection = LaneChangeDirection.NONE;

        /* dynamic information */
        state               = VehicleState.NOT_SPAWNED;
        cellPosition        = -1;
        velocity            = 0;
        setLastVelocityZero(false);

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
                    .appendln("last v was zero = " + isLastVelocityZero());
            if (driver != null)
                strBuilder.appendln(driver);
            if (lane != null) {
                strBuilder.appendln(lane);
                strBuilder.appendln("lane is correct = " + laneIsCorrect);
                strBuilder.appendln("");
                strBuilder.appendln("-- infos from next node --");
                strBuilder.appendln(
                    "permission = " + lane.getEdge().getDestination().permissionToCross(this)
                );
                strBuilder.appendln("node.id = " + lane.getEdge().getDestination().getId());
            }
        }
        strBuilder.decLevel().append("</" + getClass().getSimpleName() + ">");
        return strBuilder.toString();
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    @Override
    public boolean isLastVelocityZero() {
        return lastVelocityIsZero;
    }

    private void setLastVelocityZero(boolean wasZero) {
        lastVelocityIsZero = wasZero;
    }

    public boolean isLaneCorrect() {
        return laneIsCorrect;
    }

    public void setLaneIsCorrect(boolean laneIsCorrect) {
        this.laneIsCorrect = laneIsCorrect;
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
            if (isLastVelocityZero()) driver.becomeMoreAngry();
            setLastVelocityZero(true);
        } else {
            if (!isLastVelocityZero()) driver.calmDown();
            setLastVelocityZero(false);
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


        DirectedEdge nextEdge;
        if (!driver.getRoute().isEmpty()) {
            nextEdge = driver.peekRoute();
            outermostTurningLaneIndex = lane.getDestination().findOutermostTurningLaneIndex(lane.getEdge(), nextEdge);
            setLaneIsCorrect(lane.getDestination().isLaneCorrect(lane, driver.peekRoute()));
        } else {
            outermostTurningLaneIndex = 0;
            setLaneIsCorrect(true);
        }

        assert outermostTurningLaneIndex >= 0 : "Outermost turning lane index = " + outermostTurningLaneIndex + " < 0";
    }

    private void drive() {
        lane.moveVehicle(this, velocity);
        cellPosition = cellPosition + velocity;
        if (entity.getVisualization() != null)
            entity.getVisualization().updatePosition();
    }


    private boolean wantsToOvertake(Vehicle other) {
        if (other != null) {
            int distance = other.getCellPosition() - cellPosition;
            assert distance > 0 : "Something is wrong with the data structure! " +
                    "Vehicle, expected to be at the very front, is not.";

            // traffic jam inversion
            if (
                other.isLastVelocityZero()
                || (distance < getMaxVelocity() && getVelocity() > other.getVelocity())
            ) {
                return true;
            }

            // // no traffic jam inversion
            // if (distance < getMaxVelocity()) {
            //     if (velocity > other.getVelocity() || other.getVelocity() == 0) {
            //         return true;
            //     }
            // }
        }
        return false;
    }

    private void tendToOvertaking() {
        Vehicle front = lane.getVehicleInFront(this);

        if (wantsToOvertake(front))
            checkChangeToInnerLane();
        else
            tendToOutermostLane();
    }

    private void tendToOutermostLane() {
        if (lane.getIndex() > outermostTurningLaneIndex) {
            // check for lane.isOutermost() is not necessary due to outermostTurningLaneIndex
            Vehicle outerFront = lane.getOuterLane().getVehicleInFront(this);

            if (!wantsToOvertake(outerFront))
                checkChangeToOuterLane();
        } else if (lane.getIndex() < outermostTurningLaneIndex) {
            checkChangeToInnerLane();
        }
    }

    /**
     * Executes the lane change if there is enough space (EXCLUDING the data structure operations).
     */
    private void checkChangeToOuterLane() {
        laneChangeDirection = LaneChangeDirection.OUTER;

        if (lane.isOutermost()) {
            laneChangeDirection = LaneChangeDirection.NONE;
            return;
        }

        if (laneChangeDirection == LaneChangeDirection.NONE)
            return;

        Vehicle outerVehicle = lane.getOuterVehicle(this);
        if (outerVehicle != null) {
            int distance = cellPosition - outerVehicle.getCellPosition();
            assert distance >= 0 : "Something is wrong with the data structure. Distance is " + distance + ", but " +
                    "should be >= 0.";
            if (distance == 0)
                laneChangeDirection = LaneChangeDirection.NONE;
        }
    }

    /**
     * Executes the lane change if there is space (EXCLUDING the data structure operations).
     */
    private void checkChangeToInnerLane() {
        laneChangeDirection = LaneChangeDirection.INNER;

        if (lane.isInnermost()) {
            laneChangeDirection = LaneChangeDirection.NONE;
            return;
        }

        if (laneChangeDirection == LaneChangeDirection.NONE)
            return;

        // check for second inner vehicle
        Vehicle innerVehicle = lane.getSecondInnerVehicle(this);
        if (innerVehicle != null) {
            int distance = cellPosition - innerVehicle.getCellPosition();
            assert distance >= 0 : "Something is wrong with the data structure. Distance is " + distance + ", but " +
                    "should be >= 0.";
            if (distance == 0) {
                laneChangeDirection = LaneChangeDirection.NONE;
                return;
            }
        }

        // check for inner vehicle
        innerVehicle = lane.getInnerVehicle(this);
        if (innerVehicle != null) {
            int distance = cellPosition - innerVehicle.getCellPosition();
            assert distance >= 0 : "Something is wrong with the data structure. Distance is " + distance + ", but " +
                    "should be >= 0.";
            if (distance == 0)
                laneChangeDirection = LaneChangeDirection.NONE;
        }
    }

    /**
     * Executes the change in the data structure. This method does not focus on traffic logic, but only on the
     * underlying data structure.
     */
    private void changeToOuterLane() {
        lane.removeVehicle(this);
        lane = lane.getOuterLane();
        assert lane != null : "Lane after changing to outer lane is null.";
        lane.insertVehicle(this, cellPosition);
    }

    /**
     * Executes the change in the data structure. This method does not focus on traffic logic, but only on the
     * underlying data structure.
     */
    private void changeToInnerLane() {
        int index = lane.getIndex();
        lane.removeVehicle(this);
        lane = lane.getInnerLane();
        assert lane != null : "Lane after changing to inner lane is null. Old idx = " + index;
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
    public void willChangeLane() {
        laneChangeDirection = LaneChangeDirection.NONE;

        if (driver.tendToChangeLane()) {
            if (shouldGetIntoCorrectLane()) {
                tendToOutermostLane();
            } else {
                tendToOvertaking();
            }
        }
    }

    @Override
    public void changeLane() {
        if (laneChangeDirection != LaneChangeDirection.NONE) {
            if (laneChangeDirection == LaneChangeDirection.OUTER) {
                changeToOuterLane();
            }
            else if (laneChangeDirection == LaneChangeDirection.INNER) {
                changeToInnerLane();
            }

            lane.getDestination().unregisterVehicle(this);
            if (!driver.getRoute().isEmpty()) {
                setLaneIsCorrect(lane.getDestination().isLaneCorrect(lane, driver.peekRoute()));
            }
            else {
                setLaneIsCorrect(true);
            }
        }
    }

    @Override
    public void brake() {
        /* variables needed */
        Vehicle vehicleInFront;
        int distance;
        boolean isBraking = true;
        boolean shouldCheckForCorrection = true;


        /* determine case and prepare variables */
        vehicleInFront = lane.getVehicleInFront(this);


        if (vehicleInFront != null) {
            // brake for front vehicle
            distance = vehicleInFront.getCellPosition() - cellPosition;
            shouldCheckForCorrection = false;
        } else {
            // this vehicle is first in lane
            distance = lane.getEdge().getLength() - cellPosition;
            // would cross node?
            if (velocity >= distance) {
                if (!driver.getRoute().isEmpty()) {
                    if (isLaneCorrect() && lane.getDestination().permissionToCross(this)) {
                        // if next road has vehicles => brake for this
                        // else => brake for end of next road
                            shouldCheckForCorrection = false;

                        DirectedEdge.Lane nextLane
                            = lane.getDestination().getLeavingLane(lane, driver.peekRoute());
                            int maxInsertionIndex = nextLane.getMaxInsertionIndex();
                            if (maxInsertionIndex == nextLane.getLength() - 1)
                                maxInsertionIndex--;
                            distance += maxInsertionIndex + 1;
                        }
                } else {
                    shouldCheckForCorrection = false;
                }
            } else {
                isBraking = false;
                shouldCheckForCorrection = !driver.getRoute().isEmpty();
            }
        }


        /* execute case */
        if (isBraking) {
            // brake for front vehicle
            // OR brake for end of road
            // OR brake for first vehicle or end of next road
            velocity = Math.min(velocity, distance - 1);
        }

        if (shouldCheckForCorrection && velocity > 0) {
            // if vehicle reaches last cell of current lane
            assert cellPosition + velocity <= lane.getLength() - 1
                 : "Should brake for end of road if the following code should be executed.";
            if (cellPosition + velocity == lane.getLength() - 1) {
                if (!isLaneCorrect()) {
                    // if not: outermost vehicle and critical zone is empty
                    // => deadlock danger if vehicle does not brake!
                    if (
                        !(lane.containsOutermostVehicles() && lane.getEdge().isCriticalZoneEmpty())
                    ) {
                        velocity--;
                    }
                }
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

        if (shouldRegister())
            lane.getDestination().registerVehicle(this);
        else
            lane.getDestination().unregisterVehicle(this);
    }

    private boolean shouldGetIntoCorrectLane() {
        // the order of the if-statements is chosen by their runtime
        // check for:
        // - standing at the end of the road?
        // - Is route empty? If yes, vehicle doesn't have to register
        int distance = lane.getLength() - cellPosition;
        if (getMaxVelocity() >= distance)
            if (!driver.getRoute().isEmpty())
                return true;

        return false;
    }

    private boolean shouldRegister() {
        // the order of the if-statements is chosen by their runtime
        // check for:
        // - Is vehicle on the correct lane? If not, it has to change lane before register
        // - Does vehicle have front vehicles? If yes, it doesn't have to register.
        if (shouldGetIntoCorrectLane()) {
            // if route is empty => isLaneCorrect is always true but shouldGetIntoCorrectLane() is false
            if (isLaneCorrect()) {
                if (!lane.hasVehicleInFront(this)) {
                        return true;
                }
            }
        }

        return false;
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
