package microtrafficsim.core.logic.nodes;

import microtrafficsim.core.logic.Direction;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.logic.streets.Lane;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.ShortestPathNode;
import microtrafficsim.core.simulation.configs.ConfigUpdateListener;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.exceptions.core.logic.CrossingLogicException;
import microtrafficsim.math.Geometry;
import microtrafficsim.math.Vec2d;
import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.Resettable;
import microtrafficsim.utils.hashing.FNVHashBuilder;

import java.util.*;


/**
 * This class represents one crossing point of two or more @DirectedEdge#s.
 * <p>
 * ShortestPathNode serves functionality for shortest path calculations.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class Node implements ConfigUpdateListener, ShortestPathNode, Resettable {

    public final Long        ID;
    private ScenarioConfig   config;
    private Random random;
    private Coordinate       coordinate;

    // crossing logic
    private PriorityQueue<AbstractVehicle>                 newRegisteredVehicles;
    private HashMap<AbstractVehicle, Set<AbstractVehicle>> assessedVehicles;
    private HashSet<AbstractVehicle>                       maxPrioVehicles;
    private boolean                                        anyChangeSinceUpdate;
    private HashMap<Lane, ArrayList<Lane>>                 restrictions;

    // edges
    private HashMap<DirectedEdge, Byte> leavingEdges;    // edge, index(for crossing logic)
    private HashMap<DirectedEdge, Byte> incomingEdges;    // edge, index(for crossing logic)


    /**
     * Default constructor
     */
    public Node(ScenarioConfig config, Coordinate coordinate) {
        this.config     = config;
        ID              = config.longIDGenerator.next();
        this.coordinate = coordinate;

        // crossing logic
        random                = new Random(config.seedGenerator.next());
        assessedVehicles      = new HashMap<>();
        maxPrioVehicles       = new HashSet<>();
        newRegisteredVehicles = new PriorityQueue<>((v1, v2) -> Long.compare(v1.ID, v2.ID));
        anyChangeSinceUpdate  = false;

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
        String output = "Node ID = " + ID + " at " + coordinate.toString();

        //		for (Lane start : restrictions.keySet())
        //			for (Lane end : restrictions.get(start))
        //				output += start + " to " + end + "\n";

        return output;
    }

    /*
    |==========================|
    | (i) ConfigUpdateListener |
    |==========================|
    */
    @Override
    public void updateConfig(ScenarioConfig updatedConfig) {
//        System.err.println("Yay ConfigUpdateListener");
    }

    /*
    |================|
    | crossing logic |
    |================|
    */
    /**
     * <p>
     * Rules:<br>
     * &bull two not-spawned vehicles are compared by their IDs. The greater ID wins.<br>
     * &bull spawned vehicles gets priority over not spawned vehicles. This makes sense when thinking about the
     * situation, when you want to enter the street from your private parking place.<br>
     * &bull two spawned vehicles means they are coming from a street and want to make a turn. Thus they have to be
     * compared by the crossing logic (below).<br>
     * IMPORTANT: The registration does NOT check the positions relative to each other vehicle ON THE STREET, but it
     * checks/compares all information relevant for the crossing itself.
     *
     * <p>
     * At first, the crossing logic checks whether the two vehicles' turning ways are crossing each other (otherwise
     * return 0). If they are crossing, the origin-priorities are compared. If equal, the destination-priorities are
     * compared. If equal, they have to be compared by right-before-left or randomly. All sub-comparisons can be
     * enabled/disabled with the {@link ScenarioConfig}.
     *
     * @return an int > 0 if v1 has priority over v2; an int < 0 if v2 has priority over v1; an int = 0 if v1 and v2
     * have equal priorities
     */
    private int compare(AbstractVehicle v1, AbstractVehicle v2) throws CrossingLogicException {
        // main rules:
        // (1) two not-spawned vehicles are compared by their IDs. The greater ID wins.
        // (2) spawned vehicles before not spawned vehicles
        // (3) two spawned vehicles => comparator

        if (v1.getState() != VehicleState.SPAWNED) {
            if (v2.getState() != VehicleState.SPAWNED) {
                // (1) v1 is NOT SPAWNED, v2 is NOT SPAWNED
                return Long.compare(v1.ID, v2.ID);
            } else {
                // (2) v1 is NOT SPAWNED, v2 is SPAWNED
                return -1;
            }
        } else if (v2.getState() != VehicleState.SPAWNED) {
            // (2) v1 is SPAWNED, v2 is NOT SPAWNED
            return 1;
        }

        // (3) both SPAWNED => there is always a current edge and a next edge per vehicle
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
                        if (leftmostMatchingIdx == origin1) {
                            return 1;
                        }
                        if (leftmostMatchingIdx == origin2) {
                            return -1;
                        }
                        throw new CrossingLogicException();
                    } else {
                        // random out of {-1, 1}
                        return random.nextInt(2) * 2 - 1;
                    }
                }
            }
            return cmp;
        }
        return 0;
    }

    /**
     * The node empties its crossing sets etc. and resets its {@link Random}
     */
    @Override
    public synchronized void reset() {
        random.reset();
        assessedVehicles.clear();
        maxPrioVehicles.clear();
        newRegisteredVehicles.clear();
        anyChangeSinceUpdate  = false;
    }

    /**
     * <p>
     * If any vehicle has unregistered since the last call of {@code update}, all vehicles are compared to each other
     * for getting the highest priority. This needs O(n^2) comparisons due to the Gauss sum.
     */
    public synchronized void update() {

        /* add new registered vehicles */
        while (!newRegisteredVehicles.isEmpty()) { // invariant: all vehicles in this set are new at this point
            AbstractVehicle newVehicle = newRegisteredVehicles.poll();
            Set<AbstractVehicle> defeatedVehicles = new HashSet<>();

            // calculate priority counter
            newVehicle.resetPriorityCounter();
            for (AbstractVehicle assessedVehicle : assessedVehicles.keySet()) {
                int cmp;

                try {
                    cmp = compare(newVehicle, assessedVehicle);
                } catch (CrossingLogicException e) {
                    e.printStackTrace();
                    continue;
                }

                if (cmp > 0) {
                    newVehicle.incPriorityCounter();
                    defeatedVehicles.add(assessedVehicle);

                    assessedVehicle.decPriorityCounter();
                } else if (cmp < 0) {
                    newVehicle.decPriorityCounter();

                    assessedVehicle.incPriorityCounter();
                    assessedVehicles.get(assessedVehicle).add(newVehicle);
                } else {
                    newVehicle.incPriorityCounter();
                    defeatedVehicles.add(assessedVehicle);

                    assessedVehicle.incPriorityCounter();
                    assessedVehicles.get(assessedVehicle).add(newVehicle);
                }
            }

            assessedVehicles.put(newVehicle, defeatedVehicles);
        }

        /* find max prioritized vehicles */
        if (!assessedVehicles.isEmpty()) {
            maxPrioVehicles.clear();

            // get vehicles with max prio
            int maxPrio = Integer.MIN_VALUE;
            for (AbstractVehicle vehicle : assessedVehicles.keySet()) {
                if (maxPrio <= vehicle.getPriorityCounter()) {
                    // For all vehicles until now: the current vehicle is allowed to drive regarding priority.
                    // BUT: it is still NOT allowed if all of the following conditions are true

                    // PRO-TIP: Read the following statements as "if condition (1) is false, the case is clear and
                    // vehicle gets permission. If it is true but condition (2) is false, the case is clear and vehicle
                    // gets permission. If it is true as well but condition (3) is false, the case is clear and vehicle
                    // gets permission."

                    // (1) no change since last update
                    // (2) no space for the vehicle at the next road
                    // (3) friendly standing in jam is enabled; the effect of this boolean points out if it is false,
                    // because then, a vehicle is taken into account although it has no space at the next road

                    // (1)
                    if (!anyChangeSinceUpdate) {
                        // (3), different order than above for better performance
                        if (config.crossingLogic.friendlyStandingInJamEnabled)
                            // (2), different order than above for better performance
                            if (!(vehicle.peekNextRouteSection().getLane(0).getMaxInsertionIndex() >= 0))
                                continue;
                    }

                    // if priority is truly greater than current max => remove all current vehicles of max priority
                    if (maxPrio < vehicle.getPriorityCounter()) {
                        maxPrioVehicles.clear();
                        maxPrio = vehicle.getPriorityCounter();
                    }
                    maxPrioVehicles.add(vehicle);
                }
            }


            if (!maxPrioVehicles.isEmpty()) {
                // case #1: maxPrio == assessedVehicles.size() - 1
                // => all vehicles are beaten (otherwise: deadlock between vehicles if more than one has priority)
                boolean allOthersBeaten = maxPrio == assessedVehicles.size() - 1;
                // XOR
                // case #2: deadlock OR tooManyVehicles
                // => choose random vehicle
                boolean tooManyVehicles = config.crossingLogic.isOnlyOneVehicleEnabled() && maxPrioVehicles.size() > 1;
                if (!allOthersBeaten || tooManyVehicles) {
                    Iterator<AbstractVehicle> bla = maxPrioVehicles.iterator();
                    for (int i = 0; i < random.nextInt(maxPrioVehicles.size()); i++)
                        bla.next();
                    AbstractVehicle prioritizedVehicle = bla.next();
                    maxPrioVehicles.clear();
                    maxPrioVehicles.add(prioritizedVehicle);
                }
            }
        }

        anyChangeSinceUpdate = false;
    }

    /**
     * <p>
     * Registers the given vehicle at this node.
     *
     * <p>
     * This method is synchronized because the assertion works with the information whether a vehicle is registered or
     * not => access should be after registration has finished.
     *
     * @param newVehicle This vehicle gets registered in this node.
     * @return true, if the given vehicle is getting registered; false otherwise (e.g. if it is already registered)
     */
    public synchronized boolean registerVehicle(AbstractVehicle newVehicle) {

        if (isRegistered(newVehicle))
            return false;

        newRegisteredVehicles.add(newVehicle);
        anyChangeSinceUpdate = true;
        return true;
    }

    /**
     * <p>
     * Remove occurrence of the given vehicle in this node. Due to the complexity of the used hash maps, this method
     * has a runtime complexity in O(n logn), where n is the number of vehicles registered in this node. <br>
     * For each vehicle, the priority counter is updated and the other data structures containing the vehicle
     * getting unregistered are updated in O(log n) due to sets.
     *
     * @param vehicle This vehicle should being unregistered after this method
     * @return true, if the given vehicle has been registered and is unregistered now; false, if it hasn't been
     * registered at this node
     */
    public synchronized boolean unregisterVehicle(AbstractVehicle vehicle) {

        Set<AbstractVehicle> defeatedVehicles = assessedVehicles.remove(vehicle);
        if (defeatedVehicles == null) {
            newRegisteredVehicles.remove(vehicle);
            return false;
        }

        for (AbstractVehicle otherVehicle : assessedVehicles.keySet()) {
            boolean otherWon = assessedVehicles.get(otherVehicle).remove(vehicle);

            if (otherWon)
                otherVehicle.decPriorityCounter();
            else
                otherVehicle.incPriorityCounter();
        }

        anyChangeSinceUpdate = true;
        return true;
    }

    /**
     * <p>
     * This method is synchronized because its return value depends on {@link #registerVehicle(AbstractVehicle)},
     * {@link #unregisterVehicle(AbstractVehicle)} and {@link #update()}, which can be called concurrently.
     *
     * @param vehicle This vehicle asks whether it has permission to cross or not
     * @return true if the vehicle has permission to cross, false otherwise
     */
    public synchronized boolean permissionToCross(AbstractVehicle vehicle) {
        return maxPrioVehicles.contains(vehicle);
    }

    public synchronized boolean isRegistered(AbstractVehicle vehicle) {
        return assessedVehicles.containsKey(vehicle) || newRegisteredVehicles.contains(vehicle);
    }

