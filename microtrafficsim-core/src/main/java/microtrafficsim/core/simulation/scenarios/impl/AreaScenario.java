package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.routes.MetaRoute;
import microtrafficsim.core.logic.routes.Route;
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
import microtrafficsim.utils.collections.FastSortedArrayList;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;

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
    public void redefineMetaRoutes() {
        logger.info("DEFINING routes started");

        resetAndClearRoutes();
        areaNodeContainer.refillNodeLists(getGraph());

        for (int i = 0; i < getConfig().maxVehicleCount; i++) {
            boolean weightedUniformly = getConfig().scenario.nodesAreWeightedUniformly;

            MonitoredNode origin      = areaNodeContainer.getRdmOriginNode(weightedUniformly);
            MonitoredNode destination = areaNodeContainer.getRdmDestNode(weightedUniformly);

            Route route = new MetaRoute(origin.node, destination.node);
            route.setMonitored(origin.isMonitored || destination.isMonitored);
            routes.add(route);
        }

        logger.info("DEFINING routes finished");
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
        private final WheelOfFortune<MonitoredNode> rdmOriginSupplier;
        private final WheelOfFortune<MonitoredNode> rdmDestinationSupplier;
        private final HashMap<TypedPolygonArea, FastSortedArrayList<MonitoredNode>> areaToNode;
        private final Random nodeRandom;

        private boolean isDirty = false;

        public AreaNodeContainer(long seed) {
            nodeRandom = new Random(seed);

            originAreas            = new UnprojectedAreas();
            destinationAreas       = new UnprojectedAreas();
            rdmOriginSupplier      = new BasicWheelOfFortune<>(nodeRandom);
            rdmDestinationSupplier = new BasicWheelOfFortune<>(nodeRandom);

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
            areaToNode.computeIfAbsent(area, k -> new FastSortedArrayList<>());
            isDirty = true;
            return true;
        }

        public boolean addAreas(UnprojectedAreas areas) {
            boolean changes = false;

            for (TypedPolygonArea area : areas) {
                changes |= addArea(area);
            }

            return changes;
        }

        /**
         * Removes the given area WITHOUT the need of calling {@link #refillNodeLists(Graph)}
         *
         * @param area
         */
        public void removeArea(TypedPolygonArea area) {
            ArrayList<MonitoredNode> nodes = areaToNode.remove(area);
            if (nodes == null)
                return;

            if (area.getType() == Area.Type.ORIGIN)
                originAreas.remove(area);
            else
                destinationAreas.remove(area);
            isDirty = true;
        }


        public MonitoredNode getRdmOriginNode(boolean weightedUniformly) {
            assert !isDirty : "Random nodes could not be chosen due to the collection is not updated yet.";

            return rdmOriginSupplier.nextObject(weightedUniformly);
        }

        public MonitoredNode getRdmDestNode(boolean weightedUniformly) {
            assert !isDirty : "Random nodes could not be chosen due to the collection is not updated yet.";

            return rdmDestinationSupplier.nextObject(weightedUniformly);
        }

        public MonitoredNode getRdmNode(TypedPolygonArea area) {
            assert !isDirty : "Random nodes could not be chosen due to the collection is not updated yet.";

            ArrayList<MonitoredNode> nodes = areaToNode.get(area);
            if (nodes.size() == 0) {
                logger.warn("Empty area in class " + AreaScenario.class.getSimpleName());
                return null;
            }
            return nodes.get(nodeRandom.nextInt(nodes.size()));
        }


        public void refillNodeLists(Graph graph) {
            reset();

            isDirty |= !hasOriginAreas() || !hasDestinationAreas();
            if (isDirty) {
                clearNodeLists();

                if (!hasOriginAreas())
                    addArea(graph.total(Area.Type.ORIGIN));
                if (!hasDestinationAreas())
                    addArea(graph.total(Area.Type.DESTINATION));

                for (Node node : graph.getNodes()) {
                    MonitoredNode monitoredNode = new MonitoredNode(node, false);

                    // origin areas
                    int weight = 0;
                    boolean added = false;
                    for (TypedPolygonArea area : originAreas) {
                        if (area.contains((node))) {
                            weight++;
                            added = true;
                            monitoredNode.isMonitored |= area.isMonitored();
                            areaToNode.get(area).add(monitoredNode);
                        }
                    }
                    if (added)
                        rdmOriginSupplier.add(monitoredNode, weight);


                    // destination areas
                    weight = 0;
                    added = false;
                    for (TypedPolygonArea area : destinationAreas) {
                        if (area.contains((node))) {
                            weight++;
                            added = true;
                            monitoredNode.isMonitored |= area.isMonitored();
                            areaToNode.get(area).add(monitoredNode);
                        }
                    }
                    if (added) {
                        rdmDestinationSupplier.add(monitoredNode, weight);
                    }
                }

                isDirty = false;
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


    public static class MonitoredNode implements Comparable<MonitoredNode> {
        private Node node;
        private boolean isMonitored;

        public MonitoredNode(Node node, boolean isMonitored) {
            this.node = node;
            this.isMonitored = isMonitored;
        }

        public Node getNode() {
            return node;
        }

        public boolean isMonitored() {
            return isMonitored;
        }

        @Override
        public int compareTo(MonitoredNode o) {
            return node.compareTo(o.node);
        }
    }
}
