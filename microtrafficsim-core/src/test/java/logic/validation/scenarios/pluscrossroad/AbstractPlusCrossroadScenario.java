package logic.validation.scenarios.pluscrossroad;

import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.core.simulation.scenarios.impl.QueueScenarioSmall;
import microtrafficsim.core.simulation.utils.ODMatrix;
import microtrafficsim.core.simulation.utils.SparseODMatrix;
import microtrafficsim.utils.collections.Tuple;
import microtrafficsim.utils.collections.Triple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public abstract class AbstractPlusCrossroadScenario extends QueueScenarioSmall {

    public final Node bottomLeft, topLeft, mid, topRight, bottomRight;
    private final HashMap<Node, Integer> lengthToMid;

    public AbstractPlusCrossroadScenario(ScenarioConfig config,
                                         Graph graph,
                                         Supplier<VisualizationVehicleEntity> visVehicleFactory) {
        super(config, graph);


        /* get nodes sorted by lon */
        ArrayList<Node> sortedNodes = new ArrayList<>(getGraph().getNodes());
        sortedNodes.sort((n1, n2) -> n1.getCoordinate().lon > n2.getCoordinate().lon ? 1 : -1);

        bottomLeft  = sortedNodes.get(0); // length to mid: 8
        topLeft     = sortedNodes.get(1); // length to mid: 7
        mid              = sortedNodes.get(2); // length to mid: 0
        topRight    = sortedNodes.get(3); // length to mid: 9
        bottomRight = sortedNodes.get(4); // length to mid: 12

        /* street lengths for general sub-scenario definition (below) */
        lengthToMid = new HashMap<>();
        lengthToMid.put(bottomLeft,   8);
        lengthToMid.put(topLeft,      7);
        lengthToMid.put(mid,          0);
        lengthToMid.put(topRight,     9);
        lengthToMid.put(bottomRight, 12);


        init();
        setScenarioBuilder(new Builder(config.seed, visVehicleFactory));
    }

    /**
     * @param config
     * @return the given config updated; just for practical purpose
     */
    public static ScenarioConfig setupConfig(ScenarioConfig config) {

        QueueScenarioSmall.setupConfig(config);

        config.maxVehicleCount                            = 4;
        config.crossingLogic.friendlyStandingInJamEnabled = true;

        return config;
    }

    protected abstract void init();


    protected final void addPriorityToTheRight(Node rightStart) {
        addBothStraight(rightStart, neighbor(rightStart, true));
    }

    protected final void addPriorityTurning(Node prioStart) {
        addOneTurning(prioStart, opposite(prioStart), getConfig().crossingLogic.drivingOnTheRight);
    }


    protected final void addOneTurning(Node straightStart, Node turnStart, boolean leftTurn) {

        Node straightEnd = opposite(straightStart);
        Node turnEnd     = neighbor(turnStart, leftTurn);

        addSubScenario(
                new Tuple<>(straightStart, straightEnd),
                new Tuple<>(turnStart, turnEnd)
        );
    }

    protected final void addBothStraight(Node aStart, Node bStart) {

        Node aEnd = opposite(aStart);
        Node bEnd = opposite(bStart);

        addSubScenario(
                new Tuple<>(aStart, aEnd),
                new Tuple<>(bStart, bEnd)
        );
    }

    protected final void addSubScenario(Tuple<Node, Node>... routes) {

        Triple<Node, Node, Integer>[] triples = new Triple[routes.length];

        for (int i = 0; i < routes.length; i++)
            triples[i] = new Triple<>(routes[i].obj0, routes[i].obj1, null);

        addSubScenario(triples);
    }

    protected final void addSubScenario(Triple<Node, Node, Integer>... routes) {

        ODMatrix odMatrix         = new SparseODMatrix();
        ODMatrix spawnDelayMatrix = new SparseODMatrix();

        // used for spawn delay
        int maxLength = 0;

        // used for all not defined spawn delays
        for (Triple<Node, Node, Integer> route : routes) {
            Integer tmp = route.obj2;
            if (tmp == null)
                tmp = 0;

            tmp = Math.max(tmp, lengthToMid.get(route.obj0)); // route.obj0 <=> origin
            maxLength = Math.max(maxLength, tmp);
        }

        for (Triple<Node, Node, Integer> route : routes) {
            Node origin      = route.obj0;
            Node destination = route.obj1;
            odMatrix.add(1, origin, destination);

            // calculate spawn delay of this route
            Integer delay = route.obj2;
            if (delay == null)
                delay = maxLength - lengthToMid.get(origin);
            spawnDelayMatrix.add(delay, origin, destination);
        }

        addSubScenario(odMatrix, spawnDelayMatrix);
    }


    private Node opposite(Node node) {

        if (node == topRight)
            return bottomLeft;

        if (node == topLeft)
            return bottomRight;

        if (node == bottomLeft)
            return topRight;

        if (node == bottomRight)
            return topLeft;

        return null;
    }

    private Node neighbor(Node node, boolean cw) {

        if (node == topRight)
            return cw ? bottomRight : topLeft;

        if (node == topLeft)
            return cw ? topRight : bottomLeft;

        if (node == bottomLeft)
            return cw ? topLeft : bottomRight;

        if (node == bottomRight)
            return cw ? bottomLeft : topRight;

        return null;
    }
}