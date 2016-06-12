package microtrafficsim.ui.scenarios;

import microtrafficsim.core.frameworks.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.frameworks.shortestpath.astar.impl.FastestWayAStar;
import microtrafficsim.core.frameworks.shortestpath.astar.impl.LinearDistanceAStar;
import microtrafficsim.core.frameworks.vehicle.IVisualizationVehicle;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.EndOfTheWorldScenario;

import java.util.Random;
import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public class Scenario extends EndOfTheWorldScenario {

    /**
     * Standard constructor.
     *
     * @param config         The used config file for this scenarios.
     * @param graph          The streetgraph used for this scenarios.
     * @param vehicleFactory This creates vehicles.
     */
    public Scenario(SimulationConfig config, StreetGraph graph, Supplier<IVisualizationVehicle> vehicleFactory) {
        super(config, graph, vehicleFactory);
    }

    @Override
    protected Supplier<ShortestPathAlgorithm> createScoutFactory() {
        return new Supplier<ShortestPathAlgorithm>() {

            private Random random = new Random(config.seed);

            @Override
            public ShortestPathAlgorithm get() {
                if (random.nextFloat() < 1.0f) {
                    return new FastestWayAStar(config.metersPerCell);
                } else {
                    return new LinearDistanceAStar(config.metersPerCell);
                }
            }
        };
    }
}
