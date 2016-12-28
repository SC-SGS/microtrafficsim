package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.area.Area;
import microtrafficsim.core.map.area.RectangleArea;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.astar.impl.FastestWayBidirectionalAStar;
import microtrafficsim.core.shortestpath.astar.impl.LinearDistanceBidirectionalAStar;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.scenarios.containers.impl.ConcurrentVehicleContainer;
import microtrafficsim.math.HaversineDistanceCalculator;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Supplier;

/**
 * <p>
 * Defines one origin and one destination field around the whole graph using its latitude and longitude borders.
 *
 * <p>
 * {@link #getScoutFactory()} returns a bidirectional A-star algorithm.
 *
 * @author Dominic Parga Cacheiro
 */
public class EndOfTheWorldScenario extends BasicScenario {
    private static Logger logger = new EasyMarkableLogger(EndOfTheWorldScenario.class);

    private Random random;
    // matrix
    private final ArrayList<Node> nodes, leftNodes, bottomNodes, rightNodes, topNodes;
    // scout factory
    private final float fastestWayProbability = 1.0f;
    private final ShortestPathAlgorithm fastestWayBidirectionalAStar, linearDistanceBidirectionalAStar;

    /**
     * Default constructor calling {@link #EndOfTheWorldScenario(SimulationConfig, StreetGraph, VehicleContainer)} using
     * {@link ConcurrentVehicleContainer} as {@code VehicleContainer}
     */
    public EndOfTheWorldScenario(SimulationConfig config,
                                 StreetGraph graph,
                                 Supplier<VisualizationVehicleEntity> vehicleFactory) {
        this(config, graph, new ConcurrentVehicleContainer(vehicleFactory));
    }

    /**
     * After filling node lists for the graph's borders, this constructor calls {@link #next()} to initialize the
     * origin-destination-matrix.
     */
    public EndOfTheWorldScenario(SimulationConfig config, StreetGraph graph, VehicleContainer vehicleContainer) {
        super(config, graph, vehicleContainer);

        random = new Random(config.seedGenerator.next()); // TODO reset

        /*
        |===============|
        | scout factory |
        |===============|
        */
        fastestWayBidirectionalAStar = new FastestWayBidirectionalAStar(config.metersPerCell, config.globalMaxVelocity);
        linearDistanceBidirectionalAStar = new LinearDistanceBidirectionalAStar(config.metersPerCell);

        /*
        |=========================|
        | prepare building matrix |
        |=========================|
        */
        nodes       = new ArrayList<>(graph.getNodes());
        leftNodes   = new ArrayList<>();
        bottomNodes = new ArrayList<>();
        rightNodes  = new ArrayList<>();
        topNodes    = new ArrayList<>();
        // define areas for filling node lists
        float latLength   = Math.min(0.01f, 0.1f * (graph.maxLat - graph.minLat));
        float lonLength   = Math.min(0.01f, 0.1f * (graph.maxLon - graph.minLon));
        Area leftBorder   = new RectangleArea(graph.minLat, graph.minLon, graph.maxLat, graph.minLon + lonLength);
        Area bottomBorder = new RectangleArea(graph.minLat, graph.minLon, graph.minLat + latLength, graph.maxLon);
        Area rightBorder  = new RectangleArea(graph.minLat, graph.maxLon - lonLength, graph.maxLat, graph.maxLon);
        Area topBorder    = new RectangleArea(graph.maxLat - latLength, graph.minLon, graph.maxLat, graph.maxLon);

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

        /*
        |======|
        | init |
        |======|
        */
        next();
    }

    /**
     * <p>
     * Referring to {@code Random.nextAnything()}, this method calculates the next origin-destination-matrix based on
     * the seed of this class.
     *
     * <p>
     * Until enough vehicles (defined in {@link SimulationConfig}) are created, this method is doing this:<br>
     * &bull get random origin <br>
     * &bull calculate its position relative to the graph's center <br>
     * &bull get a random destination out of the border field (of nodes) being closest to the chosen origin
     * &bull increase the route count for the found origin-destination-pair
     */
    public void next() {
        // note: the directions used in this method's comments are referring to Europe (so the northern hemisphere)
        logger.info("BUILDING ODMatrix started");

        odMatrix.clear();
        // build matrix
        for (int i = 0; i < getConfig().maxVehicleCount; i++) {
            Node origin = getRandomNode(nodes);

            // get end node depending on start node's position
            StreetGraph graph      = getGraph();
            final float
                    minlat         = graph.minLat,
                    maxlat         = graph.maxLat,
                    minlon         = graph.minLon,
                    maxlon         = graph.maxLon;
            Coordinate
                    center         = new Coordinate((maxlat + minlat) / 2, (maxlon + minlon) / 2),
                    originCoord    = origin.getCoordinate();
            // set data relevant for distance calculation
            Coordinate
                    latProjection = new Coordinate(0, originCoord.lon),
                    lonProjection = new Coordinate(originCoord.lat, 0);
            ArrayList<Node> latNodes, lonNodes;

            if (center.lat - originCoord.lat > 0) {
                // origin is below from center
                latProjection.lat = minlat;
                latNodes          = bottomNodes;
            } else {
                // origin is over center
                latProjection.lat = maxlat;
                latNodes          = topNodes;
            }
            if (center.lon - originCoord.lon > 0) {
                // origin is left from center
                lonProjection.lon = minlon;
                lonNodes          = leftNodes;
            } else {
                // origin is right from center
                lonProjection.lon = maxlon;
                lonNodes          = rightNodes;
            }

            double latDistance = HaversineDistanceCalculator.getDistance(originCoord, latProjection);
            double lonDistance = HaversineDistanceCalculator.getDistance(originCoord, lonProjection);
            Node destination = latDistance > lonDistance ? getRandomNode(lonNodes) : getRandomNode(latNodes);
            odMatrix.inc(origin, destination);
        }

        logger.info("BUILDING ODMatrix finished");
    }

    private Node getRandomNode(List<Node> nodes) {
        int rdm = random.nextInt(nodes.size());
        return nodes.get(rdm);
    }

    /*
    |==============|
    | (i) Scenario |
    |==============|
    */
    @Override
    public Supplier<ShortestPathAlgorithm> getScoutFactory() {
        return () ->
                (random.nextFloat() < fastestWayProbability)
                        ? fastestWayBidirectionalAStar
                        : linearDistanceBidirectionalAStar;
    }
}