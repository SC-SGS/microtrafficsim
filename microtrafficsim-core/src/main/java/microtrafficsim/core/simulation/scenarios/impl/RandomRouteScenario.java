package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.scenarios.containers.impl.ConcurrentVehicleContainer;
import microtrafficsim.core.vis.scenario.areas.Area;
import microtrafficsim.math.random.distributions.impl.Random;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

/**
 * <p>
 * Defines one origin and one destination field around the whole graph using its latitude and longitude borders.
 *
 * <p>
 * {@link #getScoutFactory()} returns a bidirectional A-star algorithm.
 *
 * @author Dominic Parga Cacheiro
 */
public class RandomRouteScenario extends AreaScenario {

    private static Logger logger = new EasyMarkableLogger(RandomRouteScenario.class);

    public RandomRouteScenario(long seed,
                               SimulationConfig config,
                               Graph graph) {
        this(new Random(seed), config, graph);
    }

    public RandomRouteScenario(Random random,
                               SimulationConfig config,
                               Graph graph) {
        this(random, config, graph, new ConcurrentVehicleContainer());
    }

    public RandomRouteScenario(long seed,
                               SimulationConfig config,
                               Graph graph,
                               VehicleContainer vehicleContainer) {
        this(new Random(seed), config, graph, vehicleContainer);
    }

    public RandomRouteScenario(Random random,
                               SimulationConfig config,
                               Graph graph,
                               VehicleContainer vehicleContainer) {
        super(random, config, graph, vehicleContainer);


        /* add areas */
        addArea(getTotalGraph(Area.Type.ORIGIN));
        addArea(getTotalGraph(Area.Type.DESTINATION));


        refillNodeLists();
    }
}