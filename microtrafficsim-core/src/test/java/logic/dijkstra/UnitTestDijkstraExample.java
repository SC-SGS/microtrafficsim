package logic.dijkstra;

import microtrafficsim.core.shortestpath.ShortestPathEdge;
import microtrafficsim.core.shortestpath.impl.ShortestWayDijkstra;
import microtrafficsim.core.logic.DirectedEdge;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.math.Vec2f;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class UnitTestDijkstraExample {
    private static final Logger logger = LoggerFactory.getLogger(UnitTestDijkstraExample.class);

    private static SimulationConfig    config;
    private static ShortestWayDijkstra dijkstra;
    private static Vec2f               rubbish;
    private final int                  maxVelocity = -1;
    private Queue<? extends ShortestPathEdge> correctShortestPath;
    private LinkedList<DirectedEdge>          shortestPath;
    private StreetGraph                       graph;
    private Node                              start, end;
    private Coordinate                        uselessPosition = new Coordinate(0, 0);

    @BeforeClass
    public static void setupClass() {
        config   = new SimulationConfig();
        dijkstra = new ShortestWayDijkstra();
        rubbish  = new Vec2f(1.0f, 1.0f);
    }

    @Before
    public void setup() {
        graph        = new StreetGraph(0, 0, 0, 0);
        shortestPath = new LinkedList<>();
    }

    @Test
    public void isDijkstraDangerous() {
        // create nodes
        Node a = new Node(config, uselessPosition);
        Node b = new Node(config, uselessPosition);
        Node c = new Node(config, uselessPosition);
        Node d = new Node(config, uselessPosition);
        Node e = new Node(config, uselessPosition);

        // create edges and add them to the nodes
        DirectedEdge ab = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 1, a, b, (byte) 0);
        a.addEdge(ab);
        b.addEdge(ab);

        DirectedEdge bc = new DirectedEdge(config, 2 * config.metersPerCell, rubbish, rubbish, maxVelocity, 1, b, c, (byte) 0);
        b.addEdge(bc);
        c.addEdge(bc);

        DirectedEdge bd = new DirectedEdge(config, 5 * config.metersPerCell, rubbish, rubbish, maxVelocity, 1, b, d, (byte) 0);
        b.addEdge(bd);
        d.addEdge(bd);

        DirectedEdge cd = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 1, c, d, (byte) 0);
        c.addEdge(cd);
        d.addEdge(cd);

        DirectedEdge de = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 1, d, e, (byte) 0);
        d.addEdge(de);
        e.addEdge(de);

        DirectedEdge ec = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 1, e, c, (byte) 0);
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

        correctShortestPath = dijkstra.findShortestPath(start, end);

        // correct path
        shortestPath.addLast(ab);
        shortestPath.addLast(bc);
        shortestPath.addLast(cd);
        shortestPath.addLast(de);

        String graphAfter = graph.toString();

        // tests
        logger.info("Test: Is shortest path correct?");
        assertEquals(null, shortestPath, correctShortestPath);

        logger.info("Test: Correct and graph has not been mutated?");
        assertEquals(null, graphBefore, graphAfter);

        logger.info("Test: Correct if no path exists?");
        Node f              = new Node(config, uselessPosition);
        correctShortestPath = dijkstra.findShortestPath(start, f);
        assertEquals(null, new LinkedList<DirectedEdge>(), correctShortestPath);
    }

    @Test
    public void isShortestPathCorrect() {
        // create nodes
        Node a = new Node(config, uselessPosition);
        Node b = new Node(config, uselessPosition);
        Node c = new Node(config, uselessPosition);
        Node d = new Node(config, uselessPosition);
        Node e = new Node(config, uselessPosition);
        Node f = new Node(config, uselessPosition);
        Node g = new Node(config, uselessPosition);
        Node h = new Node(config, uselessPosition);

        // create edges and add them to the nodes
        DirectedEdge ab = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 1, a, b, (byte) 0);
        a.addEdge(ab);
        b.addEdge(ab);

        DirectedEdge ac = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 3, a, c, (byte) 0);
        a.addEdge(ac);
        c.addEdge(ac);

        DirectedEdge ba = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 1, b, a, (byte) 0);
        a.addEdge(ba);
        b.addEdge(ba);

        DirectedEdge bc = new DirectedEdge(config, 2 * config.metersPerCell, rubbish, rubbish, maxVelocity, 3, b, c, (byte) 0);
        b.addEdge(bc);
        c.addEdge(bc);

        DirectedEdge de = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 1, d, e, (byte) 0);
        d.addEdge(de);
        e.addEdge(de);

        DirectedEdge df = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 3, e, f, (byte) 0);
        d.addEdge(df);
        f.addEdge(df);

        DirectedEdge ea = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 1, e, a, (byte) 0);
        e.addEdge(ea);
        a.addEdge(ea);

        DirectedEdge fh = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 2, f, h, (byte) 0);
        f.addEdge(fh);
        h.addEdge(fh);

        DirectedEdge gd = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 3, g, d, (byte) 0);
        g.addEdge(gd);
        d.addEdge(gd);

        DirectedEdge gf = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 2, g, f, (byte) 0);
        g.addEdge(gf);
        f.addEdge(gf);

        DirectedEdge he = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 4, h, e, (byte) 0);
        h.addEdge(he);
        e.addEdge(he);

        DirectedEdge hg = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 4, h, g, (byte) 0);
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

        logger.info(a.toString());
        logger.info(e.toString());
        correctShortestPath = dijkstra.findShortestPath(start, end);

        // correct path
        shortestPath.addLast(gd);
        shortestPath.addLast(de);
        shortestPath.addLast(ea);
        shortestPath.addLast(ac);

        logger.info("Test: Is shortest path correct?");
        assertEquals(null, shortestPath, correctShortestPath);
    }

    @Test
    public void multipleCorrectPathsPossible() {
        // create nodes
        Node a = new Node(config, uselessPosition);
        Node b = new Node(config, uselessPosition);
        Node c = new Node(config, uselessPosition);
        Node d = new Node(config, uselessPosition);
        Node e = new Node(config, uselessPosition);
        Node f = new Node(config, uselessPosition);
        Node g = new Node(config, uselessPosition);
        Node h = new Node(config, uselessPosition);

        // create edges and add them to the nodes
        DirectedEdge ab = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 1, a, b, (byte) 0);
        a.addEdge(ab);
        b.addEdge(ab);

        DirectedEdge ac = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 3, a, c, (byte) 0);
        a.addEdge(ac);
        c.addEdge(ac);

        DirectedEdge ba = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 1, b, a, (byte) 0);
        a.addEdge(ba);
        b.addEdge(ba);

        DirectedEdge bc = new DirectedEdge(config, 2 * config.metersPerCell, rubbish, rubbish, maxVelocity, 2, b, c, (byte) 0);
        b.addEdge(bc);
        c.addEdge(bc);

        DirectedEdge de = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 1, d, e, (byte) 0);
        d.addEdge(de);
        e.addEdge(de);

        DirectedEdge df = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 3, e, f, (byte) 0);
        d.addEdge(df);
        f.addEdge(df);

        DirectedEdge ea = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 1, e, a, (byte) 0);
        e.addEdge(ea);
        a.addEdge(ea);

        DirectedEdge fh = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 2, f, h, (byte) 0);
        f.addEdge(fh);
        h.addEdge(fh);

        DirectedEdge gd = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 3, g, d, (byte) 0);
        g.addEdge(gd);
        d.addEdge(gd);

        DirectedEdge gf = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 2, g, f, (byte) 0);
        g.addEdge(gf);
        f.addEdge(gf);

        DirectedEdge he = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 4, h, e, (byte) 0);
        h.addEdge(he);
        e.addEdge(he);

        DirectedEdge hg = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, maxVelocity, 4, h, g, (byte) 0);
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

        correctShortestPath = dijkstra.findShortestPath(start, end);

        // correct paths
        shortestPath.addLast(gd);
        shortestPath.addLast(de);
        shortestPath.addLast(ea);
        shortestPath.addLast(ac);

        LinkedList<DirectedEdge> sP2 = new LinkedList<>();
        sP2.addLast(gd);
        sP2.addLast(de);
        sP2.addLast(ea);
        sP2.addLast(ab);
        sP2.addLast(bc);

        logger.info("Test: Correct if there are two shortest paths?");
        assertTrue(null, correctShortestPath.equals(shortestPath) || correctShortestPath.equals(sP2));
    }

    @Test
    public void blabla() {
        Node a = new Node(config, new Coordinate(0, 0));
        Node b = new Node(config, new Coordinate(0, 0));
        Node c = new Node(config, new Coordinate(0, 0));

        DirectedEdge ab = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, 5, 1, a, b, (byte) 0);
        a.addEdge(ab);
        b.addEdge(ab);

        DirectedEdge bc = new DirectedEdge(config, 1 * config.metersPerCell, rubbish, rubbish, 5, 1, b, c, (byte) 0);
        b.addEdge(bc);
        c.addEdge(bc);
    }
}
