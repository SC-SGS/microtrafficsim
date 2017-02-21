package logic.shortestpath;

import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streetgraph.StreetGraph;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.astar.AStar;
import microtrafficsim.core.shortestpath.astar.BidirectionalAStar;
import microtrafficsim.core.shortestpath.astar.impl.FastestWayAStar;
import microtrafficsim.core.shortestpath.astar.impl.FastestWayBidirectionalAStar;
import microtrafficsim.core.shortestpath.astar.impl.LinearDistanceAStar;
import microtrafficsim.core.shortestpath.astar.impl.LinearDistanceBidirectionalAStar;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.math.Vec2d;
import microtrafficsim.utils.id.BasicLongIDGenerator;
import microtrafficsim.utils.id.LongGenerator;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

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

    private static ScenarioConfig        config;
    private static ShortestPathAlgorithm shortestPathAlgorithm;
    private static Vec2d                 rubbishVec2d;
    private static LongGenerator         idGenerator;

    private final int               maxVelocity = 1;
    private Stack<ShortestPathEdge> shortestPath;
    private Stack<DirectedEdge>     correctShortestPath;
    private Graph                   graph;
    private Node                    start;
    private Node                    end;
    private Coordinate              uselessPosition = new Coordinate(0, 0);

    @BeforeClass
    public static void setupClass() {
        config       = new ScenarioConfig();
        rubbishVec2d = new Vec2d(1.0f, 1.0f);
        idGenerator  = new BasicLongIDGenerator();
    }

    @Before
    public void setup() {
        graph = new StreetGraph(0, 0, 0, 0);
        correctShortestPath = new Stack<>();
        shortestPath = new Stack<>();
    }

    /*
    |=======|
    | tests |
    |=======|
    */
    @Test
    public void testDijkstra() {
        logger.info("");
        logger.info("NEW TEST CLASS: ShortestWayDijkstra");
        shortestPathAlgorithm = AStar.createShortestWayDijkstra();
        shortestPathAlgorithm.preprocess();
        testAll();
    }

    @Test
    public void testFastestWayAStar() {
        logger.info("");
        logger.info("NEW TEST CLASS: " + FastestWayAStar.class.getSimpleName());
        shortestPathAlgorithm = new FastestWayAStar(config.metersPerCell, config.globalMaxVelocity);
        shortestPathAlgorithm.preprocess();
        testAll();
    }

    @Test
    public void testLinearDistanceAStar() {
        logger.info("");
        logger.info("NEW TEST CLASS: " + LinearDistanceAStar.class.getSimpleName());
        shortestPathAlgorithm = new LinearDistanceAStar(config.metersPerCell);
        shortestPathAlgorithm.preprocess();
        testAll();
    }

    @Test
    public void testBidirectionalDijkstra() {
        logger.info("");
        logger.info("NEW TEST CLASS: BidirectionalShortestWayDijkstra");
        shortestPathAlgorithm = BidirectionalAStar.createShortestWayDijkstra();
        shortestPathAlgorithm.preprocess();
        testAll();
    }

    @Test
    public void testFastestWayBidirectionalAStar() {
        logger.info("");
        logger.info("NEW TEST CLASS: " + FastestWayBidirectionalAStar.class.getSimpleName());
        shortestPathAlgorithm = new FastestWayBidirectionalAStar(config.metersPerCell, config.globalMaxVelocity);
        shortestPathAlgorithm.preprocess();
        testAll();
    }

    @Test
    public void testLinearDistanceBidirectionalAStar() {
        logger.info("");
        logger.info("NEW TEST CLASS: " + LinearDistanceBidirectionalAStar.class.getSimpleName());
        shortestPathAlgorithm = new LinearDistanceBidirectionalAStar(config.metersPerCell);
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
    private DirectedEdge createEdge(int lengthInCells, Node origin, Node destination, int noOfLines) {
        return new DirectedEdge(
                idGenerator.next(),
                lengthInCells * config.metersPerCell,
                rubbishVec2d,
                rubbishVec2d,
                origin,
                destination,
                config.metersPerCell,
                noOfLines,
                maxVelocity,
                (byte)0
        );
    }

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
        Node a = new Node(idGenerator.next(), uselessPosition, config.crossingLogic);
        Node b = new Node(idGenerator.next(), uselessPosition, config.crossingLogic);
        Node c = new Node(idGenerator.next(), uselessPosition, config.crossingLogic);
        Node d = new Node(idGenerator.next(), uselessPosition, config.crossingLogic);
        Node e = new Node(idGenerator.next(), uselessPosition, config.crossingLogic);

        // create edges and add them to the nodes
        DirectedEdge ab = createEdge(1, a, b, 1);
        a.addEdge(ab);
        b.addEdge(ab);

        DirectedEdge bc = createEdge(2, b, c, 1);
        b.addEdge(bc);
        c.addEdge(bc);

        DirectedEdge bd = createEdge(5, b, d, 1);
        b.addEdge(bd);
        d.addEdge(bd);

        DirectedEdge cd = createEdge(1, c, d, 1);
        c.addEdge(cd);
        d.addEdge(cd);

        DirectedEdge de = createEdge(1, d, e, 1);
        d.addEdge(de);
        e.addEdge(de);

        DirectedEdge ec = createEdge(1, e, c, 1);
        e.addEdge(ec);
        c.addEdge(ec);

        // add nodes to the graph
        graph.registerEdgeAndNodes(ab);
        graph.registerEdgeAndNodes(bc);
        graph.registerEdgeAndNodes(bd);
        graph.registerEdgeAndNodes(cd);
        graph.registerEdgeAndNodes(de);
        graph.registerEdgeAndNodes(ec);

        // create turning lanes
        b.addConnector(ab.getLane(0), bc.getLane(0), null);
        b.addConnector(ab.getLane(0), bd.getLane(0), null);
        c.addConnector(bc.getLane(0), cd.getLane(0), null);
        c.addConnector(ec.getLane(0), cd.getLane(0), null);
        d.addConnector(bd.getLane(0), de.getLane(0), null);
        d.addConnector(cd.getLane(0), de.getLane(0), null);
        e.addConnector(de.getLane(0), ec.getLane(0), null);

        String graphBefore = graph.toString();

        // shortest path
        start = a;
        end   = e;

        shortestPath.clear();
        shortestPathAlgorithm.findShortestPath(start, end, shortestPath);

        // correct path
        correctShortestPath.clear();
        correctShortestPath.push(de);
        correctShortestPath.push(cd);
        correctShortestPath.push(bc);
        correctShortestPath.push(ab);

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
        Node a = new Node(idGenerator.next(), uselessPosition, config.crossingLogic);
        Node b = new Node(idGenerator.next(), uselessPosition, config.crossingLogic);
        Node c = new Node(idGenerator.next(), uselessPosition, config.crossingLogic);
        Node d = new Node(idGenerator.next(), uselessPosition, config.crossingLogic);
        Node e = new Node(idGenerator.next(), uselessPosition, config.crossingLogic);
        Node f = new Node(idGenerator.next(), uselessPosition, config.crossingLogic);
        Node g = new Node(idGenerator.next(), uselessPosition, config.crossingLogic);
        Node h = new Node(idGenerator.next(), uselessPosition, config.crossingLogic);

        // create edges and add them to the nodes
        DirectedEdge ab = createEdge(1, a, b, 1);
        a.addEdge(ab);
        b.addEdge(ab);

        DirectedEdge ac = createEdge(1, a, c, 3);
        a.addEdge(ac);
        c.addEdge(ac);

        DirectedEdge ba = createEdge(1, b, a, 1);
        a.addEdge(ba);
        b.addEdge(ba);

        DirectedEdge bc = createEdge(2, b, c, 3);
        b.addEdge(bc);
        c.addEdge(bc);

        DirectedEdge de = createEdge(1, d, e, 1);
        d.addEdge(de);
        e.addEdge(de);

        DirectedEdge df = createEdge(1, e, f, 3);
        d.addEdge(df);
        f.addEdge(df);

        DirectedEdge ea = createEdge(1, e, a, 1);
        e.addEdge(ea);
        a.addEdge(ea);

        DirectedEdge fh = createEdge(1, f, h, 2);
        f.addEdge(fh);
        h.addEdge(fh);

        DirectedEdge gd = createEdge(1, g, d, 3);
        g.addEdge(gd);
        d.addEdge(gd);

        DirectedEdge gf = createEdge(1, g, f, 2);
        g.addEdge(gf);
        f.addEdge(gf);

        DirectedEdge he = createEdge(1, h, e, 4);
        h.addEdge(he);
        e.addEdge(he);

        DirectedEdge hg = createEdge(1, h, g, 4);
        h.addEdge(hg);
        g.addEdge(hg);

        // add nodes to the graph
        graph.registerEdgeAndNodes(ab);
        graph.registerEdgeAndNodes(ac);
        graph.registerEdgeAndNodes(ba);
        graph.registerEdgeAndNodes(bc);
        graph.registerEdgeAndNodes(de);
        graph.registerEdgeAndNodes(df);
        graph.registerEdgeAndNodes(ea);
        graph.registerEdgeAndNodes(fh);
        graph.registerEdgeAndNodes(gd);
        graph.registerEdgeAndNodes(gf);
        graph.registerEdgeAndNodes(he);
        graph.registerEdgeAndNodes(hg);

        // create turning lanes
        a.addConnector(ea.getLane(0), ab.getLane(0), null);
        a.addConnector(ea.getLane(0), ac.getLane(0), null);
        b.addConnector(ab.getLane(0), bc.getLane(0), null);
        d.addConnector(gd.getLane(0), de.getLane(0), null);
        e.addConnector(de.getLane(0), ea.getLane(0), null);
        e.addConnector(he.getLane(0), ea.getLane(0), null);
        f.addConnector(df.getLane(0), fh.getLane(0), null);
        f.addConnector(gf.getLane(0), fh.getLane(0), null);
        g.addConnector(hg.getLane(0), gd.getLane(0), null);
        g.addConnector(hg.getLane(0), gf.getLane(0), null);
        h.addConnector(fh.getLane(0), he.getLane(0), null);
        h.addConnector(fh.getLane(0), hg.getLane(0), null);

        // shortest path
        start = g;
        end   = c;

        shortestPath.clear();
        shortestPathAlgorithm.findShortestPath(start, end, shortestPath);

        // correct path
        correctShortestPath.clear();
        correctShortestPath.push(ac);
        correctShortestPath.push(ea);
        correctShortestPath.push(de);
        correctShortestPath.push(gd);

        logger.info("Test: Is shortest path correct?");
        assertEquals(correctShortestPath, shortestPath);
    }

    /**
     * <p>
     * This method creates a graph using {@link DirectedEdge} and {@link Node} and checks the shortest path algorithm
     * for correctness, if there is more than one shortest path.
     */
    private void multipleCorrectPathsPossible() {
        // create nodes
        Node a = new Node(idGenerator.next(), uselessPosition, config.crossingLogic);
        Node b = new Node(idGenerator.next(), uselessPosition, config.crossingLogic);
        Node c = new Node(idGenerator.next(), uselessPosition, config.crossingLogic);
        Node d = new Node(idGenerator.next(), uselessPosition, config.crossingLogic);
        Node e = new Node(idGenerator.next(), uselessPosition, config.crossingLogic);
        Node f = new Node(idGenerator.next(), uselessPosition, config.crossingLogic);
        Node g = new Node(idGenerator.next(), uselessPosition, config.crossingLogic);
        Node h = new Node(idGenerator.next(), uselessPosition, config.crossingLogic);

        // create edges and add them to the nodes
        DirectedEdge ab = createEdge(1, a, b, 1);
        a.addEdge(ab);
        b.addEdge(ab);

        DirectedEdge ac = createEdge(1, a, c, 3);
        a.addEdge(ac);
        c.addEdge(ac);

        DirectedEdge ba = createEdge(1, b, a, 1);
        a.addEdge(ba);
        b.addEdge(ba);

        DirectedEdge bc = createEdge(1, b, c, 2);
        b.addEdge(bc);
        c.addEdge(bc);

        DirectedEdge de = createEdge(1, d, e, 1);
        d.addEdge(de);
        e.addEdge(de);

        DirectedEdge df = createEdge(1, e, f, 3);
        d.addEdge(df);
        f.addEdge(df);

        DirectedEdge ea = createEdge(1, e, a, 1);
        e.addEdge(ea);
        a.addEdge(ea);

        DirectedEdge eb = createEdge(1, e, b, 1);
        e.addEdge(eb);
        b.addEdge(eb);

        DirectedEdge fh = createEdge(1, f, h, 2);
        f.addEdge(fh);
        h.addEdge(fh);

        DirectedEdge gd = createEdge(1, g, d, 3);
        g.addEdge(gd);
        d.addEdge(gd);

        DirectedEdge gf = createEdge(1, g, f, 2);
        g.addEdge(gf);
        f.addEdge(gf);

        DirectedEdge he = createEdge(1, h, e, 4);
        h.addEdge(he);
        e.addEdge(he);

        DirectedEdge hg = createEdge(1, h, g, 4);
        h.addEdge(hg);
        g.addEdge(hg);

        // add nodes to the graph
//        graph.registerEdgeAndNodes(ab);
//        graph.registerEdgeAndNodes(ac);
//        graph.registerEdgeAndNodes(ba);
//        graph.registerEdgeAndNodes(bc);
//        graph.registerEdgeAndNodes(de);
//        graph.registerEdgeAndNodes(df);
//        graph.registerEdgeAndNodes(ea);
//        graph.registerEdgeAndNodes(eb);
//        graph.registerEdgeAndNodes(fh);
//        graph.registerEdgeAndNodes(gd);
//        graph.registerEdgeAndNodes(gf);
//        graph.registerEdgeAndNodes(he);
//        graph.registerEdgeAndNodes(hg);

        // create turning lanes
//        a.addConnector(ea.getLane(0), ab.getLane(0), null);
//        a.addConnector(ea.getLane(0), ac.getLane(0), null);
//        b.addConnector(ab.getLane(0), bc.getLane(0), null);
//        d.addConnector(gd.getLane(0), de.getLane(0), null);
//        e.addConnector(de.getLane(0), ea.getLane(0), null);
//        e.addConnector(he.getLane(0), ea.getLane(0), null);
//        f.addConnector(df.getLane(0), fh.getLane(0), null);
//        f.addConnector(gf.getLane(0), fh.getLane(0), null);
//        g.addConnector(hg.getLane(0), gd.getLane(0), null);
//        g.addConnector(hg.getLane(0), gf.getLane(0), null);
//        h.addConnector(fh.getLane(0), he.getLane(0), null);
//        h.addConnector(fh.getLane(0), hg.getLane(0), null);

        // shortest path
        start = g;
        end   = c;

        shortestPath.clear();
        shortestPathAlgorithm.findShortestPath(start, end, shortestPath);

        // correct paths
        correctShortestPath.clear();
        // g-d-e-b-c
        correctShortestPath.push(bc);
        correctShortestPath.push(eb);
        correctShortestPath.push(de);
        correctShortestPath.push(gd);
        logger.info("Test: Correct if there are two shortest paths?");
        boolean isCorrect = shortestPath.equals(correctShortestPath);
        if (!isCorrect) {
            // g-d-e-a-c
            correctShortestPath.clear();
            correctShortestPath.push(ac);
            correctShortestPath.push(ea);
            correctShortestPath.push(de);
            correctShortestPath.push(gd);
            assertTrue(shortestPath.equals(correctShortestPath));
        }
    }
}
