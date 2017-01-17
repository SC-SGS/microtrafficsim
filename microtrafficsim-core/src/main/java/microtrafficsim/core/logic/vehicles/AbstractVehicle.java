package microtrafficsim.core.logic.vehicles;

import microtrafficsim.core.entities.vehicle.LogicVehicleEntity;
import microtrafficsim.core.entities.vehicle.VehicleEntity;
import microtrafficsim.core.logic.DirectedEdge;
import microtrafficsim.core.logic.Lane;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.Route;
import microtrafficsim.exceptions.core.logic.NagelSchreckenbergException;
import microtrafficsim.interesting.emotions.Hulk;
import microtrafficsim.utils.hashing.FNVHashBuilder;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;


/**
 * <p>
 * This class represents a vehicle for the logic based on the
 * Nagel-Schreckenberg-model. It extends this model by a dash factor and some
 * additional information.
 * <p>
 * Additional information: <br>
 * &bull {@link Hulk}: This interface represents the current mood of the vehicle. It
 * increases for each time the vehicle has a velocity of 0 for more than
 * one simulation step in following. In opposite, the vehicle calms down when driving. <br>
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public abstract class AbstractVehicle implements LogicVehicleEntity, Hulk {

    // general
    public final long                  ID;
    private final int                  spawnDelay;
    private final Object               lock_priorityCounter = new Object();
    private Random                     random;
    private VehicleEntity              entity;
    private VehicleState               state;
    private List<VehicleStateListener> stateListeners;

    // routing
    private Route<Node> route;
    private int   age;

    // driving behaviour
    private int   cellPosition;
    private int   velocity;
    private Function<Integer, Integer> accelerate, dawdle;

    // traffic
    private Lane            lane;
    private AbstractVehicle vehicleInFront;
    private AbstractVehicle vehicleInBack;
    private boolean         hasDashed;          // for simulation
    private int             priorityCounter;    // for crossing logic

    // angry factor
    private boolean lastVelocityWasZero;


    /**
     * Calls {@code AbstractVehicle(ID, seed, route, 0)}
     *
     * @see #AbstractVehicle(long, long, Route, int)
     */
    public AbstractVehicle(long ID, long seed, Route<Node> route) {
        this(ID, seed, route, 0);
    }

    /**
     * Default constructor, but calls {@link #validateDashAndDawdleFactors(float, float)} after initializing all
     * variables.
     *
     * @param ID should be unique
     * @param seed used for dashing/dawdling
     * @param route this vehicle drives on this route
     * @param spawnDelay after this number of simulation steps, this vehicle spawns
     */
    public AbstractVehicle(long ID, long seed, Route<Node> route, int spawnDelay) {
        this.ID             = ID;
        this.random         = new Random(seed);
        this.stateListeners = new LinkedList<>();
        state = VehicleState.NOT_SPAWNED;

        // routing
        this.route = route;
        age        = 0;
        this.spawnDelay = spawnDelay;
        resetPriorityCounter();

        // driving behaviour
        cellPosition = -1;
        velocity     = 0;
        accelerate   = createAccelerationFunction();
        dawdle       = createDawdleFunction();

        // traffic
        hasDashed       = false;
        priorityCounter = 0;

        // interesting stuff
        lastVelocityWasZero = false;

        try {
            validateDashAndDawdleFactors(getDashFactor(), getDawdleFactor());
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * <p>
     * Checks: <br>
     * &bull dashFactor + dawdleFactor <= 1 <br>
     * &bull dashFactor >= 0 <br>
     * &bull dawdleFactor >= 0 <br>
     *
     * @param dashFactor The probability to dash in one simulation step (addition to dawdling in
     *                   Nagel-Schreckenberg-model)
     * @param dawdleFactor The probability to dawdle in one simulation step (after Nagel-Schreckenberg-model)
     * @throws Exception if the input is wrong.
     */
    protected static void validateDashAndDawdleFactors(float dashFactor, float dawdleFactor) throws Exception {
        if (dashFactor + dawdleFactor > 1) throw new Exception("(dash factor + dawdle factor) has to be <= 1");
        if (dashFactor < 0) throw new Exception("Dash factor has to be positive.");
        if (dawdleFactor < 0) throw new Exception("Dawdle factor has to be positive.");
    }

    /**
     * @return hashcode depending only on ID
     */
    @Override
    public int hashCode() {
        return new FNVHashBuilder().add(ID).getHash();
    }

    @Override
    public String toString() {
        String output = ID + "\n";
        output += "has spawned = " + (state == VehicleState.SPAWNED) + "\n";
        output += "route size = " + route.size() + "\n";
        output += "has front vehicle = " + (vehicleInFront != null) + "\n";
        output += "velocity = " + velocity + "\n";
        output += "cell position = " + cellPosition + "\n";
        output += "prio = " + priorityCounter + "\n";
        output += "route.getStart().permission = " + route.getStart().permissionToCross(this) + "\n";
        output += "route.getStart().hashCode() = " + route.getStart().hashCode() + "\n";
        if (state == VehicleState.SPAWNED) {
            output += "lane length = " + lane.getAssociatedEdge().getLength() + "\n";
            output += "permission to cross = " + lane.getAssociatedEdge().getDestination().permissionToCross(this)
                      + "\n";
            //			output += "lane index at node = " +
            //lane.getAssociatedEdge().getDestination().incomingEdges.get(lane.getAssociatedEdge()) + "\n";
            //			output += "next node maxLaneIndex = " + lane.getAssociatedEdge().getDestination().maxLaneIndex +
            //"\n";
            //			output += "GEILER SHIT incoming size = " +
            //lane.getAssociatedEdge().getDestination().incomingEdges.size() + "\n";
        }
        if (!route.isEmpty()) {
            //			output += "route.peek().lane index at node = " +
            //route.peek().getOrigin().leavingEdges.get(route.peek()) + "\n";
            output += "route-peek max insertion index = " + ((DirectedEdge)route.peek()).getLane(0).getMaxInsertionIndex() + "\n";
            output += "route.peek().hashCode() = " + route.peek().hashCode() + "\n";
        }
        return output;
    }

    public int getAge() {
        return age - spawnDelay;
    }

    public Route<Node> getRoute() {
        return route;
    }

    /**
     * The internal collection used to store listeners is a {@link LinkedList} for easy iterating. Due to its
     * runtime in O(n) for checking whether an Object is contained or not, this method {@code addStateListener} DOES
     * NOT check for duplicates. Thus if you add a listener twice, it is called twice.
     *
     * @param listener This listener gets informed about state changes of this vehicle. If it's null, it won't be added.
     */
    public void addStateListener(VehicleStateListener listener) {
        if (listener != null)
            stateListeners.add(listener);
    }

    public VehicleState getState() {
        return state;
    }

    private void setState(VehicleState state) {
        this.state = state;
        for (VehicleStateListener listener : stateListeners)
            listener.stateChanged(this);
    }

    public DirectedEdge peekNextRouteSection() {
        return (DirectedEdge)route.peek();
    }

    private synchronized void addVehicleInFront(AbstractVehicle vehicle) {
        this.vehicleInFront   = vehicle;
        vehicle.vehicleInBack = this;
    }

    private synchronized void removeVehicleInBack() {
        if (vehicleInBack != null) {
            vehicleInBack.vehicleInFront = vehicleInFront;
            vehicleInBack                = null;
        }
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
    public int getCellPosition() {
        return cellPosition;
    }

    @Override
    public DirectedEdge getDirectedEdge() {
        if (lane == null) return null;
        return lane.getAssociatedEdge();
    }

    /*
    |================|
    | crossing logic |
    |================|
    */
    public void resetPriorityCounter() {
        synchronized (lock_priorityCounter) {
            priorityCounter = 0;
        }
    }

    public void incPriorityCounter() {
        synchronized (lock_priorityCounter) {
            int old = priorityCounter;
            priorityCounter++;
            if (old > priorityCounter) {
                try {
                    throw new Exception("Vehicle.incPriorityCounter() - int overflow");
                } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }

    public void decPriorityCounter() {
        synchronized (lock_priorityCounter) {
            int old = priorityCounter;
            priorityCounter--;
            if (old < priorityCounter) {
                try {
                    throw new Exception("Vehicle.incPriorityCounter() - int underflow");
                } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }

    /*
    |========================|
    | world/node interaction |
    |========================|
    */
    public int getPriorityCounter() {
        return priorityCounter;
    }

    /**
     * This method registers the vehicle in the start node of the vehicle's route independant of whether the route is
     * empty or not. The route must have a start being not null.
     */
    public void registerInGraph() {
        if (!route.isEmpty())
            route.getStart().registerVehicle(this);
    }

    /**
     * If this vehicle is not spawned yet, this method checks if the route is
     * empty. If yes, the vehicle will despawn instantly. If no, it has to
     * check, if it can cross the node.
     */
    public void spawn() {
        if (state == VehicleState.NOT_SPAWNED && age >= spawnDelay) {
            if (!route.isEmpty()) {
                if (!route.getStart().permissionToCross(this)) {
                    velocity = 0;
                } else {    // allowed to spawn
                    if (((DirectedEdge)route.peek()).getLane(0).getMaxInsertionIndex() < 0) {
                        velocity = 0;
                    } else {
                        velocity = 1;
                        route.getStart().unregisterVehicle(this);
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

    private void despawn() {
        lane = null;
        setState(VehicleState.DESPAWNED);
    }

    /*
    |============|
    | simulation |
    |============|
    */
    public void accelerate() {
        if (velocity < getMaxVelocity()) velocity = Math.min(getMaxVelocity(), accelerate.apply(velocity));
    }

    public void dash() {
        if (velocity < getMaxVelocity()) {
            hasDashed               = random.nextFloat() < getDashFactor();
            if (hasDashed) velocity = accelerate.apply(velocity);
        }
    }

    public void brake() throws NagelSchreckenbergException {
        if (state == VehicleState.SPAWNED) {
            if (vehicleInFront != null) {
                // brake for front vehicle
                int distance = vehicleInFront.cellPosition - cellPosition;
                velocity     = Math.min(velocity, distance - 1);
            } else {    // this vehicle is first in lane
                DirectedEdge edge     = lane.getAssociatedEdge();
                int          distance = edge.getLength() - cellPosition;
                // Would cross node?
                if (velocity >= distance)
                    if (route.isEmpty()) {
                        // brake for end of road
                        velocity = Math.min(velocity, distance - 1);
                    } else {
                        if (edge.getDestination().permissionToCross(this)) {
                            // if next road has vehicles => brake for this
                            // else => brake for end of next road
                            int maxInsertionIndex = ((DirectedEdge)route.peek()).getLane(0).getMaxInsertionIndex();
                            velocity              = Math.min(velocity, distance + maxInsertionIndex);
                        } else {
                            // brake for end of road
                            velocity = Math.min(velocity, distance - 1);
                        }
                    }
            }

            // brake for edges max velocity
            velocity = Math.min(velocity, lane.getAssociatedEdge().getMaxVelocity());
            // better dashing
            //            if (hasDashed)
            //                velocity = Math.min(velocity, lane.getAssociatedEdge().getMaxVelocity() + 1);
            //            else
            //                velocity = Math.min(velocity, lane.getAssociatedEdge().getMaxVelocity());
        }

        if (velocity < 0)
            throw NagelSchreckenbergException.velocityLessThanZero(NagelSchreckenbergException.Step.brake, velocity);
    }

    public void dawdle() {
        if (!hasDashed)
            // if dash factor == 1, hasDashed is always true => dividing by (1 - dash factor) is okay without check
            if (velocity > 0 && state == VehicleState.SPAWNED) {
                //     P[dawdle] = P[dawdle | not dash] * P[not dash]
                // <=> P[dawdle | not dash] = P[dawdle] / (1 - P[dash])
                float p                              = getDawdleFactor() / (1 - getDashFactor());
                if (random.nextFloat() < p) velocity = dawdle.apply(velocity);
            }
    }

    /**
     * Moves the vehicle depending on its position:<br>
     * &bull If it stand's at a node, it will leave the current road and enter
     * the next road.<br>
     * &bull If it stand's in the lane, it just drives at the next position depending on its velocity.
     */
    public void move() {

        if (state == VehicleState.SPAWNED) {
            DirectedEdge edge     = lane.getAssociatedEdge();
            int          distance = edge.getLength() - getCellPosition();
            // Will cross node?
            if (velocity >= distance)
                if (!route.isEmpty()) {
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
                if (velocity == 0 && distance == 1 && route.isEmpty()) {
                    leaveCurrentRoad();
                    despawn();
                } else {
                    drive();
                }
            }
        }
    }

    public void didMove() {
        if (state == VehicleState.SPAWNED) {
            didOneSimulationStep();

            if (!route.isEmpty()) {
                int distance    = lane.getAssociatedEdge().getLength() - cellPosition;
                int maxVelocity = Math.min(getMaxVelocity(), lane.getAssociatedEdge().getMaxVelocity());
                if (maxVelocity >= distance && vehicleInFront == null)
                    lane.getAssociatedEdge().getDestination().registerVehicle(this);
            }
        }
    }

    private void didOneSimulationStep() {
        // anger
        if (velocity == 0) {
            if (lastVelocityWasZero) becomeMoreAngry();
            lastVelocityWasZero = true;
        } else {
            if (!lastVelocityWasZero) calmDown();
            lastVelocityWasZero = false;
        }
        // age
        age++;
    }

    /**
     * <p>
     * This method calculates the incrementation of the velocity in the accelaration step. It is recommended to
     * use the following formula:<br>
     * ((1 - c) * vmax + c * vin) with c = e^(-1s/TAU)<br>
     * TAU(Ferrari) is ca. 10s, TAU(Car) is ca. 15s
     * <p>
     * This formula is equal to:<br>
     * (1) calculating t0 in<br>
     * vin = (1 - e^(-t0/TAU)) * vmax<br>
     * (2) putting t0 into<br>
     * vout = (1 - e^(-t0/TAU)) * vmax<br>
     * <p>
     * After simplifying vout, you get the formula above.
     *
     * @return A function that calculates a greater velocity depending on a given velocity.<br>
     * E.g.: constant acceleration <=> this method returns something like obj1 -> obj1 + const
     */
    protected abstract Function<Integer, Integer> createAccelerationFunction();

    /**
     * This method is only called when the vehicle is dawdling, thus it should return<br>
     * e.g. obj1 -> obj1 - 5km/h
     *
     * @return A function that calculates a lower velocity depending on a given velocity.<br>
     * E.g.: constant acceleration <=> this method returns something like obj1 -> obj1 - const
     */
    protected abstract Function<Integer, Integer> createDawdleFunction();

    /**
     * @return Max velocity this vehicle is able to have.
     */
    protected abstract int getMaxVelocity();

    /**
     * @return The probability to dash in one simulation step.
     */
    protected abstract float getDashFactor();

    /**
     * @return The probability to dawdle in one simulation step.
     */
    protected abstract float getDawdleFactor();

    /*
    |===============|
    | driving logic |
    |===============|
    */
    private void leaveCurrentRoad() {
        lane.getAssociatedEdge().getDestination().unregisterVehicle(this);
        synchronized (lane.lock) {
            lane.removeVehicle(this);
            removeVehicleInBack();
        }

        // -1 * distance to end of road
        cellPosition = cellPosition - lane.getAssociatedEdge().getLength();
    }

    private void enterNextRoad() {
        lane = ((DirectedEdge)route.pop()).getLane(0);
        synchronized (lane.lock) {
            AbstractVehicle lastVehicle = lane.getLastVehicle();
            if (lastVehicle != null) { addVehicleInFront(lastVehicle); }
        }
        cellPosition = cellPosition + velocity;
        lane.insertVehicle(this, cellPosition);
        entity.getVisualization().updatePosition();
    }

    private void drive() {
        lane.moveVehicle(this, velocity);
        cellPosition = cellPosition + velocity;
        entity.getVisualization().updatePosition();
    }
}