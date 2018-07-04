package microtrafficsim.core.logic.nodes;

import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.logic.vehicles.driver.Driver;
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
import microtrafficsim.utils.collections.Tuple;
import microtrafficsim.utils.functional.Procedure2;
import microtrafficsim.utils.strings.builder.LevelStringBuilder;

import java.util.*;


/**
 * This class represents one crossing point of two or more {@link DirectedEdge}s.
 * <p>
 * {@code ShortestPathNode} serves functionality for shortest path calculations.
 *
 * @author Jan-Oliver Schmidt, Dominic Parga Cacheiro
 */
public class Node implements ShortestPathNode<DirectedEdge>, Resettable, Seeded, Comparable<Node> {

    private final long          id;
    private Coordinate          coordinate;
    private CrossingLogicConfig config;
    private final Random        random;

    // crossing logic
    private HashSet<Vehicle>               registerLog;
    private PriorityQueue<Vehicle>         newRegisteredVehicles;
    private TreeMap<Vehicle, Set<Vehicle>> assessedVehicles;
    private TreeSet<Vehicle>               maxPrioVehicles;
    private boolean                        anyChangeSinceUpdate;
    private TreeMap<DirectedEdge.Lane, TreeMap<DirectedEdge, DirectedEdge.Lane>> connectors;

    // edges
    private final TreeSet<DirectedEdge> leaving;
    private final TreeSet<DirectedEdge> incoming;
    private final TreeMap<DirectedEdge.Lane, Byte> leavingLanes;     // lane, index(for crossing logic)
    private final TreeMap<DirectedEdge.Lane, Byte> incomingLanes;     // lane, index(for crossing logic)


    /**
     * Default constructor
     */
    public Node(long id, Coordinate coordinate, CrossingLogicConfig config) {
        this.id         = id;
        this.coordinate = coordinate;
        this.config     = config;

        // crossing logic
        random                = new Random();  // set below for determinism
        registerLog           = new HashSet<>();
        assessedVehicles      = new TreeMap<>(Comparator.comparingLong(Vehicle::getId));
        maxPrioVehicles       = new TreeSet<>(Comparator.comparingLong(Vehicle::getId));
        newRegisteredVehicles = new PriorityQueue<>(Comparator.comparingLong(Vehicle::getId));
        anyChangeSinceUpdate  = false;

        // edges
        connectors = new TreeMap<>();
        leaving = new TreeSet<>();
        incoming = new TreeSet<>();
        leavingLanes = new TreeMap<>();
        incomingLanes = new TreeMap<>();
    }

    public Key key() {
        return new Key(this);
    }

    @Override
    public String toString() {
        return "Node id = " + id + " at " + coordinate.toString();
    }

    public String toStringVerbose() {
        LevelStringBuilder builder = new LevelStringBuilder()
                .setDefaultLevelSeparator()
                .setDefaultLevelSubString();

        builder.appendln("<Node>").incLevel(); {
            /* id */
            builder.appendln("<id>").incLevel(); {
                builder.appendln(id);
            } builder.decLevel().appendln("</id>").appendln();


            /* coordinate */
            builder.appendln("<coordinate>").incLevel(); {
                builder.appendln(coordinate.toString());
            } builder.decLevel().appendln("</coordinate>").appendln();


            Procedure2<String, Set<DirectedEdge>> edgesToString = (type, set) -> {
                builder.appendln("<" + type + " edges>").incLevel(); {
                    Iterator<DirectedEdge> iter = set.iterator();
                    while (iter.hasNext()) {
                        DirectedEdge edge = iter.next();
                        builder.appendln(edge);

                        if (iter.hasNext())
                            builder.appendln();
                    }
                } builder.decLevel().appendln("</" + type + " edges>").appendln();
            };


            edgesToString.invoke("incoming", incoming);
            edgesToString.invoke("leaving", leaving);

            builder.appendln("<connectors: incoming(edge_laneIdx) -> leaving(edge_laneIdx)>").incLevel(); {
                connectors.entrySet().forEach(entry -> {
                    DirectedEdge.Lane incomingLane = entry.getKey();
                    entry.getValue().entrySet().forEach(leaving -> {
                        DirectedEdge.Lane leavingLane = leaving.getValue();
                        builder.appendln(incomingLane.getEdge().getId() + "_" + incomingLane.getIndex()
                                + " -> "
                                + leavingLane.getEdge().getId() + "_" + leavingLane.getIndex());
                    });
                });
            } builder.decLevel().appendln("</connectors>");

        } builder.decLevel().append("</Node>");
        return builder.toString();
    }

