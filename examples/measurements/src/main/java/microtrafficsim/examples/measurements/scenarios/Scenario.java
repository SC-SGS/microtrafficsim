package microtrafficsim.examples.measurements.scenarios;

import microtrafficsim.core.frameworks.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.frameworks.shortestpath.astar.impl.FastestWayAStar;
import microtrafficsim.core.frameworks.shortestpath.astar.impl.LinearDistanceAStar;
import microtrafficsim.core.frameworks.vehicle.IVisualizationVehicle;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.simulation.scenarios.EndOfTheWorldScenario;
import microtrafficsim.utils.id.ConcurrentLongIDGenerator;

import java.util.Random;
import java.util.function.Supplier;

/**
 * Created by Dominic on 12.03.16.
 */
public class Scenario extends EndOfTheWorldScenario {

    public static class Config extends EndOfTheWorldScenario.Config {

        {
            // super attributes
            longIDGenerator = new ConcurrentLongIDGenerator();
            msPerTimeStep = 5;
            maxVehicleCount = 10000;
            // own attributes
            ageForPause = -1;
        }
    }

    /**
     * Standard constructor.
     *
     * @param config         The used config file for this scenarios.
     * @param graph          The streetgraph used for this scenarios.
     * @param vehicleFactory This creates vehicles.
     */
    public Scenario(Config config, StreetGraph graph, Supplier<IVisualizationVehicle> vehicleFactory) {
        super(config, graph, vehicleFactory);
    }

    @Override
    public void didRunOneStep() {
        super.didRunOneStep();
    }

    @Override
    protected Supplier<ShortestPathAlgorithm> createScoutFactory() {
        return new Supplier<ShortestPathAlgorithm>() {

            private Random random = new Random(config.seed);

            @Override
            public ShortestPathAlgorithm get() {
                if (random.nextFloat() < 70f) {
                    return new FastestWayAStar(config.metersPerCell);
                } else {
                    return new LinearDistanceAStar(config.metersPerCell);
                }
            }
        };
    }
}