package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.routes.MetaRoute;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.map.UnprojectedAreas;
import microtrafficsim.core.map.area.polygons.TypedPolygonArea;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.scenarios.containers.impl.ConcurrentVehicleContainer;
import microtrafficsim.core.simulation.utils.RouteContainer;
import microtrafficsim.core.simulation.utils.SortedRouteContainer;
import microtrafficsim.core.vis.scenario.areas.Area;
import microtrafficsim.math.random.Seeded;
import microtrafficsim.math.random.distributions.WheelOfFortune;
import microtrafficsim.math.random.distributions.impl.BasicWheelOfFortune;
import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.Resettable;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dominic Parga Cacheiro
 */
public class AreaScenario extends BasicRandomScenario {
    private static final Logger logger = new EasyMarkableLogger(AreaScenario.class);

    private final AreaNodeContainer areaNodeContainer;
    private final RouteContainer routes;

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
        areaNodeContainer = new AreaNodeContainer(random.getSeed());
        routes = new SortedRouteContainer();
    }


    @Override
    public RouteContainer getRoutes() {
        return routes;
    }

    public AreaNodeContainer getAreaNodeContainer() {
        return areaNodeContainer;
    }


    @Override
    public final void redefineMetaRoutes() {
        redefineMetaRoutes(null);
    }

    /**
     * @param newRoutes ignores {@link SimulationConfig#maxVehicleCount config.maxVehicleCount}
     */
    @Override
    public final void redefineMetaRoutes(RouteContainer newRoutes) {
        logger.info("DEFINING routes started");

        reset();
        areaNodeContainer.refillNodeLists(getGraph());

        routes.clear();
        if (newRoutes == null)
            defineRoutesAfterClearing();
        else
            routes.addAll(newRoutes);

        logger.info("DEFINING routes finished");
    }

    /**
     * Called in {@link #redefineMetaRoutes()} after all collections are cleared and all relevant attributes are
     * reset calling {@link #reset()}.
     */
    protected void defineRoutesAfterClearing() {
        for (int i = 0; i < getConfig().maxVehicleCount; i++) {
            boolean weightedUniformly = getConfig().scenario.nodesAreWeightedUniformly;

            Node origin      = areaNodeContainer.getRdmOriginNode(weightedUniformly);
            Node destination = areaNodeContainer.getRdmDestNode(weightedUniformly);

            routes.add(new MetaRoute(origin, destination));
        }
    }


    @Override
    public void reset() {
        super.reset();
        areaNodeContainer.reset();
    }

    @Override
    public void setSeed(long seed) {
        super.setSeed(seed);
        areaNodeContainer.setSeed(seed);
    }


    public static class AreaNodeContainer implements Seeded, Resettable {
        private final UnprojectedAreas originAreas;
        private final UnprojectedAreas destinationAreas;
        private final WheelOfFortune<Node> rdmOriginSupplier;
        private final WheelOfFortune<Node> rdmDestinationSupplier;
        private final Map<TypedPolygonArea, ArrayList<Node>> areaToNode;
        private final Random nodeRandom;

        private boolean isDirty = false;

        public AreaNodeContainer(long seed) {
            nodeRandom = new Random(seed);

            originAreas            = new UnprojectedAreas();
            destinationAreas       = new UnprojectedAreas();
            rdmOriginSupplier      = new BasicWheelOfFortune<>(seed);
            rdmDestinationSupplier = new BasicWheelOfFortune<>(seed);

            areaToNode = new HashMap<>();
        }


        public boolean hasOriginAreas() {
            return !originAreas.isEmpty();
        }

        public boolean hasDestinationAreas() {
            return !destinationAreas.isEmpty();
        }


        public UnprojectedAreas getAreas() {
            UnprojectedAreas areas = new UnprojectedAreas();
            areas.addAll(originAreas);
            areas.addAll(destinationAreas);
            return areas;
        }

        /**
         * Adds the given area WITHOUT refilling the respective node list. If you like to, call
         * {@link #refillNodeLists(Graph)}
         *
         * @param area
         */
        public boolean addArea(TypedPolygonArea area) {
            if (areaToNode.containsKey(area))
                return false;

            if (area.getType() == Area.Type.ORIGIN)
                originAreas.add(area);
            else
                destinationAreas.add(area);
            areaToNode.computeIfAbsent(area, k -> new ArrayList<>());
            isDirty = true;
            return true;
        }

        /**
         * Removes the given area WITHOUT the need of calling {@link #refillNodeLists(Graph)}
         *
         * @param area
         */
        public void removeArea(TypedPolygonArea area) {
            ArrayList<Node> nodes = areaToNode.remove(area);
            if (nodes == null)
                return;

            if (area.getType() == Area.Type.ORIGIN)
                originAreas.remove(area);
            else
                destinationAreas.remove(area);
            isDirty = true;
        }


        public Node getRdmOriginNode(boolean weightedUniformly) {
            return rdmOriginSupplier.nextObject(weightedUniformly);
        }

        public Node getRdmDestNode(boolean weightedUniformly) {
            return rdmDestinationSupplier.nextObject(weightedUniformly);
        }

        public Node getRdmNode(TypedPolygonArea area) {
            ArrayList<Node> nodes = areaToNode.get(area);
            return nodes.get(nodeRandom.nextInt(nodes.size()));
        }


        public void refillNodeLists(Graph graph) {
            reset();

            if (isDirty) {
                clearNodeLists();

                if (!hasOriginAreas())
                    addArea(graph.total(Area.Type.ORIGIN));
                if (!hasDestinationAreas())
                    addArea(graph.total(Area.Type.DESTINATION));

                for (Node node : graph.getNodes()) {
                    for (TypedPolygonArea area : originAreas) {
                        if (area.contains((node))) {
                            areaToNode.get(area).add(node);
                            rdmOriginSupplier.incWeight(node);
                        }
                    }
                    for (TypedPolygonArea area : destinationAreas) {
                        if (area.contains((node))) {
                            areaToNode.get(area).add(node);
                            rdmDestinationSupplier.incWeight(node);
                        }
                    }
                }
            }
        }

        private void clearNodeLists() {
            rdmOriginSupplier.clear();
            rdmDestinationSupplier.clear();
            areaToNode.values().forEach(ArrayList::clear);
        }


        @Override
        public void reset() {
            nodeRandom.reset();
            rdmOriginSupplier.reset();
            rdmDestinationSupplier.reset();
        }

        @Override
        public long getSeed() {
            return nodeRandom.getSeed();
        }

        @Override
        public void setSeed(long seed) {
            nodeRandom.setSeed(seed);
            rdmOriginSupplier.setSeed(seed);
            rdmDestinationSupplier.setSeed(seed);
        }
    }
}