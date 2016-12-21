package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.map.area.Area;
import microtrafficsim.core.map.area.RectangleArea;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.astar.impl.FastestWayBidirectionalAStar;
import microtrafficsim.core.shortestpath.astar.impl.LinearDistanceBidirectionalAStar;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 *
 *
 * @author Dominic Parga Cacheiro
 */
public class EndOfTheWorldScenario extends BasicScenario {

    private final Area startArea, destinationArea;
    // scout factory
    private Random random;
    private final float fastestWayProbability = 1.0f;
    private final ShortestPathAlgorithm fastestWayBidirectionalAStar, linearDistanceBidirectionalAStar;

    /**
     * Default constructor. Defines one origin and one destination field around the whole graph using its latitude
     * and longitude borders.
     */
    public EndOfTheWorldScenario(SimulationConfig config, StreetGraph graph, VehicleContainer vehicleContainer) {
        super(config, graph, vehicleContainer);

        startArea = new RectangleArea(graph.minLat, graph.minLon, graph.maxLat, graph.maxLon);
        destinationArea = new RectangleArea(graph.minLat, graph.minLon, graph.maxLat, graph.maxLon);

        // scout factory
        random = new Random(config.seedGenerator.next()); // TODO reset
        fastestWayBidirectionalAStar = new FastestWayBidirectionalAStar(config.metersPerCell, config.globalMaxVelocity);
        linearDistanceBidirectionalAStar = new LinearDistanceBidirectionalAStar(config.metersPerCell);
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

    @Override
    public Collection<Area> getOriginFields() {
        List<Area> list = new LinkedList<>();
        list.add(startArea);
        return list;
    }

    @Override
    public Collection<Area> getDestinationFields() {
        List<Area> list = new LinkedList<>();
        list.add(destinationArea);
        return list;
    }
}
