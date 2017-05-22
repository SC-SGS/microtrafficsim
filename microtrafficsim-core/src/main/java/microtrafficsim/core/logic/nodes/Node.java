package microtrafficsim.core.logic.nodes;

import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.logic.streets.Lane;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.shortestpath.ShortestPathNode;
import microtrafficsim.core.simulation.configs.CrossingLogicConfig;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.math.Geometry;
import microtrafficsim.math.MathUtils;
import microtrafficsim.math.Vec2d;
import microtrafficsim.math.random.Seeded;
import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.Resettable;
import microtrafficsim.utils.collections.FastSortedArrayList;
import microtrafficsim.utils.collections.Triple;
import microtrafficsim.utils.collections.Tuple;
import microtrafficsim.utils.functional.Procedure2;
import microtrafficsim.utils.hashing.FNVHashBuilder;
import microtrafficsim.utils.strings.builder.LevelStringBuilder;

import java.util.*;


/**
 * This class represents one crossing point of two or more {@link DirectedEdge}s.
 * <p>
 * {@code ShortestPathNode} serves functionality for shortest path calculations.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class Node implements ShortestPathNode<DirectedEdge>, Resettable, Seeded {
    private final long          id;
    private Coordinate          coordinate;
    private CrossingLogicConfig config;
    private final Random        random;

    // crossing logic
    private PriorityQueue<Vehicle>         newRegisteredVehicles;
    private HashMap<Vehicle, Set<Vehicle>> assessedVehicles;
    private HashSet<Vehicle>               maxPrioVehicles;
    private boolean                        anyChangeSinceUpdate;
    private HashMap<Lane, ArrayList<Lane>> connectors;

    // edges
    private HashMap<DirectedEdge, Byte> leaving;     // edge, index(for crossing logic)
    private HashMap<DirectedEdge, Byte> incoming;    // edge, index(for crossing logic)


    /**
     * Default constructor
     */
    public Node(long id, Coordinate coordinate, CrossingLogicConfig config) {
        this.id         = id;
        this.coordinate = coordinate;
        this.config     = config;

        // crossing logic
        random                = new Random();  // set below for determinism
        assessedVehicles      = new HashMap<>();
        maxPrioVehicles       = new HashSet<>();
        newRegisteredVehicles = new PriorityQueue<>(Comparator.comparingLong(Vehicle::getId));
        anyChangeSinceUpdate  = false;

        // edges
        connectors = new HashMap<>();
        leaving = new HashMap<>();
        incoming = new HashMap<>();
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder()
                .add(id)
                .add(coordinate)
                .getHash();
    }

    @Override
    public String toString() {
        return "Node id = " + id + " at " + coordinate.toString();
    }

    public String toStringVerbose() {
        LevelStringBuilder builder = new LevelStringBuilder()
                .setLevelSeparator(System.lineSeparator())
                .setLevelSubString("    ");

        builder.appendln("<Node>").incLevel(); {
            /* id */
            builder.appendln("<id>").incLevel(); {
                builder.appendln(id);
            } builder.decLevel().appendln("</id>").appendln();


            /* coordinate */
            builder.appendln("<coordinate>").incLevel(); {
                builder.appendln(coordinate.toString());
            } builder.decLevel().appendln("</coordinate>").appendln();


            Procedure2<String, HashMap<DirectedEdge, Byte>> edgesToString = (type, map) -> {
                builder.appendln("<" + type + " edges>").incLevel(); {
                    Iterator<DirectedEdge> iter = map.keySet().iterator();
                    while (iter.hasNext()) {
                        DirectedEdge edge = iter.next();
                        builder.appendln(edge);
                        builder.appendln("with crossing index = " + map.get(edge));

                        if (iter.hasNext())
                            builder.appendln();
                    }
                } builder.decLevel().appendln("</" + type + " edges>").appendln();
            };


            edgesToString.invoke("incoming", incoming);
            edgesToString.invoke("leaving", leaving);

        } builder.decLevel().appendln("</Node>");

        return builder.toString();
    }


    public long getId() {
        return id;
    }

    public CrossingLogicConfig getCrossingLogicConfig() {
        return config;
    }

    public HashMap<Lane, ArrayList<Lane>> getConnectors() {
        return connectors;
    }

    /*
    |================|
    | crossing logic |
    |================|
    */
    /**
     * Rules:<br>
     * &bull two not-spawned vehicles are compared by their IDs. The greater id wins.<br>
     * &bull spawned vehicles gets priority over not spawned vehicles. This makes sense when thinking about the
     * situation, when you want to enter the street from your private parking place.<br>
     * &bull two spawned vehicles means they are coming from a street and want to make a turn. Thus they have to be
     * compared by the crossing logic (below).<br>
     * IMPORTANT: The registration does NOT check the positions relative to each other vehicle ON THE STREET, but it
     * checks/compares all information relevant for the crossing itself.
     * <p>
     * At first, the crossing logic checks whether the two vehicles' turning ways are crossing each other (otherwise
     * return 0). If they are crossing, the origin-priorities are compared. If equal, the destination-priorities are
     * compared. If equal, they have to be compared by right-before-left or randomly. All sub-comparisons can be
     * enabled/disabled with the {@link SimulationConfig}.
     *
     * @return an int > 0 if v1 has priority over v2; an int < 0 if v2 has priority over v1; an int = 0 if v1 and v2
     * have equal priorities
     */
    private int compare(Vehicle v1, Vehicle v2) {
        // main rules:
        // (1) two not-spawned vehicles are compared by their IDs. The smaller id wins.
        // (2) spawned vehicles before not spawned vehicles
        // (3) two spawned vehicles => comparator

        if (v1.getState() != VehicleState.SPAWNED) {
            if (v2.getState() != VehicleState.SPAWNED) {
                // (1) v1 is NOT SPAWNED, v2 is NOT SPAWNED
                return -1 * Long.compare(v1.getId(), v2.getId());
            } else {
                // (2) v1 is NOT SPAWNED, v2 is SPAWNED
                return -1;
            }
        } else if (v2.getState() != VehicleState.SPAWNED) {
            // (2) v1 is SPAWNED, v2 is NOT SPAWNED
            return 1;
        }

        // (3) both SPAWNED => there is always a current edge and a next edge per vehicle
        assert v1.getDirectedEdge() != null : "Vehicle 1 in node-comparator has no directed edge!";
        assert v2.getDirectedEdge() != null : "Vehicle 2 in node-comparator has no directed edge!";
        byte origin1        = incoming.get(v1.getDirectedEdge());
        byte destination1   = leaving.get(v1.getDriver().peekRoute());
        byte origin2        = incoming.get(v2.getDirectedEdge());
        byte destination2   = leaving.get(v2.getDriver().peekRoute());
        byte supremum = (byte) (1 + MathUtils.max(origin1, destination1, origin2, destination2));


        // if vehicles are crossing each other's way
        if (IndicesCalculator.areIndicesCrossing(origin1, destination1, origin2, destination2, supremum)) {
            // compare priorities of origins
            byte cmp = (byte) (v1.getDirectedEdge().getPriorityLevel() - v2.getDirectedEdge().getPriorityLevel());
            boolean edgePriorityEnabled = config.edgePriorityEnabled;
            if (cmp == 0 || !edgePriorityEnabled) {
                // compare priorities of destinations
                cmp = (byte) (v1.getDriver().peekRoute().getPriorityLevel()
                        - v2.getDriver().peekRoute().getPriorityLevel());
                if (cmp == 0 || !edgePriorityEnabled) {
                    // compare right before left (or left before right)
                    if (config.priorityToTheRightEnabled) {
                        byte leftmostMatchingIdx = IndicesCalculator.leftmostIndexInMatching(
                                origin1, destination1, origin2, destination2, supremum);
                        if (leftmostMatchingIdx == origin1)
                            return 1;
                        if (leftmostMatchingIdx == origin2)
                            return -1;
                        assert false : "Crossing logic returns 0 where it should not be 0.";
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
     * If any vehicle has unregistered since the last call of {@code update}, all vehicles are compared to each other
     * for getting the highest priority. This needs O(n^2) comparisons due to the Gauss sum.
     */
    public synchronized void update() {

        /* add new registered vehicles */
        while (!newRegisteredVehicles.isEmpty()) { // invariant: all vehicles in this set are new at this point
            Vehicle newVehicle = newRegisteredVehicles.poll();
            Set<Vehicle> defeatedVehicles = new HashSet<>();

            // calculate priority counter
            newVehicle.getDriver().resetPriorityCounter();
            for (Vehicle assessedVehicle : assessedVehicles.keySet()) {
                int cmp = compare(newVehicle, assessedVehicle);

                if (cmp > 0) {
                    newVehicle.getDriver().incPriorityCounter();
                    defeatedVehicles.add(assessedVehicle);

                    assessedVehicle.getDriver().decPriorityCounter();
                } else if (cmp < 0) {
                    newVehicle.getDriver().decPriorityCounter();

                    assessedVehicle.getDriver().incPriorityCounter();
                    assessedVehicles.get(assessedVehicle).add(newVehicle);
                } else {
                    newVehicle.getDriver().incPriorityCounter();
                    defeatedVehicles.add(assessedVehicle);

                    assessedVehicle.getDriver().incPriorityCounter();
                    assessedVehicles.get(assessedVehicle).add(newVehicle);
                }
            }

            assessedVehicles.put(newVehicle, defeatedVehicles);
        }

        /* find max prioritized vehicles */
        maxPrioVehicles.clear();
        if (!assessedVehicles.isEmpty()) {

            // get vehicles with max prio
            int maxPrio = Integer.MIN_VALUE;
            for (Vehicle vehicle : assessedVehicles.keySet()) {
                if (maxPrio <= vehicle.getDriver().getPriorityCounter()) {
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
                        if (config.friendlyStandingInJamEnabled)
                            // (2), different order than above for better performance
                            if (!(vehicle.getDriver().peekRoute().getLane(0).getMaxInsertionIndex() >= 0))
                                continue;
                    }

                    // if priority is truly greater than current max => remove all current vehicles of max priority
                    if (maxPrio < vehicle.getDriver().getPriorityCounter()) {
                        maxPrioVehicles.clear();
                        maxPrio = vehicle.getDriver().getPriorityCounter();
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
                boolean tooManyVehicles = config.onlyOneVehicleEnabled && maxPrioVehicles.size() > 1;
                if (!allOthersBeaten || tooManyVehicles) {
                    Iterator<Vehicle> bla = maxPrioVehicles.iterator();
                    for (int i = 0; i < random.nextInt(maxPrioVehicles.size()); i++)
                        bla.next();
                    Vehicle prioritizedVehicle = bla.next();
                    maxPrioVehicles.clear();
                    maxPrioVehicles.add(prioritizedVehicle);
                }
            }
        }

        anyChangeSinceUpdate = false;
    }

    /**
     * Registers the given vehicle at this node.
     * <p>
     * This method is synchronized because the assertion works with the information whether a vehicle is registered or
     * not => access should be after registration has finished.
     *
     * @param newVehicle This vehicle gets registered in this node.
     * @return true, if the given vehicle is getting registered; false otherwise (e.g. if it is already registered)
     */
    public synchronized boolean registerVehicle(Vehicle newVehicle) {
        if (isRegistered(newVehicle))
            return false;

        newRegisteredVehicles.add(newVehicle);
        anyChangeSinceUpdate = true;
        return true;
    }

    /**
     * Remove occurrence of the given vehicle in this node. Due to the complexity of the used hash maps, this method
     * has a runtime complexity in O(n logn), where n is the number of vehicles registered in this node. <br>
     * For each vehicle, the priority counter is updated and the other data structures containing the vehicle
     * getting unregistered are updated in O(log n) due to sets.
     *
     * @param vehicle This vehicle should being unregistered after this method
     * @return true, if the given vehicle has been registered and is unregistered now; false, if it hasn't been
     * registered at this node
     */
    public synchronized boolean unregisterVehicle(Vehicle vehicle) {

        Set<Vehicle> defeatedVehicles = assessedVehicles.remove(vehicle);
        if (defeatedVehicles == null) {
            newRegisteredVehicles.remove(vehicle);
            return false;
        }

        for (Vehicle otherVehicle : assessedVehicles.keySet()) {
            boolean otherWon = assessedVehicles.get(otherVehicle).remove(vehicle);

            if (otherWon)
                otherVehicle.getDriver().decPriorityCounter();
            else
                otherVehicle.getDriver().incPriorityCounter();
        }

        anyChangeSinceUpdate = true;
        return true;
    }

    /**
     * This method is synchronized because its return value depends on {@link #registerVehicle(Vehicle)},
     * {@link #unregisterVehicle(Vehicle)} and {@link #update()}, which can be called concurrently.
     *
     * @param vehicle This vehicle asks whether it has permission to cross or not
     * @return true if the vehicle has permission to cross, false otherwise
     */
    public synchronized boolean permissionToCross(Vehicle vehicle) {
        return maxPrioVehicles.contains(vehicle);
    }

    public synchronized boolean isRegistered(Vehicle vehicle) {
        return assessedVehicles.containsKey(vehicle) || newRegisteredVehicles.contains(vehicle);
    }

    /*
    |===========================|
    | add edges (preprocessing) |
    |===========================|
    */
    /**
     * This method adds one (one-directional) lane connector. Not every lane is connected to any
     * other lane and vehicles are only allowed to travel from one lane {@code incoming} to another lane
     * {@code leaving}, if a respective connector exists. Does not check for duplicates.
     *
     * @param incoming the edge from which this connector connects to the leaving edge.
     * @param leaving the edge to which this connector connects.
     */
    public void addConnector(Lane incoming, Lane leaving) {
        ArrayList<Lane> connectedLanes = connectors.computeIfAbsent(incoming, k -> new ArrayList<>());
        connectedLanes.add(leaving);
    }

    public void addLeavingEdge(DirectedEdge edge) {
        if (edge.getOrigin() != this)
            throw new IllegalArgumentException("edge.getOrigin() != this");

        leaving.put(edge, (byte) -1);
    }

    public void addIncomingEdge(DirectedEdge edge) {
        if (edge.getDestination() != this)
            throw new IllegalArgumentException("edge.getDestination() != this");

        incoming.put(edge, (byte) -1);
    }

    /**
     * This method should be called after all edges are added to this node. It
     * calculates the order of the edges that is needed for crossing logic
     * calculation.
     */
    public void updateEdgeIndices() {
        if (leaving.keySet().size() == 0 && incoming.keySet().size() == 0)
            return;


        /* init */
        final boolean IS_LEAVING = true;
        final boolean IS_INCOMING = false;
        HashMap<Vec2d, ArrayList<Tuple<DirectedEdge, Boolean>>> edges = new HashMap<>();
        Vec2d zero = null;

        /* get all vectors for sorting later */
        /* collect all leaving edges */
        for (DirectedEdge edge : leaving.keySet()) {
            // invert leaving XOR incoming edges
            Vec2d v = Vec2d.mul(edge.getOriginDirection(), -1);
            // important for hashing later due to:
            // 0.0 == -0.0 but new Double(0.0).hashCode() != new Double(-0.0).hashCode()
            if (v.x == 0.0)
                v.x = 0.0;
            if (v.y == 0.0)
                v.y = 0.0;

            // set zero reference if not done yet
            if (zero == null)
                zero = v;

            // add edge to its vector
            ArrayList<Tuple<DirectedEdge, Boolean>> vectorsEdges = edges.computeIfAbsent(v, k -> new ArrayList<>(2));
            vectorsEdges.add(new Tuple<>(edge, IS_LEAVING));
        }


        /* collect all incoming edges */
        for (DirectedEdge edge : incoming.keySet()) {
            Vec2d v = new Vec2d(edge.getDestinationDirection());
            // important for hashing later due to:
            // 0.0 == -0.0 but new Double(0.0).hashCode() != new Double(-0.0).hashCode()
            if (v.x == 0.0)
                v.x = 0.0;
            if (v.y == 0.0)
                v.y = 0.0;

            // set zero reference if not done yet
            if (zero == null)
                zero = v;

            // add edge to its vector
            ArrayList<Tuple<DirectedEdge, Boolean>> vectorsEdges = edges.computeIfAbsent(v, k -> new ArrayList<>(2));
            vectorsEdges.add(new Tuple<>(edge, IS_INCOMING));
        }

        /* now: all vectors are keys and can be sorted */
        Queue<Vec2d> sortedVectors = Geometry.sortClockwiseAsc(zero, edges.keySet(), !config.drivingOnTheRight);
        byte nextCrossingIndex = 0;
        // iterate over the sorted vectors
        while (!sortedVectors.isEmpty()) {
            Vec2d v = sortedVectors.poll();
            // take the current vector's edges
            ArrayList<Tuple<DirectedEdge, Boolean>> nextEdges = edges.remove(v);


            // add its leaving edges before its incoming edges
            for (Tuple<DirectedEdge, Boolean> nextEdge : nextEdges) {
                if (nextEdge.obj1 == IS_LEAVING) {
                    leaving.put(nextEdge.obj0, nextCrossingIndex++);
                }
            }

            for (Tuple<DirectedEdge, Boolean> nextEdge : nextEdges) {
                if (nextEdge.obj1 == IS_INCOMING) {
                    incoming.put(nextEdge.obj0, nextCrossingIndex++);
                }
            }
        }
    }

    /**
     * This method needs {@link #updateEdgeIndices() updated crossing indices for all edges} to work correctly.
     *
     * @return a map containing all lanes (concerning to an edge of this node) mapping to a crossing index.
     */
    public Map<Lane, Byte> calcLaneIndices() {
        boolean IS_LEAVING = true;
        boolean IS_INCOMING = false;


        /* add edges sorted by their index */
        FastSortedArrayList<Triple<Boolean, DirectedEdge, Byte>> sortedEdges = new FastSortedArrayList<>(
                leaving.size() + incoming.size(),
                (t1, t2) -> Byte.compare(t1.obj2, t2.obj2));
        for (Map.Entry<DirectedEdge, Byte> entry : leaving.entrySet())
            sortedEdges.add(new Triple<>(IS_LEAVING, entry.getKey(), entry.getValue()));
        for (Map.Entry<DirectedEdge, Byte> entry : incoming.entrySet())
            sortedEdges.add(new Triple<>(IS_INCOMING, entry.getKey(), entry.getValue()));


        /* iterate over lanes and fill map */
        final Map<Lane, Byte> indicesMap = new HashMap<>();
        byte nextCrossingIndex = 0;
        for (Triple<Boolean, DirectedEdge, Byte> triple : sortedEdges) {
            ArrayList<Lane> lanes = triple.obj1.getLanes();
            // if leaving => indices ascending like ascending lane-idx
            // if incoming => indices reverse to ascending lane-idx
            if (triple.obj0 == IS_INCOMING)
                Collections.reverse(lanes);

            for (Lane lane : lanes)
                indicesMap.put(lane, nextCrossingIndex++);
        }

        return indicesMap;
    }


    /*
    |======================|
    | (i) ShortestPathNode |
    |======================|
    */
    /**
     * Get all leaving edges for the specified incoming edge, i.e. all edges that are connected (via connectors) to the
     * incoming edge on this node in such a way that a vehicle is allowed to travel from the incoming edge to said
     * other edges.
     *
     * @param incoming The edge from which a travelling vehicle is arriving. The leaving edges may be depending on the
     *                 incoming edge, due to turn-restrictions. If {@code null}, every leaving edge will be returned
     *                 (i.e. if a vehicle has just spawned at a node and no previous edge is exists).
     * @return All leaving edges depending on the incoming edge.
     */
    @Override
    public Set<DirectedEdge> getLeavingEdges(DirectedEdge incoming) {
        // TODO: maybe pre-compute leaving edges?

        // return everything if incoming edge is null
        if (incoming == null)
            return Collections.unmodifiableSet(this.leaving.keySet());

        HashSet<DirectedEdge> result = new HashSet<>();
        for (Lane lane : incoming.getLanes()) {
            ArrayList<Lane> connected = connectors.get(lane);

            if (connected != null)
                for (Lane leaving : connected)
                    result.add(leaving.getAssociatedEdge());
        }

        return result;
    }

    public Set<DirectedEdge> getLeavingEdges() {
        return Collections.unmodifiableSet(leaving.keySet());
    }

    @Override
    public Set<DirectedEdge> getIncomingEdges() {
        return Collections.unmodifiableSet(incoming.keySet());
    }

    @Override
    public Coordinate getCoordinate() {
        return coordinate;
    }


    /*
    |================|
    | (i) Resettable |
    |================|
    */
    /**
     * The node empties its crossing sets etc. and resets its {@link Random}
     */
    @Override
    public synchronized void reset() {
        random.reset();
        assessedVehicles.clear();
        maxPrioVehicles.clear();
        newRegisteredVehicles.clear();
        anyChangeSinceUpdate = false;
    }


    /*
    |============|
    | (i) Seeded |
    |============|
    */
    @Override
    public void setSeed(long seed) {
        random.setSeed(seed);
    }

    @Override
    public long getSeed() {
        return random.getSeed();
    }
}
