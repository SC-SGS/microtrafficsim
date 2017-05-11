package logic.shortestpath;

import microtrafficsim.core.entities.street.StreetEntity;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streetgraph.StreetGraph;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.StreetType;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.astar.AStars;
import microtrafficsim.core.shortestpath.astar.BidirectionalAStars;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.math.Vec2d;
import microtrafficsim.utils.id.BasicLongIDGenerator;
import microtrafficsim.utils.id.LongGenerator;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Stack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * This test creates small graphs and checks a certain instance of {@link ShortestPathAlgorithm} defined in
 * {@link #setupClass()}.<br>
 * For more detailed information about adding new test cases or existing test cases,
 * see {@link #testAll()}.
 *
 * @author Dominic Parga Cacheiro
 */
public class TestShortestPathAlgorithms {

    private static final Logger logger = new EasyMarkableLogger(TestShortestPathAlgorithms.class);

    private static SimulationConfig config;
    private static ShortestPathAlgorithm<Node, DirectedEdge> shortestPathAlgorithm;
    private static Vec2d                                     rubbishVec2d;
    private static LongGenerator                             idGenerator;

    /* data structures for easier graph build */
    private final HashMap<String, Node> nodes = new HashMap<>();
    private final HashMap<String, StreetEntity> edges = new HashMap<>();

    private final int               maxVelocity = 1;
    private Stack<ShortestPathEdge> shortestPath;
    private Stack<DirectedEdge>     correctShortestPath;
    private Graph                   graph;
    private Node                    start;
    private Node                    end;
    private Coordinate              uselessPosition = new Coordinate(0, 0);

    @BeforeClass
    public static void setupClass() {
        config       = new SimulationConfig();
        rubbishVec2d = new Vec2d(1.0f, 1.0f);
        idGenerator  = new BasicLongIDGenerator();
    }

    @Before
    public void setup() {
        graph = new StreetGraph(new Bounds(0, 0, 0, 0));
        correctShortestPath = new Stack<>();
        shortestPath = new Stack<>();
        nodes.clear();
        edges.clear();
    }

    /*
    |=======|
    | tests |
    |=======|
    */
    @Test
    public void testDijkstra() {
        logger.info("");
        logger.info("NEW TEST: AStars.shortestPathDijkstra()");
        shortestPathAlgorithm = AStars.shortestPathDijkstra();
        shortestPathAlgorithm.preprocess();
        testAll();
    }

    @Test
    public void testFastestWayAStar() {
        logger.info("");
        logger.info("NEW TEST: AStars.fastestPathAStar()");
        shortestPathAlgorithm = AStars.fastestPathAStar(config.metersPerCell, config.globalMaxVelocity);
        shortestPathAlgorithm.preprocess();
        testAll();
    }

    @Test
    public void testLinearDistanceAStar() {
        logger.info("");
        logger.info("NEW TEST: AStars.shortestPathAStar()");
        shortestPathAlgorithm = AStars.shortestPathAStar(config.metersPerCell);
        shortestPathAlgorithm.preprocess();
        testAll();
    }

    @Test
    public void testBidirectionalDijkstra() {
        logger.info("");
        logger.info("NEW TEST: BidirectionalAStars.shortestPathDijkstra()");
        shortestPathAlgorithm = BidirectionalAStars.shortestPathDijkstra();
        shortestPathAlgorithm.preprocess();
        testAll();
    }

    @Test
    public void testFastestWayBidirectionalAStar() {
        logger.info("");
        logger.info("NEW TEST: BidirectionalAStars.fastestPathAStar()");
        shortestPathAlgorithm = BidirectionalAStars.fastestPathAStar(config.metersPerCell, config.globalMaxVelocity);
        shortestPathAlgorithm.preprocess();
        testAll();
    }

    @Test
    public void testLinearDistanceBidirectionalAStar() {
        logger.info("");
        logger.info("NEW TEST: BidirectionalAStars.shortestPathAStar()");
        shortestPathAlgorithm = BidirectionalAStars.shortestPathAStar(config.metersPerCell);
        shortestPathAlgorithm.preprocess();
        testAll();
    }

    /**
     * <p>
     * Executes: <br>
     * &bull {@link #isDangerous()} <br>
     * &bull {@link #isShortestPathCorrect()} <br>
     * &bull {@link #multipleCorrectPathsPossible()} <br>
     *
     * <p>
     * A {@code @Test} method is needed if you want to add a new shortest path algorithm for testing <br>
     * A new test case implementation has to be added to this method.
     *
     */
    private void testAll() {
        isDangerous();
        isShortestPathCorrect();
        multipleCorrectPathsPossible();
    }

    /*
    |================|
    | test case impl |
    |================|
    */
    /**
     * <p>
     * This method creates a graph using {@link DirectedEdge} and {@link Node} and checks the shortest path algorithm
     * for:<br>
     * &bull Is the shortest path correct? This check differs from the method {@link #isShortestPathCorrect()} in the
     * graph (this graph here is a few nodes smaller). <br>
     * &bull Is the shortest path not changing the graph? <br>
     * &bull Is the shortest path empty if there does not exist a shortest path?
     *
     * <p>
     * Console output is generated, so you will be able to differentiate the errors above.
     */
    private void isDangerous() {
        // create nodes
        nodes.put("a", new Node(idGenerator.next(), uselessPosition, config.crossingLogic));
        nodes.put("b", new Node(idGenerator.next(), uselessPosition, config.crossingLogic));
        nodes.put("c", new Node(idGenerator.next(), uselessPosition, config.crossingLogic));
        nodes.put("d", new Node(idGenerator.next(), uselessPosition, config.crossingLogic));
        nodes.put("e", new Node(idGenerator.next(), uselessPosition, config.crossingLogic));

        // create edges and add them to the nodes
        edges.put("ab", createAndAddForwardEdge(1, "a", "b", 1));
        edges.put("bc", createAndAddForwardEdge(2, "b", "c", 1));
        edges.put("bd", createAndAddForwardEdge(5, "b", "d", 1));
        edges.put("cd", createAndAddForwardEdge(1, "c", "d", 1));
        edges.put("de", createAndAddForwardEdge(1, "d", "e", 1));
        edges.put("ec", createAndAddForwardEdge(1, "e", "c", 1));


        finishBuildingGraph();


        graph.updateGraphGUID();
        String graphBefore = graph.toString();

        // shortest path
        start = nodes.get("a");
        end   = nodes.get("e");

        shortestPath.clear();
        shortestPathAlgorithm.findShortestPath(start, end, shortestPath);

        // correct path
        correctShortestPath.clear();
        correctShortestPath.push((DirectedEdge) edges.get("de").getForwardEdge());
        correctShortestPath.push((DirectedEdge) edges.get("cd").getForwardEdge());
        correctShortestPath.push((DirectedEdge) edges.get("bc").getForwardEdge());
        correctShortestPath.push((DirectedEdge) edges.get("ab").getForwardEdge());

        String graphAfter = graph.toString();

        // tests
        logger.info("Test: Is shortest path correct?");
        assertEquals(correctShortestPath, shortestPath);

        logger.info("Test: Correct and graph has not been mutated?");
        assertEquals(graphBefore, graphAfter);

        logger.info("Test: Correct if no path exists?");
        Node f = new Node(idGenerator.next(), uselessPosition, config.crossingLogic);
        shortestPath.clear();
        shortestPathAlgorithm.findShortestPath(start, f, shortestPath);
        assertTrue(shortestPath.isEmpty());
    }

    /**
     * <p>
     * This method creates a graph using {@link DirectedEdge} and {@link Node} and checks the shortest path algorithm
     * for its correctness. The graph contains circles.
     */
    private void isShortestPathCorrect() {
        // create nodes
        nodes.put("a", new Node(idGenerator.next(), uselessPosition, config.crossingLogic));
        nodes.put("b", new Node(idGenerator.next(), uselessPosition, config.crossingLogic));
        nodes.put("c", new Node(idGenerator.next(), uselessPosition, config.crossingLogic));
        nodes.put("d", new Node(idGenerator.next(), uselessPosition, config.crossingLogic));
        nodes.put("e", new Node(idGenerator.next(), uselessPosition, config.crossingLogic));
        nodes.put("f", new Node(idGenerator.next(), uselessPosition, config.crossingLogic));
        nodes.put("g", new Node(idGenerator.next(), uselessPosition, config.crossingLogic));
        nodes.put("h", new Node(idGenerator.next(), uselessPosition, config.crossingLogic));

        // create edges and add them to the nodes
        edges.put("ab", createAndAddEdge(1, "a", "b", 1));
        edges.put("ac", createAndAddForwardEdge(1, "a", "c", 3));
        edges.put("bc", createAndAddForwardEdge(2, "b", "c", 3));
        edges.put("de", createAndAddForwardEdge(1, "d", "e", 3));
        edges.put("df", createAndAddForwardEdge(1, "d", "f", 3));
        edges.put("ea", createAndAddForwardEdge(1, "e", "a", 1));
        edges.put("fh", createAndAddForwardEdge(1, "f", "h", 2));
        edges.put("gd", createAndAddForwardEdge(1, "g", "d", 3));
        edges.put("gf", createAndAddForwardEdge(1, "g", "f", 2));
        edges.put("he", createAndAddForwardEdge(1, "h", "e", 4));
        edges.put("hg", createAndAddForwardEdge(1, "h", "g", 4));


        finishBuildingGraph();


        // finish
        graph.updateGraphGUID();

        // shortest path
        start = nodes.get("g");
        end   = nodes.get("c");

        shortestPath.clear();
        shortestPathAlgorithm.findShortestPath(start, end, shortestPath);

        // correct path
        correctShortestPath.clear();
        correctShortestPath.push((DirectedEdge) edges.get("ac").getForwardEdge());
        correctShortestPath.push((DirectedEdge) edges.get("ea").getForwardEdge());
        correctShortestPath.push((DirectedEdge) edges.get("de").getForwardEdge());
        correctShortestPath.push((DirectedEdge) edges.get("gd").getForwardEdge());

        logger.info("Test: Is shortest path correct?");
        assertEquals(correctShortestPath, shortestPath);
    }

    /**
     * <p>
     * This method creates a graph using {@link DirectedEdge} and {@link Node} and checks the shortest path algorithm
     * for correctness, if there is more than one shortest path.
     */
    private void multipleCorrectPathsPossible() {
        /* create nodes */
        nodes.put("a", new Node(idGenerator.next(), uselessPosition, config.crossingLogic));
        nodes.put("b", new Node(idGenerator.next(), uselessPosition, config.crossingLogic));
        nodes.put("c", new Node(idGenerator.next(), uselessPosition, config.crossingLogic));
        nodes.put("d", new Node(idGenerator.next(), uselessPosition, config.crossingLogic));
        nodes.put("e", new Node(idGenerator.next(), uselessPosition, config.crossingLogic));
        nodes.put("f", new Node(idGenerator.next(), uselessPosition, config.crossingLogic));
        nodes.put("g", new Node(idGenerator.next(), uselessPosition, config.crossingLogic));
        nodes.put("h", new Node(idGenerator.next(), uselessPosition, config.crossingLogic));

        /* create edges and add them to the nodes */
        edges.put("ab", createAndAddEdge(1, "a", "b", 1));
        edges.put("ac", createAndAddForwardEdge(1, "a", "c", 3));
        edges.put("bc", createAndAddForwardEdge(1, "b", "c", 2));
        edges.put("de", createAndAddForwardEdge(1, "d", "e", 1));
        edges.put("df", createAndAddForwardEdge(1, "d", "f", 3));
        edges.put("ea", createAndAddForwardEdge(1, "e", "a", 1));
        edges.put("eb", createAndAddForwardEdge(1, "e", "b", 1));
        edges.put("fh", createAndAddForwardEdge(1, "f", "h", 2));
        edges.put("gd", createAndAddForwardEdge(1, "g", "d", 3));
        edges.put("gf", createAndAddForwardEdge(1, "g", "f", 2));
        edges.put("he", createAndAddForwardEdge(1, "h", "e", 4));
        edges.put("hg", createAndAddForwardEdge(1, "h", "g", 4));


        finishBuildingGraph();


        start = nodes.get("g");
        end   = nodes.get("c");

        shortestPath.clear();
        shortestPathAlgorithm.findShortestPath(start, end, shortestPath);

        /* correct paths */
        correctShortestPath.clear();
        /* g-d-e-b-c */
        correctShortestPath.push((DirectedEdge) edges.get("bc").getForwardEdge());
        correctShortestPath.push((DirectedEdge) edges.get("eb").getForwardEdge());
        correctShortestPath.push((DirectedEdge) edges.get("de").getForwardEdge());
        correctShortestPath.push((DirectedEdge) edges.get("gd").getForwardEdge());
        logger.info("Test: Correct if there are two shortest paths?");
        boolean isCorrect = shortestPath.equals(correctShortestPath);
        if (!isCorrect) {
            /* g-d-e-a-c */
            correctShortestPath.clear();
            correctShortestPath.push((DirectedEdge) edges.get("ac").getForwardEdge());
            correctShortestPath.push((DirectedEdge) edges.get("ea").getForwardEdge());
            correctShortestPath.push((DirectedEdge) edges.get("de").getForwardEdge());
            correctShortestPath.push((DirectedEdge) edges.get("gd").getForwardEdge());
            assertTrue(shortestPath.equals(correctShortestPath));
        }
    }


    /*
    |=======|
    | utils |
    |=======|
    */
    private StreetEntity createAndAddEdge(int lengthInCells, String originStr, String destinationStr, int nLanes) {
        Node origin = nodes.get(originStr);
        Node destination = nodes.get(destinationStr);

        DirectedEdge forward = new DirectedEdge(
                idGenerator.next(),
                lengthInCells * config.metersPerCell,
                new StreetType(StreetType.UNCLASSIFIED),
                nLanes,
                maxVelocity,
                origin,
                destination,
                rubbishVec2d,
                rubbishVec2d,
                config.metersPerCell,
                type -> (byte) 0
        );

        DirectedEdge backward = new DirectedEdge(
                idGenerator.next(),
                lengthInCells * config.metersPerCell,
                new StreetType(StreetType.UNCLASSIFIED),
                nLanes,
                maxVelocity,
                destination,
                origin,
                rubbishVec2d,
                rubbishVec2d,
                config.metersPerCell,
                type -> (byte) 0
        );

        StreetEntity entity = new StreetEntity(forward, backward, null);
        forward.setEntity(entity);
        backward.setEntity(entity);

        origin.addEdge(forward);
        origin.addEdge(backward);
        destination.addEdge(forward);
        destination.addEdge(backward);

        return entity;
    }

    private StreetEntity createAndAddForwardEdge(int lengthInCells,
                                                 String originStr,
                                                 String destinationStr,
                                                 int nLanes) {
        Node origin = nodes.get(originStr);
        Node destination = nodes.get(destinationStr);

        DirectedEdge forward = new DirectedEdge(
                idGenerator.next(),
                lengthInCells * config.metersPerCell,
                new StreetType(StreetType.UNCLASSIFIED),
                nLanes,
                maxVelocity,
                origin,
                destination,
                rubbishVec2d,
                rubbishVec2d,
                config.metersPerCell,
                type -> (byte) 0
        );

        StreetEntity entity = new StreetEntity(forward, null, null);
        forward.setEntity(entity);

        origin.addEdge(forward);
        destination.addEdge(forward);

        return entity;
    }

    private StreetEntity createAndAddBackwardEdge(int lengthInCells,
                                                  String originStr,
                                                  String destinationStr,
                                                  int nLanes) {
        Node origin = nodes.get(originStr);
        Node destination = nodes.get(destinationStr);

        DirectedEdge backward = new DirectedEdge(
                idGenerator.next(),
                lengthInCells * config.metersPerCell,
                new StreetType(StreetType.UNCLASSIFIED),
                nLanes,
                maxVelocity,
                destination,
                origin,
                rubbishVec2d,
                rubbishVec2d,
                config.metersPerCell,
                type -> (byte) 0
        );

        StreetEntity entity = new StreetEntity(backward, null, null);
        backward.setEntity(entity);

        origin.addEdge(backward);
        destination.addEdge(backward);

        return entity;
    }

    private void finishBuildingGraph() {
        /* add nodes to the graph */
        nodes.values().forEach(graph::addNode);


        /* add edges to the graph */
        for (StreetEntity entity : edges.values()) {
            DirectedEdge forward  = (DirectedEdge) entity.getForwardEdge();
            DirectedEdge backward = (DirectedEdge) entity.getBackwardEdge();

            if (forward != null)
                graph.addEdge(forward);
            if (backward != null)
                graph.addEdge(backward);
        }


        /* create turning lanes */
        for (Node node : nodes.values()) {
            for (DirectedEdge incoming : node.getIncomingEdges()) {
                for (DirectedEdge leaving : node.getLeavingEdges()) {
                    node.addConnector(incoming.getLane(0), leaving.getLane(0));
                }
            }
            node.getLeavingEdges(null);
        }
    }
}
