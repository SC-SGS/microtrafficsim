package serialization.graph;

import microtrafficsim.core.convenience.parser.DefaultParserConfig;
import microtrafficsim.core.exfmt.Container;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.extractor.streetgraph.StreetGraphExtractor;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streetgraph.StreetGraph;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.map.MapProperties;
import microtrafficsim.core.map.style.impl.DarkStyleSheet;
import microtrafficsim.core.serialization.ExchangeFormatSerializer;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.utils.resources.PackagedResource;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class StreetGraphExchangeFormatTest {

    /**
     * If not null, this file is used instead of the default 'map.osm'.
     */
    private static final File OPTIONAL_TEST_FILE = null;


    private static Graph osm;
    private static Graph xfm;

    @BeforeClass
    public static void initializeTestData() throws Exception {
        File osmxml;

        //noinspection ConstantConditions
        if (OPTIONAL_TEST_FILE == null)
            osmxml = new PackagedResource(StreetGraphExchangeFormatTest.class, "/preprocessing/graph/map.osm").asTemporaryFile();
        else
            osmxml = OPTIONAL_TEST_FILE;

        osm = loadGraphOsm(osmxml);
        xfm = serializeDeserialize(osm);
    }

    private static Graph loadGraphOsm(File file) throws Exception {
        return DefaultParserConfig.get(getConfig()).build().parse(file, new MapProperties(true)).streetgraph;
    }

    private static SimulationConfig getConfig() {
        SimulationConfig config = new SimulationConfig();

        config.maxVehicleCount                            = 1000;
        config.speedup                                    = Integer.MAX_VALUE;
        config.seed                                       = 0;
        config.multiThreading.nThreads                    = 8;
        config.crossingLogic.drivingOnTheRight            = true;
        config.crossingLogic.edgePriorityEnabled          = true;
        config.crossingLogic.priorityToTheRightEnabled    = true;
        config.crossingLogic.friendlyStandingInJamEnabled = true;
        config.crossingLogic.onlyOneVehicleEnabled        = false;
        config.visualization.style                        = new DarkStyleSheet();

        return config;
    }

    private static Graph serializeDeserialize(Graph graph) throws Exception {
        ExchangeFormat fmt = ExchangeFormat.getDefault();
        fmt.getConfig().set(new StreetGraphExtractor.Config(getConfig()));
        ExchangeFormatSerializer ser = ExchangeFormatSerializer.create();

        File tmp = File.createTempFile("map", "mtsm");

        // store
        Container container = fmt.manipulator()
                .inject(graph)
                .getContainer();

        ser.write(tmp, container);

        // load
        return fmt.manipulator(ser.read(tmp)).extract(StreetGraph.class);
    }


    /*
    |=======|
    | by id |
    |=======|
    */
    @Test
    public void testFullSerializationAndDeserializationByIds() {
        assertEquals(osm.getNodes().size(), xfm.getNodes().size());
        assertEquals(osm.getEdges().size(), xfm.getEdges().size());

        // check nodes
        for (Node osmNode : osm.getNodes()) {
            Node xfmNode = nodeById(xfm.getNodes(), osmNode);
            assertNotNull(xfmNode);

            // check incoming edges
            assertEquals(osmNode.getIncomingEdges().size(), xfmNode.getIncomingEdges().size());
            for (DirectedEdge osmEdge : osmNode.getIncomingEdges()) {
                DirectedEdge xfmEdge = edgeByIds(xfmNode.getIncomingEdges(), osmEdge);
                assertNotNull(xfmEdge);
            }

            // check leaving edges
            assertEquals(osmNode.getLeavingEdges().size(), xfmNode.getLeavingEdges().size());
            for (DirectedEdge osmEdge : osmNode.getLeavingEdges()) {
                DirectedEdge xfmEdge = edgeByIds(xfmNode.getLeavingEdges(), osmEdge);
                assertNotNull(xfmEdge);
            }

            // check connectors
            assertEquals(osmNode.getConnectors().size(), xfmNode.getConnectors().size());
            for (Map.Entry<DirectedEdge.Lane, TreeMap<DirectedEdge, DirectedEdge.Lane>> osmConnector
                    : osmNode.getConnectors().entrySet())
            {
                Map.Entry<DirectedEdge.Lane, TreeMap<DirectedEdge, DirectedEdge.Lane>> xfmConnector;
                xfmConnector = connectorByIds(xfmNode.getConnectors().entrySet(), osmConnector);
                assertNotNull(xfmConnector);
            }
        }

        // check edges
        for (DirectedEdge osmEdge : osm.getEdges()) {
            DirectedEdge xfmEdge = edgeByIds(xfm.getEdges(), osmEdge);
            assertNotNull(xfmEdge);

            // TODO: check other properties
        }
    }


    private Node nodeById(Set<Node> nodes, Node query) {
        for (Node node : nodes)
            if (node.getId() == query.getId())
                return node;

        return null;
    }

    private DirectedEdge edgeByIds(Set<DirectedEdge> edges, DirectedEdge query) {
        for (DirectedEdge edge : edges)
            if (edgeEqualsByIds(edge, query))
                return edge;

        return null;
    }

    private Map.Entry<DirectedEdge.Lane, TreeMap<DirectedEdge, DirectedEdge.Lane>> connectorByIds(
            Set<Map.Entry<DirectedEdge.Lane, TreeMap<DirectedEdge, DirectedEdge.Lane>>> connectors,
            Map.Entry<DirectedEdge.Lane, TreeMap<DirectedEdge, DirectedEdge.Lane>> query)
    {
        for (Map.Entry<DirectedEdge.Lane, TreeMap<DirectedEdge, DirectedEdge.Lane>> connector : connectors) {
            if (connectorEqualsById(connector, query))
                return connector;
        }

        return null;
    }


    private boolean edgeEqualsByIds(DirectedEdge a, DirectedEdge b) {
        return a.getId() == b.getId()
                && a.getOrigin().getId() == b.getOrigin().getId()
                && a.getDestination().getId() == b.getDestination().getId();
    }

    private boolean laneEqualsByIds(DirectedEdge.Lane a, DirectedEdge.Lane b) {
        return edgeEqualsByIds(a.getEdge(), b.getEdge()) && a.getIndex() == b.getIndex();
    }

    private boolean lanesEqualsByIds(Collection<DirectedEdge.Lane> a, Collection<DirectedEdge.Lane> b) {
        for (DirectedEdge.Lane la : a) {
            boolean found = false;

            for (DirectedEdge.Lane lb : b) {
                if (laneEqualsByIds(la, lb)) {
                    found = true;
                    break;
                }
            }

            if (!found)
                return false;
        }

        return true;
    }

    private boolean connectorEqualsById(Map.Entry<DirectedEdge.Lane, TreeMap<DirectedEdge, DirectedEdge.Lane>> a,
                                        Map.Entry<DirectedEdge.Lane, TreeMap<DirectedEdge, DirectedEdge.Lane>> b) {
        return laneEqualsByIds(a.getKey(), b.getKey()) && lanesEqualsByIds(a.getValue().values(), b.getValue().values());
    }


    /*
    |=========|
    | by keys |
    |=========|
    */
    @Test
    public void testFullSerializationAndDeserializationByKeys() {
        assertEquals(osm.getNodes().size(), xfm.getNodes().size());
        assertEquals(osm.getEdges().size(), xfm.getEdges().size());

        // check nodes
        for (Node osmNode : osm.getNodes()) {
            Node xfmNode = nodeByKey(xfm.getNodes(), osmNode);
            assertNotNull(xfmNode);

            // check incoming edges
            assertEquals(osmNode.getIncomingEdges().size(), xfmNode.getIncomingEdges().size());
            for (DirectedEdge osmEdge : osmNode.getIncomingEdges()) {
                DirectedEdge xfmEdge = edgeByKeys(xfmNode.getIncomingEdges(), osmEdge);
                assertNotNull(xfmEdge);
            }

            // check leaving edges
            assertEquals(osmNode.getLeavingEdges().size(), xfmNode.getLeavingEdges().size());
            for (DirectedEdge osmEdge : osmNode.getLeavingEdges()) {
                DirectedEdge xfmEdge = edgeByKeys(xfmNode.getLeavingEdges(), osmEdge);
                assertNotNull(xfmEdge);
            }

            // check connectors
            assertEquals(osmNode.getConnectors().size(), xfmNode.getConnectors().size());
            for (Map.Entry<DirectedEdge.Lane, TreeMap<DirectedEdge, DirectedEdge.Lane>> osmConnector
                    : osmNode.getConnectors().entrySet())
            {
                Map.Entry<DirectedEdge.Lane, TreeMap<DirectedEdge, DirectedEdge.Lane>> xfmConnector
                        = connectorByKeys(xfmNode.getConnectors().entrySet(), osmConnector);
                assertNotNull(xfmConnector);
            }
        }

        // check edges
        for (DirectedEdge osmEdge : osm.getEdges()) {
            DirectedEdge xfmEdge = edgeByKeys(xfm.getEdges(), osmEdge);
            assertNotNull(xfmEdge);

            // TODO: check other properties
        }
    }


    private Node nodeByKey(Set<Node> nodes, Node query) {
        for (Node node : nodes)
            if (node.key().equals(query.key()))
                return node;

        return null;
    }

    private DirectedEdge edgeByKeys(Set<DirectedEdge> edges, DirectedEdge query) {
        for (DirectedEdge edge : edges)
            if (edgeEqualsByKeys(edge, query))
                return edge;

        return null;
    }

    private Map.Entry<DirectedEdge.Lane, TreeMap<DirectedEdge, DirectedEdge.Lane>> connectorByKeys(
            Set<Map.Entry<DirectedEdge.Lane, TreeMap<DirectedEdge, DirectedEdge.Lane>>> connectors,
            Map.Entry<DirectedEdge.Lane, TreeMap<DirectedEdge, DirectedEdge.Lane>> query)
    {
        for (Map.Entry<DirectedEdge.Lane, TreeMap<DirectedEdge, DirectedEdge.Lane>> connector : connectors) {
            if (connectorEqualsByKey(connector, query))
                return connector;
        }

        return null;
    }


    private boolean edgeEqualsByKeys(DirectedEdge a, DirectedEdge b) {
        return a.key().equals(b.key())
                && a.getOrigin().key().equals(b.getOrigin().key())
                && a.getDestination().key().equals(b.getDestination().key());
    }

    private boolean laneEqualsByKeys(DirectedEdge.Lane a, DirectedEdge.Lane b) {
        return edgeEqualsByKeys(a.getEdge(), b.getEdge()) && a.getIndex() == b.getIndex();
    }

    private boolean lanesEqualsByKeys(Collection<DirectedEdge.Lane> a, Collection<DirectedEdge.Lane> b) {
        for (DirectedEdge.Lane la : a) {
            boolean found = false;

            for (DirectedEdge.Lane lb : b) {
                if (laneEqualsByKeys(la, lb)) {
                    found = true;
                    break;
                }
            }

            if (!found)
                return false;
        }

        return true;
    }

    private boolean connectorEqualsByKey(
            Map.Entry<DirectedEdge.Lane, TreeMap<DirectedEdge, DirectedEdge.Lane>> a,
            Map.Entry<DirectedEdge.Lane, TreeMap<DirectedEdge, DirectedEdge.Lane>> b)
    {
        return laneEqualsByKeys(a.getKey(), b.getKey()) && lanesEqualsByKeys(a.getValue().values(), b.getValue().values());
    }
}