    public long getId() {
        return id;
    }

    public CrossingLogicConfig getCrossingLogicConfig() {
        return config;
    }

    public TreeMap<DirectedEdge.Lane, TreeMap<DirectedEdge, DirectedEdge.Lane>> getConnectors() {
        return connectors;
    }

    /*
    |================|
    | crossing logic |
    |================|
    */
    /**
     * Rules:<br>
     * &bull; two not-spawned vehicles are compared by their IDs. The greater id wins.<br>
     * &bull; spawned vehicles gets priority over not spawned vehicles. This makes sense when thinking about the
     * situation, when you want to enter the street from your private parking place.<br>
     * &bull; two spawned vehicles means they are coming from a street and want to make a turn. Thus they have to be
     * compared by the crossing logic (below).<br>
     * IMPORTANT: The registration does NOT check the positions relative to each other vehicle ON THE STREET, but it
     * checks/compares all information relevant for the crossing itself.
     * <p>
     * At first, the crossing logic checks whether the two vehicles' turning ways are crossing each other (otherwise
     * return 0). If they are crossing, the origin-priorities are compared. If equal, the destination-priorities are
     * compared. If equal, they have to be compared by right-before-left or randomly. All sub-comparisons can be
     * enabled/disabled with the {@link SimulationConfig}.
     *
     * @return an {@code int > 0} if v1 has priority over v2; an {@code int < 0} if v2 has priority over v1; an {@code int = 0} if v1 and v2
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
        assert v1.getLane() != null : "Vehicle 1 in node-comparator has no lane!";
        assert v2.getLane() != null : "Vehicle 2 in node-comparator has no lane!";
        DirectedEdge.Lane v1LeavingLane = getLeavingLane(v1.getLane(), v1.getDriver().peekRoute());
        DirectedEdge.Lane v2LeavingLane = getLeavingLane(v2.getLane(), v2.getDriver().peekRoute());
        assert v1LeavingLane != null : "Vehicle 1 in node-comparator has no matching leaving lane!";
        assert v2LeavingLane != null : "Vehicle 2 in node-comparator has no matching leaving lane!";
        byte origin1        = incomingLanes.get(v1.getLane());
        byte destination1   = leavingLanes.get(v1LeavingLane);
        byte origin2        = incomingLanes.get(v2.getLane());
        byte destination2   = leavingLanes.get(v2LeavingLane);
        assert MathUtils.min(origin1, destination1, origin2, destination2) >= 0 : "Wrong crossing indices";

        byte supremum = (byte) (1 + MathUtils.max(origin1, destination1, origin2, destination2));
        assert supremum >= 0 : "Crossing indices cannot be stored as byte any longer.";

        // if vehicles are crossing each other's way
        if (IndicesCalculator.areIndicesCrossing(origin1, destination1, origin2, destination2, supremum)) {
            // compare priorities of origins
            byte cmp = (byte) (v1.getLane().getEdge().getPriorityLevel() - v2.getLane().getEdge().getPriorityLevel());
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
    public void update() {

        /* add new registered vehicles */
        while (!newRegisteredVehicles.isEmpty()) { // invariant: all vehicles in this set are new at this point
            Vehicle newVehicle = newRegisteredVehicles.poll();
            Set<Vehicle> defeatedVehicles = new TreeSet<>(Comparator.comparingLong(Vehicle::getId));

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
                Driver driver = vehicle.getDriver();
                if (maxPrio <= driver.getPriorityCounter()) {
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
                        if (config.friendlyStandingInJamEnabled) {
                            DirectedEdge.Lane leavingLane;
                            if (vehicle.getState() == VehicleState.SPAWNED) {
                                leavingLane = getLeavingLane(vehicle.getLane(), driver.peekRoute());
                            } else {
                                leavingLane = vehicle.getDriver().peekRoute().getLane(0);
                            }
                            // (2), different order than above for better performance
                            if (!(leavingLane.getMaxInsertionIndex() >= 0)) {
                                continue;
                            }
                        }
                    }

                    // if priority is truly greater than current max => remove all current vehicles of max priority
                    if (maxPrio < driver.getPriorityCounter()) {
                        maxPrioVehicles.clear();
                        maxPrio = driver.getPriorityCounter();
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
     * not {@literal ->} access should be after registration has finished.
     *
     * @param newVehicle This vehicle gets registered in this node.
     * @return true, if the given vehicle is getting registered; false otherwise (e.g. if it is already registered)
     */
    public synchronized boolean registerVehicle(Vehicle newVehicle) {
        if (isRegistered(newVehicle))
            return false;

        newRegisteredVehicles.add(newVehicle);
        anyChangeSinceUpdate = true;

        registerLog.add(newVehicle);
        return true;
    }

    /**
     * Remove occurrence of the given vehicle in this node. Due to the complexity of the used {@link TreeMap}s, this
     * method has a runtime complexity in O(n logn), where n is the number of vehicles registered in this node. <br>
     * For each vehicle, the priority counter is updated and the other data structures containing the vehicle
     * getting unregistered are updated in O(log n) due to sets.
     *
     * @param vehicle This vehicle should being unregistered after this method
     * @return true, if the given vehicle has been registered and is unregistered now; false, if it hasn't been
     * registered at this node
     */
    public synchronized boolean unregisterVehicle(Vehicle vehicle) {
        if (!isRegistered(vehicle))
            return false;

        Set<Vehicle> defeatedVehicles = assessedVehicles.remove(vehicle);
        if (defeatedVehicles == null) {
            newRegisteredVehicles.remove(vehicle);
        } else {
            for (Vehicle otherVehicle : assessedVehicles.keySet()) {
                boolean otherWon = assessedVehicles.get(otherVehicle).remove(vehicle);

                if (otherWon)
                    otherVehicle.getDriver().decPriorityCounter();
                else
                    otherVehicle.getDriver().incPriorityCounter();
            }
            maxPrioVehicles.remove(vehicle);

            anyChangeSinceUpdate = true;
        }

        registerLog.remove(vehicle);
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
        return registerLog.contains(vehicle);
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
    public void addConnector(DirectedEdge.Lane incoming, DirectedEdge.Lane leaving) {
        TreeMap<DirectedEdge, DirectedEdge.Lane> connectedLanes
                = connectors.computeIfAbsent(incoming, k -> new TreeMap<>());
        connectedLanes.put(leaving.getEdge(), leaving);
    }

    public void addLeavingEdge(DirectedEdge edge) {
        if (edge.getOrigin() != this)
            throw new IllegalArgumentException("edge.getOrigin() != this");

        leaving.add(edge);
        edge.forEach(lane -> leavingLanes.put(lane, (byte) -1));
    }

    public void addIncomingEdge(DirectedEdge edge) {
        if (edge.getDestination() != this)
            throw new IllegalArgumentException("edge.getDestination() != this");

        incoming.add(edge);
        edge.forEach(lane -> incomingLanes.put(lane, (byte) -1));
    }

    /**
     * This method should be called after all edges are added to this node. It calculates the order of the edges'
     * lanes that is needed for crossing logic calculation.
     */
    public synchronized void updateCrossingIndices() {
        Tuple<TreeMap<DirectedEdge.Lane, Byte>, TreeMap<DirectedEdge.Lane, Byte>> tuple = calcCrossingIndices();
        leavingLanes.clear();
        leavingLanes.putAll(tuple.obj0);
        incomingLanes.clear();
        incomingLanes.putAll(tuple.obj1);
    }

    /**
     * @return tuple (leaving lane indices map, incoming lane indices map)
     */
    public synchronized Tuple<TreeMap<DirectedEdge.Lane, Byte>, TreeMap<DirectedEdge.Lane, Byte>> calcCrossingIndices() {
        if (leaving.size() == 0 && incoming.size() == 0)
            return new Tuple<>(new TreeMap<>(), new TreeMap<>());

        TreeMap<DirectedEdge.Lane, Byte> leavingLanes = new TreeMap<>();
        TreeMap<DirectedEdge.Lane, Byte> incomingLanes = new TreeMap<>();


        /* init */
        final boolean IS_LEAVING = true;
        final boolean IS_INCOMING = false;
        HashMap<Vec2d, ArrayList<Tuple<DirectedEdge, Boolean>>> edges = new HashMap<>();

        Vec2d zero = null;

        /* get all vectors for sorting later */
        /* collect all leaving edges */
        for (DirectedEdge edge : leaving) {
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
        for (DirectedEdge edge : incoming) {
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
                    // if leaving => indices ascending like ascending lane-idx
                    Iterator<DirectedEdge.Lane> iter = nextEdge.obj0.iterator();
                    while (iter.hasNext())
                        leavingLanes.put(iter.next(), nextCrossingIndex++);
                }
            }

            for (Tuple<DirectedEdge, Boolean> nextEdge : nextEdges) {
                if (nextEdge.obj1 == IS_INCOMING) {
                    // if incoming => indices reverse to ascending lane-idx
                    Iterator<DirectedEdge.Lane> iter = nextEdge.obj0.reverseIterator();
                    while (iter.hasNext())
                        incomingLanes.put(iter.next(), nextCrossingIndex++);
                }
            }
        }

        return new Tuple<>(leavingLanes, incomingLanes);
    }


    public synchronized int findOutermostTurningLaneIndex(DirectedEdge incoming, DirectedEdge leaving) {
        for (DirectedEdge.Lane lane : incoming)
            if (isLaneCorrect(lane, leaving))
                return lane.getIndex();

        return -1;
    }

    public synchronized boolean isLaneCorrect(DirectedEdge.Lane incomingLane, DirectedEdge leavingEdge) {
        return getLeavingLane(incomingLane, leavingEdge) != null;
    }

    public synchronized DirectedEdge.Lane getLeavingLane(DirectedEdge.Lane incomingLane, DirectedEdge leavingEdge) {
        TreeMap<DirectedEdge, DirectedEdge.Lane> leaving = connectors.get(incomingLane);
        if (leaving == null)
            return null;
        return leaving.get(leavingEdge);
    }

    /**
     * Get all leaving edges for the specified incoming edge, i.e. all edges that are connected (via connectors) to the
     * incoming edge on this node in such a way that a vehicle is allowed to travel from the incoming edge to said
     * other edges.
     *
     * @param incomingEdge The edge from which a travelling vehicle is arriving. The leaving edges may be depending
     *                     on the incoming edge, due to turn-restrictions. If {@code null}, every leaving edge will
     *                     be returned (i.e. if a vehicle has just spawned at a node and no previous edge is exists).
     * @return All leaving edges depending on the incoming edge.
     */
    @Override
    public synchronized Set<DirectedEdge> getLeavingEdges(DirectedEdge incomingEdge) {
        // TODO: maybe pre-compute leaving edges?

        // return everything if incoming edge is null
        if (incomingEdge == null)
            return Collections.unmodifiableSet(leaving);

        TreeSet<DirectedEdge> result = new TreeSet<>();
        for (DirectedEdge.Lane incomingLane : incomingEdge) {
            TreeMap<DirectedEdge, DirectedEdge.Lane> leaving = connectors.get(incomingLane);
            if (leaving != null)
                result.addAll(leaving.keySet());
        }

        return result;
    }

    public synchronized Set<DirectedEdge> getLeavingEdges() {
        return getLeavingEdges(null);
    }

    @Override
    public synchronized Set<DirectedEdge> getIncomingEdges(DirectedEdge leavingEdge) {
        // TODO: maybe pre-compute incoming edges?

        // return everything if leaving edge is null
        if (leavingEdge == null)
            return Collections.unmodifiableSet(incoming);

        TreeSet<DirectedEdge> result = new TreeSet<>();
        connectors.entrySet().forEach(entry -> {
            TreeMap<DirectedEdge, DirectedEdge.Lane> leaving = entry.getValue();
            if (leaving != null) {
                if (leaving.containsKey(leavingEdge)) {
                    DirectedEdge.Lane incomingLane = entry.getKey();
                    result.add(incomingLane.getEdge());
                }
            }
        });

        return result;
    }

    public synchronized Set<DirectedEdge> getIncomingEdges() {
        return getIncomingEdges(null);
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


    @Override
    public int compareTo(Node o) {
        return key().compareTo(o.key());
    }

    public static class Key implements Comparable<Key> {
        private long nodeId;

        private Key() {

        }

        private Key(Node node) {
            nodeId = node.id;
        }

        @Override
        public int compareTo(Node.Key o) {
            return Long.compare(nodeId, o.nodeId);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;

            if (!(obj instanceof Node.Key))
                return false;

            return compareTo((Node.Key) obj) == 0;
        }
    }
}
