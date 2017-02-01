package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.map.style.VehicleStyleSheet;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.astar.impl.FastestWayBidirectionalAStar;
import microtrafficsim.core.shortestpath.astar.impl.LinearDistanceBidirectionalAStar;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.scenarios.containers.impl.ConcurrentVehicleContainer;
import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import java.util.ArrayList;
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
public class RandomRouteScenario extends BasicRandomScenario {

    private static Logger logger = new EasyMarkableLogger(RandomRouteScenario.class);

    public RandomRouteScenario(long seed,
                               ScenarioConfig config,
                               StreetGraph graph) {
        this(new Random(seed), config, graph);
    }

    public RandomRouteScenario(Random random,
                               ScenarioConfig config,
                               StreetGraph graph) {
        this(random, config, graph, new ConcurrentVehicleContainer());
    }

    public RandomRouteScenario(long seed,
                               ScenarioConfig config,
                               StreetGraph graph,
                               VehicleContainer vehicleContainer) {
        this(new Random(seed), config, graph, vehicleContainer);
    }

    public RandomRouteScenario(Random random,
                               ScenarioConfig config,
                               StreetGraph graph,
                               VehicleContainer vehicleContainer) {
        super(random, config, graph, vehicleContainer);
        fillMatrix();
    }

    /**
     * <p>
     * Until enough vehicles (defined in {@link ScenarioConfig}) are created, this method is doing this:<br>
     * &bull get random origin <br>
     * &bull get random destination <br>
     * &bull increase the route count for the found origin-destination-pair
     */
    @Override
    protected void fillMatrix() {
        logger.info("BUILDING ODMatrix started");

        Random random = new Random(getSeed());
        ArrayList<Node> nodes = new ArrayList<>(getGraph().getNodes());
        odMatrix.clear();

        // TODO can the runtime be improved by mathematical magic?
        for (int i = 0; i < getConfig().maxVehicleCount; i++) {
            int rdmOrig = random.nextInt(nodes.size());
            int rdmDest = random.nextInt(nodes.size());
            odMatrix.inc(nodes.get(rdmOrig), nodes.get(rdmDest));
        }

        logger.info("BUILDING ODMatrix finished");
    }
}