//    /**
//     * @return Iterator returning tuples of edges with their crossing index
//     */
//    public Iterator<Tuple<DirectedEdge, Byte>> iterCrossingIndices() {
//
//        Iterator<DirectedEdge> iterLeavingEdges = this.leavingEdges.keySet().iterator();
//        Iterator<DirectedEdge> iterIncomingEdges = this.incomingEdges.keySet().iterator();
//        HashMap<DirectedEdge, Byte> leavingEdges = new HashMap<>(this.leavingEdges);
//        HashMap<DirectedEdge, Byte> incomingEdges = new HashMap<>(this.incomingEdges);
//
//        return new Iterator<Tuple<DirectedEdge, Byte>>() {
//
//            Iterator<DirectedEdge> bla = leavingEdges.keySet().iterator();
//
//            @Override
//            public boolean hasNext() {
//                return iterLeavingEdges.hasNext() || iterIncomingEdges.hasNext();
//            }
//
//            @Override
//            public Tuple<DirectedEdge, Byte> next() {
//
//                if (iterLeavingEdges.hasNext()) {
//                    DirectedEdge edge = iterLeavingEdges.next();
//                    Byte crossingIndex = leavingEdges.get(edge);
//                    return new Tuple<>(edge, crossingIndex);
//                } else if (iterIncomingEdges.hasNext()) {
//                    DirectedEdge edge = iterIncomingEdges.next();
//                    Byte crossingIndex = incomingEdges.get(edge);
//                    return new Tuple<>(edge, crossingIndex);
//                }
//
//                return null;
//            }
//        };
//    }

    /*
    |===========================|
    | add edges (preprocessing) |
    |===========================|
    */
    /**
     * This method adds one turning lane. Not every lane is connected to any
     * other lane.
     *
     * @param incoming
     * @param leaving
     * @param direction UNUSED
     */
    public void addConnector(Lane incoming, Lane leaving, Direction direction) {
        ArrayList<Lane> connectedLanes = restrictions.get(incoming);
        if (connectedLanes == null) {
            connectedLanes = new ArrayList<>();
            restrictions.put(incoming, connectedLanes);
        }
        connectedLanes.add(leaving);
    }

    /**
     * Adds a {@link DirectedEdge} to this node. It's traffic index for
     * calculating crossing order is set to -1.
     */
    public void addEdge(DirectedEdge edge) {
        if (edge.getOrigin() == this)
            leavingEdges.put(edge, (byte) -1);
        else if (edge.getDestination() == this)
            incomingEdges.put(edge, (byte) -1);
    }

    /**
     * This method should be called after all edges are added to this node. It
     * calculates the order of the edges that is needed for crossing logic
     * calculation.
     */
    public void calculateEdgeIndices() {

        /* init */
        HashMap<Vec2d, ArrayList<DirectedEdge>> edges = new HashMap<>();
        Vec2d zero = null;

        /* get all vectors for sorting */
        for (DirectedEdge edge : leavingEdges.keySet()) {
            // invert leaving XOR incoming edges
            Vec2d v = Vec2d.mul(edge.getOriginDirection(), -1);

            // set zero reference if not done yet
            if (zero == null)
                zero = v;

            // add edge to its vector
            ArrayList<DirectedEdge> vectorsEdges = edges.get(v);
            if (vectorsEdges == null) {
                vectorsEdges = new ArrayList<>(2);
                edges.put(v, vectorsEdges);
            }
            vectorsEdges.add(edge);
        }

        for (DirectedEdge edge : incomingEdges.keySet()) {
            Vec2d v = new Vec2d(edge.getDestinationDirection());

            // set zero reference if not done yet
            if (zero == null)
                zero = v;

            // add edge to its vector
            ArrayList<DirectedEdge> vectorsEdges = edges.get(v);
            if (vectorsEdges == null) {
                vectorsEdges = new ArrayList<>(2);
                edges.put(v, vectorsEdges);
            }
            vectorsEdges.add(edge);
        }

        /* now: all vectors are keys */
        Queue<Vec2d> sortedVectors
                = Geometry.sortClockwiseAsc(zero, edges.keySet(), !config.crossingLogic.drivingOnTheRight);
        byte nextCrossingIndex = 0;
        // iterate over the sorted vectors
        while (!sortedVectors.isEmpty()) {
            Vec2d v = sortedVectors.poll();
            // take the current vector's edges
            ArrayList<DirectedEdge> nextEdges = edges.remove(v);

            // add its leaving edges before its incoming edges
            for (DirectedEdge nextEdge : nextEdges)
                if (leavingEdges.containsKey(nextEdge))
                    leavingEdges.put(nextEdge, nextCrossingIndex++);

            for (DirectedEdge nextEdge : nextEdges)
                if (incomingEdges.containsKey(nextEdge))
                    incomingEdges.put(nextEdge, nextCrossingIndex++);
        }
    }

    /*
    |======================|
    | (i) ShortestPathNode |
    |======================|
    */
    @Override
    public Set<ShortestPathEdge> getLeavingEdges(ShortestPathEdge incoming) {

        if (incoming == null)
            return Collections.unmodifiableSet(leavingEdges.keySet());

        HashSet<ShortestPathEdge> returnEdges = new HashSet<>();

        for (Lane incomingLane : ((DirectedEdge) incoming).getLanes()) {
            ArrayList<Lane> restrictedLeavingLanes = restrictions.get(incomingLane);
            // if there exist restrictions
            if (restrictedLeavingLanes != null)
                for (Lane leavingLane : restrictedLeavingLanes)
                    returnEdges.add(leavingLane.getAssociatedEdge());
            else
                return Collections.unmodifiableSet(leavingEdges.keySet());
        }
        return returnEdges;
    }

    @Override
    public Set<ShortestPathEdge> getIncomingEdges() {
        return new HashSet<>(incomingEdges.keySet());
    }

    @Override
    public Coordinate getCoordinate() {
        return coordinate;
    }
}