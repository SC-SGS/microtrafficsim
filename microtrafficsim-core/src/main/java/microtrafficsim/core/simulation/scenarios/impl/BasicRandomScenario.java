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
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import java.util.function.Supplier;

/**
 * The type of scenario represented by this class chooses start and end nodes randomly out of node-collections.
 *
 * @author Dominic Parga Cacheiro
 */
public abstract class BasicRandomScenario extends BasicScenario implements Seeded {
    public static final Logger logger = new EasyMarkableLogger(BasicRandomScenario.class);

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


    /**
     * <p>
     * Let n be {@link SimulationConfig#maxVehicleCount config.maxVehicleCount} and <br>
     * let m be {@link RouteContainer#size() routeLexicon.size()}.
     *
     * <p>
     * If n >= m: All routes are added and (n-m) are chosen randomly <br>
     * If n < m: n routes are chosen randomly<br>
     * The randomness depends on the given {@link RouteContainer#getRdm(Random)}
     */
    public void setRoutes(RouteContainer routeLexicon) {
        logger.info("SETTING routes started");

        reset();

        Random random = new Random(getSeed());
        getRoutes().clear();
        if (getConfig().maxVehicleCount >= routeLexicon.size())
            getRoutes().addAll(routeLexicon);
        while (routeLexicon.size() < getConfig().maxVehicleCount)
            getRoutes().add(routeLexicon.getRdm(random));

        logger.info("SETTING routes finished");
    }

    public void chooseRdmFromRoutes(RouteContainer routeLexicon) {
        logger.info("CHOOSING RANDOM routes started");

        reset();

        Random random = new Random(getSeed());
        getRoutes().clear();
        for (int i = 0; i < getConfig().maxVehicleCount; i++)
            getRoutes().add(routeLexicon.getRdm(random));

        logger.info("CHOOSING RANDOM routes finished");
    }

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
