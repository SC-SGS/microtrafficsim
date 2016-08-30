package microtrafficsim.core.logic;

import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.ShortestPathNode;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.math.Geometry;
import microtrafficsim.math.Vec2f;
import microtrafficsim.utils.hashing.FNVHashBuilder;

import java.util.*;
import java.util.stream.Collectors;


/**
 * This class represents one crossing point of two or more @DirectedEdge#s.
 * <p>
 * IDijkstrableNode serves functionality for shortest path calculations.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class Node implements ShortestPathNode {

    public final Long        ID;
    private SimulationConfig config;
    private Random           random;
    private Coordinate       coordinate;
    private HashMap<Lane, ArrayList<Lane>> restrictions;

    // crossing logic
    private HashSet<AbstractVehicle>    assessedVehicles;
    private HashSet<AbstractVehicle>    maxPrioVehicles;
    private Comparator<AbstractVehicle> crossingLogic;
    private boolean                     anyChangeSinceUpdate;

    // edges
    private HashMap<DirectedEdge, Byte> leavingEdges;    // edge, index(for

    // crossing logic)
    private HashMap<DirectedEdge, Byte> incomingEdges;    // edge, index(for
                                                          // crossing logic)

    /**
     * Standard constructor. The name is just for printing use and has no
     * meaning for the simulation.
     */
    public Node(SimulationConfig config, Coordinate coordinate) {
        this.config     = config;
        ID              = config.longIDGenerator.next();
        random          = config.rndGenGenerator.next();
        this.coordinate = coordinate;

        // crossing logic
        assessedVehicles     = new HashSet<>();
        maxPrioVehicles      = new HashSet<>();
        crossingLogic        = generateCrossingLogic();
        anyChangeSinceUpdate = false;

        // edges
        restrictions  = new HashMap<>();
        leavingEdges  = new HashMap<>();
        incomingEdges = new HashMap<>();
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder().add(ID).add(coordinate).getHash();
    }

    @Override
    public String toString() {
        String output = "Node name = " + ID + " at " + coordinate.toString();

        //		for (Lane start : restrictions.keySet())
        //			for (Lane end : restrictions.get(start))
        //				output += start + " to " + end + "\n";

        return output;
    }

    /*
    |================|
    | crossing logic |
    |================|
    */
    private Comparator<AbstractVehicle> generateCrossingLogic() {
        return (v1, v2) -> {
            // there is always a current edge and a next edge per vehicle
            byte origin1        = incomingEdges.get(v1.getDirectedEdge());
            byte destination1   = leavingEdges.get(v1.peekNextRouteSection());
            byte origin2        = incomingEdges.get(v2.getDirectedEdge());
            byte destination2   = leavingEdges.get(v2.peekNextRouteSection());
            byte indicesPerNode = (byte) (incomingEdges.size() + leavingEdges.size());

            // if vehicles are crossing each other's way
            if (IndicesCalculator.areIndicesCrossing(origin1, destination1, origin2, destination2, indicesPerNode)) {
                // compare priorities of origins
                byte cmp = (byte) (v1.getDirectedEdge().getPriorityLevel() - v2.getDirectedEdge().getPriorityLevel());
                boolean edgePriorityEnabled = config.crossingLogic.edgePriorityEnabled;
                if (cmp == 0 || !edgePriorityEnabled) {
                    // compare priorities of destinations
                    cmp = (byte) (v1.peekNextRouteSection().getPriorityLevel()
                                  - v2.peekNextRouteSection().getPriorityLevel());
                    if (cmp == 0 || !edgePriorityEnabled) {
                        // compare right before left (or left before right)
                        if (config.crossingLogic.priorityToTheRightEnabled) {
                            byte leftmostMatchingIdx = IndicesCalculator.leftmostIndexInMatching(
                                    origin1, destination1, origin2, destination2, indicesPerNode);
                            if (leftmostMatchingIdx == origin1) return 1;
                            if (leftmostMatchingIdx == origin2) return -1;
                            return 0;
                        } else {
                            // random
                            return random.nextInt(3) - 1;
                        }
                    } else
                        return cmp;
                } else
                    return cmp;
            }
            return 0;
        };
    }

    /**
     * The node empties its crossing sets etc., but also reset its instance of {@link Random}, whereas it is not
     * guaranteed, that it will be identical.
     */
    void reset() {
        random = config.rndGenGenerator.next();

        // crossing logic
        assessedVehicles     = new HashSet<>();
        maxPrioVehicles      = new HashSet<>();
        anyChangeSinceUpdate = false;
    }

    public void update() {

        if (!assessedVehicles.isEmpty()) {
            maxPrioVehicles.clear();
            Iterator<AbstractVehicle> iter = assessedVehicles.iterator();

            // get vehicles with max prio
            int     maxPrio                  = Integer.MIN_VALUE;
            boolean goWithoutPriorityEnabled = config.crossingLogic.friendlyStandingInJamEnabled;
            while (iter.hasNext()) {
                AbstractVehicle v = iter.next();
                // if
                // priority is higher than current highest priority
                if (maxPrio <= v.getPriorityCounter()) {
                    // if
                    // next route has space for current vehicle
                    // BUT
                    // configs has to be false to enable going without priority:
                    // false <=>  config.friendlyStandingInJamEnabled && !anyChangeSinceUpdate
                    // true  <=> !config.friendlyStandingInJamEnabled || anyChangeSinceUpdate
                    if (!goWithoutPriorityEnabled || anyChangeSinceUpdate
                        || v.peekNextRouteSection().getLane(0).getMaxInsertionIndex() >= 0) {
                        if (maxPrio < v.getPriorityCounter()) {
                            maxPrioVehicles.clear();
                            maxPrio = v.getPriorityCounter();
                        }
                        maxPrioVehicles.add(v);
                    }
                }
            }

            if (!maxPrioVehicles.isEmpty()) {
                // case #1: maxPrio == assessedVehicles.size() - 1
                // => all vehicles are beaten
                // XOR
                // boolean deadlock = maxPrio < assessedVehicles.size() - 1;
                // boolean tooManyVehicles = config.onlyOneVehicleDrivesAtNode && maxPrioVehicles.size() > 1;
                // case #2: deadlock OR tooManyVehicles
                // => choose random vehicle
                if (maxPrio < assessedVehicles.size() - 1
                    || (config.crossingLogic.isOnlyOneVehicleEnabled() && maxPrioVehicles.size() > 1)) {
                    Iterator<AbstractVehicle> bla = maxPrioVehicles.iterator();
                    for (int i = 0; i < random.nextInt(maxPrioVehicles.size()); i++) {
                        bla.next();
                    }
                    AbstractVehicle v = bla.next();
                    maxPrioVehicles.clear();
                    maxPrioVehicles.add(v);
                }
            }
        }
        anyChangeSinceUpdate = false;
    }

    public synchronized void registerVehicle(AbstractVehicle vehicle) {

        if (!assessedVehicles.contains(vehicle)) {
            // calculate priority counter
            vehicle.resetPriorityCounter();
            for (AbstractVehicle v : assessedVehicles) {
                // main rules:
                // 1) spawned vehicles before not spawned vehicles
                // 2) registered, not spawned vehicles before new not-spawned
                // ones
                // => only the first vehicle, that wants to spawn, can do this
                // 3) two spawned vehicles => comparator

                // 1) + 2)
                if (vehicle.getState() != VehicleState.SPAWNED) {
                    vehicle.decPriorityCounter();
                    v.incPriorityCounter();
                }    // 1) if new vehicle has spawned and registered one wants to
                else if (v.getState() != VehicleState.SPAWNED) {
                    vehicle.incPriorityCounter();
                    v.decPriorityCounter();
                }    // 3)
                else {
                    int cmp = crossingLogic.compare(vehicle, v);
                    if (cmp > 0) {
                        vehicle.incPriorityCounter();
                        v.decPriorityCounter();
                    } else if (cmp < 0) {
                        vehicle.decPriorityCounter();
                        v.incPriorityCounter();
                    } else {
                        vehicle.incPriorityCounter();
                        v.incPriorityCounter();
                    }
                }
            }
            assessedVehicles.add(vehicle);
            anyChangeSinceUpdate = true;
        }
    }

    public synchronized void deregisterVehicle(AbstractVehicle vehicle) {

        if (assessedVehicles.remove(vehicle)) {
            for (AbstractVehicle v : assessedVehicles) {
                if (vehicle.getState() != VehicleState.SPAWNED) {
                    if (v.getState() != VehicleState.SPAWNED) {
                        // if vehicle and v are not spawned, the older one was prefered
                        // => the older one has a higher priority counter
                        if (vehicle.getPriorityCounter() > v.getPriorityCounter())
                            v.incPriorityCounter();
                        else
                            v.decPriorityCounter();
                    } else {
                        // v has been the winner, but because vehicle is not
                        // spawned yet, there can not exist a v
                        // => else should not be possible
                        v.decPriorityCounter();
                    }
                } else {
                    if (v.getState() != VehicleState.SPAWNED) {
                        v.incPriorityCounter();
                    } else {
                        int cmp = crossingLogic.compare(vehicle, v);
                        if (cmp > 0) {
                            v.incPriorityCounter();
                        } else if (cmp < 0) {
                            v.decPriorityCounter();
                        } else {
                            v.decPriorityCounter();
                        }
                    }
                }
            }
            vehicle.resetPriorityCounter();
            anyChangeSinceUpdate = true;
        }
    }

    public synchronized boolean permissionToCross(AbstractVehicle vehicle) {
        return maxPrioVehicles.contains(vehicle);
    }

    // |===========================|
    // | add edges (preprocessing) |
    // |===========================|

    /**
     * This method adds one turning lane. Not every lane is connected to any
     * other lane.
     *
     * @param incoming
     * @param leaving
     * @param direction UNUSED
     */
    public void addConnector(Lane incoming, Lane leaving, Direction direction) {
        if (!restrictions.containsKey(incoming)) { restrictions.put(incoming, new ArrayList<>()); }
        restrictions.get(incoming).add(leaving);
    }

    /**
     * Adds a {@link DirectedEdge} to this node. It's traffic index for
     * calculating crossing order is set to -1.
     */
    public void addEdge(DirectedEdge edge) {
        if (edge.getOrigin() == this) {
            leavingEdges.put(edge, (byte) -1);
        } else if (edge.getDestination() == this) {
            incomingEdges.put(edge, (byte) -1);
        }
    }

    /**
     * This method should be called after all edges are added to this node. It
     * calculates the order of the edges that is needed for crossing logic
     * calculation.
     */
    public void calculateEdgeIndices() {

        // set zero vector
        Vec2f zero = null;
        for (DirectedEdge edge : leavingEdges.keySet()) {
            zero = new Vec2f(edge.getOriginDirection());
            break;
        }
        if (zero == null)
            for (DirectedEdge edge : incomingEdges.keySet()) {
                zero = Vec2f.mul(edge.getDestinationDirection(), -1);
                break;
            }

        // get all vectors for sorting
        HashMap<Vec2f, ArrayList<DirectedEdge>> edges = new HashMap<>();
        for (DirectedEdge edge : leavingEdges.keySet()) {
            Vec2f v = new Vec2f(edge.getOriginDirection());
            if (!edges.containsKey(v)) edges.put(v, new ArrayList<>(2));
            edges.get(v).add(edge);
        }
        for (DirectedEdge edge : incomingEdges.keySet()) {
            Vec2f v = Vec2f.mul(edge.getDestinationDirection(), -1);
            if (!edges.containsKey(v)) edges.put(v, new ArrayList<>(2));
            edges.get(v).add(edge);
        }

        // now: all vectors are keys
        Queue<Vec2f> sortedVectors
                = Geometry.sortClockwiseAsc(zero, edges.keySet(), !config.crossingLogic.drivingOnTheRight);
        byte nextCrossingIndex = 0;
        while (!sortedVectors.isEmpty()) {
            ArrayList<DirectedEdge> nextEdges = edges.remove(sortedVectors.poll());
            for (DirectedEdge nextEdge : nextEdges)
                if (leavingEdges.containsKey(nextEdge)) leavingEdges.put(nextEdge, nextCrossingIndex++);
            for (DirectedEdge nextEdge : nextEdges)
                if (incomingEdges.containsKey(nextEdge)) incomingEdges.put(nextEdge, nextCrossingIndex++);
        }
    }

    /*
    |======================|
    | (i) IDijkstrableNode |
    |======================|
    */
    @Override
    public Iterator<ShortestPathEdge> getLeavingEdges(ShortestPathEdge incoming) {
        HashSet<ShortestPathEdge> returnEdges = new HashSet<>();

        if (incoming != null) {
            for (Lane incomingLane : ((DirectedEdge) incoming).getLanes()) {
                ArrayList<Lane> restrictedLeavingLanes = restrictions.get(incomingLane);
                // if there exist restrictions
                if (restrictedLeavingLanes != null)
                    returnEdges.addAll(
                            restrictedLeavingLanes.stream().map(Lane::getAssociatedEdge).collect(Collectors.toList()));
                // before: (wtf Intellij is so awesome)
                // for (Lane leavingLane : restrictedLeavingLanes)
                // returnEdges.add(leavingLane.getAssociatedEdge());
                else
                    returnEdges.addAll(leavingEdges.keySet());
            }
        } else {
            returnEdges.addAll(leavingEdges.keySet());
        }

        return returnEdges.iterator();
    }

    @Override
    public Coordinate getCoordinate() {
        return coordinate;
    }
}