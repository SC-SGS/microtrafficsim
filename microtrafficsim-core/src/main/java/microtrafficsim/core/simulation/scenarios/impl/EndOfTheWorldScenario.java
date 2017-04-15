package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.area.Area;
import microtrafficsim.core.map.area.polygons.RectangleArea;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
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
        nodes       = new ArrayList<>(graph.getNodes());
        leftNodes   = new ArrayList<>();
        bottomNodes = new ArrayList<>();
        rightNodes  = new ArrayList<>();
        topNodes    = new ArrayList<>();

        final Bounds bounds = graph.getBounds();

        // define areas for filling node lists
        double latLength   = Math.min(0.01f, 0.1f * (bounds.maxlat - bounds.minlat));
        double lonLength   = Math.min(0.01f, 0.1f * (bounds.maxlon - bounds.minlon));
        Area leftBorder   = new RectangleArea(
                bounds.minlat,
                bounds.minlon,
                bounds.maxlat,
                bounds.minlon + lonLength);
        Area bottomBorder = new RectangleArea(
                bounds.minlat,
                bounds.minlon,
                bounds.minlat + latLength,
                bounds.maxlon);
        Area rightBorder  = new RectangleArea(
                bounds.minlat,
                bounds.maxlon - lonLength,
                bounds.maxlat,
                bounds.maxlon);
        Area topBorder    = new RectangleArea(
                bounds.maxlat - latLength,
                bounds.minlon,
                bounds.maxlat,
                bounds.maxlon);

        // fill node lists
        for (Node node : nodes) {
            if (leftBorder.contains(node))
                leftNodes.add(node);

            if (bottomBorder.contains(node))
                bottomNodes.add(node);

            if (rightBorder.contains(node))
                rightNodes.add(node);

            if (topBorder.contains(node))
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
