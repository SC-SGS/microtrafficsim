package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.map.area.Area;
import microtrafficsim.core.map.area.RectangleArea;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.astar.impl.FastestWayBidirectionalAStar;
import microtrafficsim.core.shortestpath.astar.impl.LinearDistanceBidirectionalAStar;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.utils.ODMatrix;
import microtrafficsim.core.simulation.utils.SparseODMatrix;

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
public class RandomRouteScenario extends BasicScenario {

    private final Area startArea, destinationArea;
    // scout factory
    private Random random;
    private final float fastestWayProbability = 1.0f;
    private final ShortestPathAlgorithm fastestWayBidirectionalAStar, linearDistanceBidirectionalAStar;

    /**
     * Default constructor. Defines one origin and one destination field around the whole graph using its latitude
     * and longitude borders.
     */
    public RandomRouteScenario(SimulationConfig config, StreetGraph graph, VehicleContainer vehicleContainer) {
        super(config, graph, vehicleContainer);

        startArea = new RectangleArea(graph.minLat, graph.minLon, graph.maxLat, graph.maxLon);
        destinationArea = new RectangleArea(graph.minLat, graph.minLon, graph.maxLat, graph.maxLon);

        // scout factory
        random = new Random(config.seedGenerator.next()); // TODO reset
        fastestWayBidirectionalAStar = new FastestWayBidirectionalAStar(config.metersPerCell, config.globalMaxVelocity);
        linearDistanceBidirectionalAStar = new LinearDistanceBidirectionalAStar(config.metersPerCell);
    }

    public void next() {
        logger.info("BUILDING ODMatrix started");

        ArrayList<Node>
                origins = new ArrayList<>(),
                destinations = new ArrayList<>();

        /*
        |===================================================================================|
        | for each graph node, check its location relative to the origin/destination fields |
        |===================================================================================|
        */
        Iterator<Node> nodes = getGraph().getNodeIterator();
        while (nodes.hasNext()) {
            Node node = nodes.next();

            // for each node being in an origin field => add it
            for (Area area : getOriginFields())
                if (area.contains(node)) {
                    origins.add(node);
                    break;
                }

            // for each node being in a destination field => add it
            for (Area area : getDestinationFields())
                if (area.contains(node)) {
                    destinations.add(node);
                    break;
                }
        }

        /*
        |==============|
        | build matrix |
        |==============|
        */
        ODMatrix odmatrix = new SparseODMatrix();
        for (int i = 0; i < getConfig().maxVehicleCount; i++) {
            int rdmOrig = random.nextInt(origins.size());
            int rdmDest = random.nextInt(destinations.size());
            odmatrix.inc(origins.get(rdmOrig), destinations.get(rdmDest));
        }

        /*
        |==========================|
        | finish creating ODMatrix |
        |==========================|
        */
        setODMatrix(odmatrix);
        logger.info("BUILDING ODMatrix finished");
    }

    /*
    |==============|
    | (i) Scenario |
    |==============|
    */
    @Override
    public void setODMatrix(ODMatrix matrix) {

    }

    @Override
    public ODMatrix getODMatrix() {
        return null;
    }

    @Override
    public Supplier<ShortestPathAlgorithm> getScoutFactory() {
        return () ->
                (random.nextFloat() < fastestWayProbability)
                ? fastestWayBidirectionalAStar
                : linearDistanceBidirectionalAStar;
    }
}
