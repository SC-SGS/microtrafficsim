package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.area.polygons.BasicPolygonArea;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.scenarios.containers.impl.ConcurrentVehicleContainer;
import microtrafficsim.math.HaversineDistanceCalculator;
import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * <p>
 * Defines one origin and one destination field around the whole graph using its latitude and longitude borders.
 *
 * <p>
 * {@link #getScoutFactory()} returns a bidirectional A-star algorithm.
 *
 * @author Dominic Parga Cacheiro
 */
public class EndOfTheWorldScenario extends BasicRandomScenario {

    private static Logger logger = new EasyMarkableLogger(EndOfTheWorldScenario.class);

    // matrix
    private final ArrayList<Node> nodes, leftNodes, bottomNodes, rightNodes, topNodes;
    private final BasicPolygonArea originArea;
    private final BasicPolygonArea destinationAreaLeft;
    private final BasicPolygonArea destinationAreaBottom;
    private final BasicPolygonArea destinationAreaRight;
    private final BasicPolygonArea destinationAreaTop;

    public EndOfTheWorldScenario(long seed,
                                 SimulationConfig config,
                                 Graph graph) {
        this(new Random(seed), config, graph, new ConcurrentVehicleContainer());
    }

    public EndOfTheWorldScenario(Random random,
                                 SimulationConfig config,
                                 Graph graph) {
        this(random, config, graph, new ConcurrentVehicleContainer());
    }

    public EndOfTheWorldScenario(long seed,
                                 SimulationConfig config,
                                 Graph graph,
                                 VehicleContainer vehicleContainer) {
        this(new Random(seed), config, graph, vehicleContainer);
    }

    public EndOfTheWorldScenario(Random random,
                                 SimulationConfig config,
                                 Graph graph,
                                 VehicleContainer vehicleContainer) {
        super(random, config, graph, vehicleContainer);

        /* prepare building matrix */
        nodes       = new ArrayList<>();
        leftNodes   = new ArrayList<>();
        bottomNodes = new ArrayList<>();
        rightNodes  = new ArrayList<>();
        topNodes    = new ArrayList<>();


        /* helping variables */
        final Bounds bounds = graph.getBounds();

        final Coordinate bottomLeft = new Coordinate(   bounds.minlat, bounds.minlon);
        final Coordinate bottomRight = new Coordinate(  bounds.minlat, bounds.maxlon);
        final Coordinate topRight = new Coordinate(     bounds.maxlat, bounds.maxlon);
        final Coordinate topLeft = new Coordinate(      bounds.maxlat, bounds.minlon);

        double latLength = Math.min(0.01f, 0.1f * (bounds.maxlat - bounds.minlat));
        double lonLength = Math.min(0.01f, 0.1f * (bounds.maxlon - bounds.minlon));

        final Coordinate innerBottomLeft = new Coordinate(  bounds.minlat + latLength, bounds.minlon + lonLength);
        final Coordinate innerBottomRight = new Coordinate( bounds.minlat + latLength, bounds.maxlon - lonLength);
        final Coordinate innerTopRight = new Coordinate(    bounds.maxlat - latLength, bounds.maxlon - lonLength);
        final Coordinate innerTopLeft = new Coordinate(     bounds.maxlat - latLength, bounds.minlon + lonLength);


        /* define areas for filling node lists */
        originArea = new BasicPolygonArea(new Coordinate[] {
                bottomLeft,
                bottomRight,
                topRight,
                topLeft
        });
        destinationAreaLeft = new BasicPolygonArea(new Coordinate[] {
                bottomLeft,
                innerBottomLeft,
                innerTopLeft,
                topLeft
        });
        destinationAreaBottom = new BasicPolygonArea(new Coordinate[] {
                bottomLeft,
                bottomRight,
                innerBottomRight,
                innerBottomLeft
        });
        destinationAreaRight = new BasicPolygonArea(new Coordinate[] {
                innerBottomRight,
                bottomRight,
                topRight,
                innerTopRight
        });
        destinationAreaTop = new BasicPolygonArea(new Coordinate[] {
                innerTopLeft,
                innerTopRight,
                topRight,
                topLeft
        });

        // fill node lists
        for (Node node : graph.getNodes()) {
            if (originArea.contains(node))
                nodes.add(node);

            if (destinationAreaLeft.contains(node))
                leftNodes.add(node);

            if (destinationAreaBottom.contains(node))
                bottomNodes.add(node);

            if (destinationAreaRight.contains(node))
                rightNodes.add(node);

            if (destinationAreaTop.contains(node))
                topNodes.add(node);
        }

        /* init */
        fillMatrix();
    }

    /**
     * <p>
     * Until enough vehicles (defined in {@link SimulationConfig}) are created, this method is doing this:<br>
     * &bull get random origin <br>
     * &bull calculate its position relative to the graph's center <br>
     * &bull get a random destination out of the border field (of nodes) being closest to the chosen origin
     * &bull increase the route count for the found origin-destination-pair
     */
    @Override
    protected void fillMatrix() {
        // note: the directions used in this method's comments are referring to Europe (so the northern hemisphere)
        logger.info("BUILDING ODMatrix started");

        Random random = new Random(getSeed());
        Function<List<Node>, Node> getRandomNode = nodes -> nodes.get(random.nextInt(nodes.size()));

        odMatrix.clear();
        // build matrix
        for (int i = 0; i < getConfig().maxVehicleCount; i++) {
            Node origin = getRandomNode.apply(nodes);

            // get end node depending on start node's position
            Graph graph = getGraph();
            final Bounds bounds = graph.getBounds();

            Coordinate center = new Coordinate((bounds.maxlat + bounds.minlat) / 2, (bounds.maxlon + bounds.minlon) / 2);
            Coordinate originCoord = origin.getCoordinate();

            // set data relevant for distance calculation
            Coordinate latProjection = new Coordinate(0, originCoord.lon);
            Coordinate lonProjection = new Coordinate(originCoord.lat, 0);

            ArrayList<Node> latNodes, lonNodes;

            if (center.lat - originCoord.lat > 0) {
                // origin is below from center
                latProjection.lat = bounds.minlat;
                latNodes          = bottomNodes;
            } else {
                // origin is over center
                latProjection.lat = bounds.maxlat;
                latNodes          = topNodes;
            }
            if (center.lon - originCoord.lon > 0) {
                // origin is left from center
                lonProjection.lon = bounds.minlon;
                lonNodes          = leftNodes;
            } else {
                // origin is right from center
                lonProjection.lon = bounds.maxlon;
                lonNodes          = rightNodes;
            }

            double latDistance = HaversineDistanceCalculator.getDistance(originCoord, latProjection);
            double lonDistance = HaversineDistanceCalculator.getDistance(originCoord, lonProjection);
            Node destination = latDistance > lonDistance ? getRandomNode.apply(lonNodes) : getRandomNode.apply(latNodes);
            odMatrix.inc(origin, destination);
        }

        logger.info("BUILDING ODMatrix finished");
    }
}