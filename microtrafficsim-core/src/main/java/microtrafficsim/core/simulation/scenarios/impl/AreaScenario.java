package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.area.polygons.BasicPolygonArea;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.scenarios.containers.impl.ConcurrentVehicleContainer;
import microtrafficsim.core.vis.scenario.areas.Area;
import microtrafficsim.math.random.distributions.WheelOfFortune;
import microtrafficsim.math.random.distributions.impl.BasicWheelOfFortune;
import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Dominic Parga Cacheiro
 */
public abstract class AreaScenario extends BasicRandomScenario {

    private static Logger logger = new EasyMarkableLogger(AreaScenario.class);

    // matrix
    private final HashMap<ScenarioPolygonArea, ArrayList<Node>> nodes;
    private final WheelOfFortune<Node> wheelOfFortune;

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

        /* prepare building matrix */
        nodes = new HashMap<>();
        wheelOfFortune = new BasicWheelOfFortune<>(random);
    }

    public ArrayList<Node> get(ScenarioPolygonArea area) {
        return nodes.get(area);
    }

    public Set<ScenarioPolygonArea> getAreas() {
        return new HashSet<>(nodes.keySet());
    }

    /**
     * Adds the given area WITHOUT filling the repective node list.
     *
     * @param area
     */
    public void addArea(ScenarioPolygonArea area) {
        nodes.computeIfAbsent(area, k -> new ArrayList<>());
    }

    public void refillNodeLists() {
        nodes.values().forEach(ArrayList::clear);

        for (Node node : getGraph().getNodes()) {
            nodes.keySet().stream()
                    .filter(area -> area.contains(node))
                    .forEach(area -> {
                        nodes.get(area).add(node);
                        wheelOfFortune.incWeight(node);
                    });
        }
    }


    public class ScenarioPolygonArea extends BasicPolygonArea {

        public Area.Type type;

        public ScenarioPolygonArea(Coordinate[] coordinates, Area.Type type) {
            super(coordinates);
            this.type = type;
        }

        public Area.Type getType() {
            return type;
        }
    }
}