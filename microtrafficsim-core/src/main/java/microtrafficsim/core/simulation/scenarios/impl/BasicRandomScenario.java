package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.astar.AStars;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.containers.VehicleContainer;
import microtrafficsim.core.simulation.scenarios.containers.impl.ConcurrentVehicleContainer;
import microtrafficsim.core.simulation.utils.RouteContainer;
import microtrafficsim.math.random.Seeded;
import microtrafficsim.math.random.distributions.impl.Random;

import java.util.function.Supplier;

/**
 * The type of scenario represented by this class chooses start and end nodes randomly out of node-collections.
 *
 * @author Dominic Parga Cacheiro
 */
public abstract class BasicRandomScenario extends BasicScenario implements Seeded {

    /* scout factory */
    private final Random random;
    private final float fastestWayProbability;
    private final ShortestPathAlgorithm<Node, DirectedEdge> fastestPathAlg;
    private final ShortestPathAlgorithm<Node, DirectedEdge> shortestPathAlg;

    protected BasicRandomScenario(long seed,
                                  SimulationConfig config,
                                  Graph graph) {
        this(new Random(seed), config, graph);
    }

    protected BasicRandomScenario(Random random,
                                  SimulationConfig config,
                                  Graph graph) {
        this(random, config, graph, new ConcurrentVehicleContainer());
    }

    protected BasicRandomScenario(long seed,
                                  SimulationConfig config,
                                  Graph graph,
                                  VehicleContainer vehicleContainer) {
        this(new Random(seed), config, graph, vehicleContainer);
    }

    protected BasicRandomScenario(Random random,
                                  SimulationConfig config,
                                  Graph graph,
                                  VehicleContainer vehicleContainer) {
        super(config, graph, vehicleContainer);

        this.random = random;
        fastestWayProbability = 0.7f;

        /* scout factory */
        fastestPathAlg = AStars.fastestPathAStar(config.metersPerCell, config.globalMaxVelocity);
        shortestPathAlg = AStars.shortestPathAStar(config.metersPerCell);
    }

    public abstract void redefineMetaRoutes();

    public abstract void redefineMetaRoutes(RouteContainer newRoutes);

    /*
    |==============|
    | (i) Scenario |
    |==============|
    */
    @Override
    public Supplier<ShortestPathAlgorithm<Node, DirectedEdge>> getScoutFactory() {
        float nextFloat = random.nextFloat();
        return () -> (nextFloat < fastestWayProbability)
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
    public long getSeed() {
        return random.getSeed();
    }

    @Override
    public void setSeed(long seed) {
        random.setSeed(seed);
    }
}
