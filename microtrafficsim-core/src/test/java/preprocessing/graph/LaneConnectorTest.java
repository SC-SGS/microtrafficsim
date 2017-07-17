package preprocessing.graph;

import microtrafficsim.core.convenience.parser.DefaultParserConfig;
import microtrafficsim.core.exfmt.ExchangeFormat;
import microtrafficsim.core.exfmt.exceptions.NotAvailableException;
import microtrafficsim.core.exfmt.extractor.map.QuadTreeTiledMapSegmentExtractor;
import microtrafficsim.core.exfmt.extractor.streetgraph.StreetGraphExtractor;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streetgraph.StreetGraph;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.map.MapProperties;
import microtrafficsim.core.map.MapSegment;
import microtrafficsim.core.map.tiles.QuadTreeTiledMapSegment;
import microtrafficsim.core.map.tiles.QuadTreeTilingScheme;
import microtrafficsim.core.serialization.ExchangeFormatSerializer;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.math.MathUtils;
import microtrafficsim.math.Vec2d;
import microtrafficsim.utils.resources.PackagedResource;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


public class LaneConnectorTest {

    /**
     * If not null, this file is used instead of the default 'map.osm'.
     */
    private static final File OPTIONAL_TEST_FILE = null;


    private static SimulationConfig config;
    private static Graph graph;

    private static SimulationConfig config() {
        SimulationConfig config = new SimulationConfig();
        config.crossingLogic.drivingOnTheRight = true;

        return config;
    }

    @BeforeClass
    public static void initialize() throws Exception {
        File map;

        //noinspection ConstantConditions
        if (OPTIONAL_TEST_FILE == null)
            map = new PackagedResource(GraphConsistencyTest.class, "/preprocessing/graph/map.osm").asTemporaryFile();
        else
            map = OPTIONAL_TEST_FILE;

        config = config();
        if (map.getName().endsWith(".osm")) {
            graph = DefaultParserConfig.get(config).build().parse(map, new MapProperties(true)).streetgraph;
        } else {
            ExchangeFormatSerializer serializer = ExchangeFormatSerializer.create();
            ExchangeFormat exfmt = ExchangeFormat.getDefault();

            exfmt.getConfig().set(QuadTreeTiledMapSegmentExtractor.Config.getDefault(
                    new QuadTreeTilingScheme(new MercatorProjection()), 12));

            exfmt.getConfig().set(new StreetGraphExtractor.Config(config));
            ExchangeFormat.Manipulator xmp = exfmt.manipulator(serializer.read(map));
            try {
                QuadTreeTiledMapSegment unused = xmp.extract(QuadTreeTiledMapSegment.class);
            } catch (NotAvailableException e) {     // thrown when no TileGrid available
                MapSegment unused = xmp.extract(MapSegment.class);
            }
            graph = xmp.extract(StreetGraph.class);
        }
    }


    @Test
    public void testLaneConnectorValidity() {
        for (DirectedEdge edge : graph.getEdges()) {
            ArrayList<LaneConnector> leaving = getLeavingConnectors(edge);

            for (int i = 0; i < leaving.size(); i++) {
                LaneConnector a = leaving.get(i);

                for (int j = i + 1; j < leaving.size(); j++) {
                    LaneConnector b = leaving.get(j);
                    assertLaneConnectorsDoNotIntersect(a, b, config.crossingLogic.drivingOnTheRight);
                }
            }
        }
    }

    private static void assertLaneConnectorsDoNotIntersect(LaneConnector a, LaneConnector b, boolean drivingOnTheRight) {
        assertEquals(a.from.getEdge(), b.from.getEdge());

        boolean aIsUturn = a.from.getEdge().getEntity() == a.to.getEdge().getEntity();
        boolean bIsUturn = b.from.getEdge().getEntity() == b.to.getEdge().getEntity();

        if (aIsUturn || bIsUturn) {
            if (aIsUturn && bIsUturn) {
                assertFalse(a.from.getIndex() < b.from.getIndex() && a.to.getIndex() > b.to.getIndex());
                assertFalse(a.from.getIndex() > b.from.getIndex() && a.to.getIndex() < b.to.getIndex());
            } else if (aIsUturn) {
                assertFalse(a.from.getIndex() < b.from.getIndex());
            } else {
                assertFalse(b.from.getIndex() < a.from.getIndex());
            }

        } else {
            assertFalse(a.to == b.to);

            if (a.from == b.from)
                return;

            if (a.to.getEdge() == b.to.getEdge()) {
                assertFalse(a.from.getIndex() < b.from.getIndex() && a.to.getIndex() > b.to.getIndex());
                assertFalse(a.from.getIndex() > b.from.getIndex() && a.to.getIndex() < b.to.getIndex());
                return;
            }

            if (drivingOnTheRight) {
                assertFalse(a.from.getIndex() < b.from.getIndex() && a.angle > b.angle);
                assertFalse(a.from.getIndex() > b.from.getIndex() && a.angle < b.angle);
            } else {
                assertFalse(a.from.getIndex() < b.from.getIndex() && a.angle < b.angle);
                assertFalse(a.from.getIndex() > b.from.getIndex() && a.angle > b.angle);
            }
        }
    }

    /**
     * Calculates the counter-clockwise angle between a and b in radian.
     *
     * @param a the first vector
     * @param b the second vector
     * @return the counter-clockwise angle between {@code a} and {@code b}
     */
    private static double angle(Vec2d a, Vec2d b) {
        Vec2d na = Vec2d.normalize(a);
        Vec2d nb = Vec2d.normalize(b);

        double cross = na.cross(nb);
        if (cross == 0.0) {
            return Math.PI;
        } else {
            double inner = Math.acos(MathUtils.clamp(na.dot(nb), -1.0, 1.0));
            return cross > 0.0 ? inner : (2 * Math.PI - inner);
        }
    }

    private static ArrayList<LaneConnector> getLeavingConnectors(DirectedEdge edge) {
        ArrayList<LaneConnector> leaving = new ArrayList<>();

        for (Map.Entry<DirectedEdge.Lane, TreeMap<DirectedEdge, DirectedEdge.Lane>> connector
                : edge.getDestination().getConnectors().entrySet())
        {
            final DirectedEdge.Lane from = connector.getKey();
            final Vec2d fromVec = Vec2d.mul(from.getEdge().getDestinationDirection(), -1.0);

            if (from.getEdge() == edge) {
                for (DirectedEdge.Lane to : connector.getValue().values()) {
                    Vec2d toVec = to.getEdge().getOriginDirection();
                    leaving.add(new LaneConnector(from, to, angle(fromVec, toVec)));
                }
            }
        }

        return leaving;
    }

    private static class LaneConnector {
        DirectedEdge.Lane from;
        DirectedEdge.Lane to;
        double angle;

        LaneConnector(DirectedEdge.Lane from, DirectedEdge.Lane to, double angle) {
            this.from = from;
            this.to = to;
            this.angle = angle;
        }
    }
}
