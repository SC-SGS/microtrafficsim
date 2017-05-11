package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.UnprojectedAreas;
import microtrafficsim.core.map.area.polygons.TypedPolygonArea;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.scenarios.containers.impl.ConcurrentVehicleContainer;
import microtrafficsim.core.vis.scenario.areas.Area;
import microtrafficsim.math.random.distributions.WheelOfFortune;
import microtrafficsim.math.random.distributions.impl.BasicWheelOfFortune;
import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Function;

/**
 * @author Dominic Parga Cacheiro
 */
public class AreaScenario extends BasicRandomScenario {

    private static Logger logger = new EasyMarkableLogger(AreaScenario.class);

    // matrix
    private final Map<TypedPolygonArea, ArrayList<Node>> originNodes;
    private final Map<TypedPolygonArea, ArrayList<Node>> destinationNodes;
    private final WheelOfFortune<Node> randomOriginSupplier;
    private final WheelOfFortune<Node> randomDestinationSupplier;
    private final Random nodeRandom;

    public AreaScenario(long seed,
                        SimulationConfig config,
                        Graph graph) {
        this(new Random(seed), config, graph, new ConcurrentVehicleContainer());
    }

    public AreaScenario(Random random,
                        SimulationConfig config,
                        Graph graph) {
        this(random, config, graph, new ConcurrentVehicleContainer());
    }

    public AreaScenario(long seed,
                        SimulationConfig config,
                        Graph graph,
                        VehicleContainer vehicleContainer) {
        this(new Random(seed), config, graph, vehicleContainer);
    }

    public AreaScenario(Random random,
                        SimulationConfig config,
                        Graph graph,
                        VehicleContainer vehicleContainer) {
        super(random, config, graph, vehicleContainer);
        nodeRandom = new Random(random.getSeed());

        /* prepare building matrix */
        originNodes = new HashMap<>();
        destinationNodes = new HashMap<>();
        randomOriginSupplier = new BasicWheelOfFortune<>(random);
        randomDestinationSupplier = new BasicWheelOfFortune<>(random);
    }


    protected void resetRandomNodeSupplier() {
        nodeRandom.reset();
        randomOriginSupplier.reset();
        randomDestinationSupplier.reset();
    }

    protected Node getRandomOriginNode() {
        return randomOriginSupplier.nextObject();
    }

    protected Node getRandomDestinationNode() {
        return randomDestinationSupplier.nextObject();
    }

    protected Node getRandomNode(TypedPolygonArea area) {
        ArrayList<Node> nodes = area.getType() == Area.Type.ORIGIN
                ? originNodes.get(area)
                : destinationNodes.get(area);
        return nodes.get(nodeRandom.nextInt(nodes.size()));
    }

    public TypedPolygonArea getTotalGraph(Area.Type type) {
        final Bounds bounds = getGraph().getBounds();

        final Coordinate bottomLeft = new Coordinate( bounds.minlat, bounds.minlon);
        final Coordinate bottomRight = new Coordinate(bounds.minlat, bounds.maxlon);
        final Coordinate topRight = new Coordinate(   bounds.maxlat, bounds.maxlon);
        final Coordinate topLeft = new Coordinate(    bounds.maxlat, bounds.minlon);


        /* add areas */
        return new TypedPolygonArea(new Coordinate[] {
                bottomLeft,
                bottomRight,
                topRight,
                topLeft
        }, type);
    }

    public UnprojectedAreas getAreas() {
        UnprojectedAreas areas = new UnprojectedAreas();
        areas.addAll(originNodes.keySet());
        areas.addAll(destinationNodes.keySet());
        return areas;
    }


    /**
     * Adds the given area WITHOUT refilling the respective node list. If you like to, call {@link #refillNodeLists()}
     *
     * @param area
     */
    public void addArea(TypedPolygonArea area) {
        if (area.getType() == Area.Type.ORIGIN)
            originNodes.computeIfAbsent(area, k -> new ArrayList<>());
        else
            destinationNodes.computeIfAbsent(area, k -> new ArrayList<>());
    }

    /**
     * Removes the given area WITHOUT the need of calling {@link #refillNodeLists()}
     *
     * @param area
     */
    public void removeArea(TypedPolygonArea area) {
        if (area.getType() == Area.Type.ORIGIN)
            originNodes.remove(area).forEach(randomOriginSupplier::decWeight);
        else
            destinationNodes.remove(area).forEach(randomDestinationSupplier::decWeight);
    }


    public void refillNodeLists() {
        originNodes.values().forEach(ArrayList::clear);
        destinationNodes.values().forEach(ArrayList::clear);

        if (originNodes.isEmpty())
            originNodes.put(getTotalGraph(Area.Type.ORIGIN), new ArrayList<>());
        if (destinationNodes.isEmpty())
            destinationNodes.put(getTotalGraph(Area.Type.DESTINATION), new ArrayList<>());

        for (Node node : getGraph().getNodes()) {
            for (TypedPolygonArea area : originNodes.keySet()) {
                if (area.contains((node))) {
                    originNodes.get(area).add(node);
                    randomOriginSupplier.incWeight(node);
                }
            }
            for (TypedPolygonArea area : destinationNodes.keySet()) {
                if (area.contains((node))) {
                    destinationNodes.get(area).add(node);
                    randomDestinationSupplier.incWeight(node);
                }
            }
        }

        fillMatrix();
    }


    @Override
    protected void fillMatrix() {
        logger.info("BUILDING ODMatrix started");

        randomOriginSupplier.reset();
        randomDestinationSupplier.reset();
        odMatrix.clear();


        for (int i = 0; i < getConfig().maxVehicleCount; i++) {
            boolean weightedUniformly = getConfig().scenario.nodesAreWeightedUniformly;
            Node origin = randomOriginSupplier.nextObject(weightedUniformly);
            Node destination = randomDestinationSupplier.nextObject(weightedUniformly);
            odMatrix.inc(origin, destination);
        }

        logger.info("BUILDING ODMatrix finished");
    }
}