package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.astar.BidirectionalAStars;
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
    private final ShortestPathAlgorithm<Node, DirectedEdge> fastestPathAlg;
    private final ShortestPathAlgorithm<Node, DirectedEdge> shortestPathAlg;

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
        fastestWayProbability = 0;

        /* scout factory */
        fastestPathAlg = BidirectionalAStars.fastestPathAStar(config.metersPerCell, config.globalMaxVelocity);
        shortestPathAlg = BidirectionalAStars.shortestPathAStar(config.metersPerCell);
    }

    protected abstract void fillMatrix();

    /*
    |==============|
    | (i) Scenario |
    |==============|
    */
    @Override
    public Supplier<ShortestPathAlgorithm<Node, DirectedEdge>> getScoutFactory() {
        return () -> (random.nextFloat() < fastestWayProbability)
                        ? fastestPathAlg
                        : shortestPathAlg;
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
