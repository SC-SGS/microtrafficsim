package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.astar.impl.FastestWayBidirectionalAStar;
import microtrafficsim.core.shortestpath.astar.impl.LinearDistanceBidirectionalAStar;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.math.random.Seeded;
import microtrafficsim.math.random.distributions.impl.Random;

import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public abstract class BasicRandomScenario extends BasicScenario implements Seeded {

    /* scout factory */
    private final Random random;
    private final float fastestWayProbability;
    private final ShortestPathAlgorithm fastestWayBidirectionalAStar, linearDistanceBidirectionalAStar;

    protected BasicRandomScenario(long seed,
                                  ScenarioConfig config,
                                  Graph graph,
                                  VehicleContainer vehicleContainer) {
        this(new Random(seed), config, graph, vehicleContainer);
    }

    protected BasicRandomScenario(Random random,
                                  ScenarioConfig config,
                                  Graph graph,
                                  VehicleContainer vehicleContainer) {
        super(config, graph, vehicleContainer);

        this.random = random;
        fastestWayProbability = 1.0f;

        /* scout factory */
        fastestWayBidirectionalAStar = new FastestWayBidirectionalAStar(config.metersPerCell, config.globalMaxVelocity);
        linearDistanceBidirectionalAStar = new LinearDistanceBidirectionalAStar(config.metersPerCell);
    }

    protected abstract void fillMatrix();

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
    public void reset() {
        super.reset();
        random.reset();
    }

    /*
    |============|
    | (i) Seeded |
    |============|
    */
    @Override
    public void setSeed(long seed) {
        random.setSeed(seed);
    }

    @Override
    public long getSeed() {
        return random.getSeed();
    }
}
