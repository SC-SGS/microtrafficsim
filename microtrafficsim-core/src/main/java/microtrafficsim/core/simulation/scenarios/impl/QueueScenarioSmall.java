package microtrafficsim.core.simulation.scenarios.impl;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.astar.BidirectionalAStars;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.utils.RouteContainer;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * This scenario defines different scenarios in a queue, which can be executed after each other. The scenarios are
 * getting prepared/calculated on the fly, so this class is made only for small scenarios due to runtime.
 *
 * @author Dominic Parga Cacheiro
 */
public class QueueScenarioSmall extends BasicScenario {
    private final ShortestPathAlgorithm<Node, DirectedEdge> scout;
    private ArrayList<RouteContainer> routeContainers;
    private int curIdx;
    private boolean isLooping;
    private ScenarioBuilder scenarioBuilder;


    /**
     * Calling this needs calling {@link #setScenarioBuilder(ScenarioBuilder)}
     */
    protected QueueScenarioSmall(SimulationConfig config, Graph graph) {
        super(config, graph);
        scout = BidirectionalAStars.shortestPathAStar(config.metersPerCell);
        routeContainers = new ArrayList<>();
        curIdx = -1;
        isLooping = false;

        setPrepared(true);
    }

    public void setScenarioBuilder(ScenarioBuilder scenarioBuilder) {
        this.scenarioBuilder = scenarioBuilder;
    }

    public static SimulationConfig setupConfig(SimulationConfig config) {
        config.metersPerCell           = 7.5f;
        config.seed                    = 1455374755807L;
        config.multiThreading.nThreads = 1;

        config.speedup                                 = 5;
        config.crossingLogic.drivingOnTheRight         = true;
        config.crossingLogic.edgePriorityEnabled       = true;
        config.crossingLogic.priorityToTheRightEnabled = true;
        config.crossingLogic.onlyOneVehicleEnabled     = false;

        return config;
    }


    public final void addSubScenario(RouteContainer routes) {
        routeContainers.add(routes);
    }

    @Override
    public void executeBeforeBuilding() {
        super.executeBeforeBuilding();

        curIdx = curIdx + 1;
        if (!isLooping && curIdx == routeContainers.size()) {
            curIdx = -1;
            return;
        }
        curIdx %= routeContainers.size();
    }


    @Override
    public void willDoOneStep(Simulation simulation) {
        if (getVehicleContainer().getVehicleCount() == 0) {
            boolean isPaused = simulation.isPaused();
            simulation.cancel();
            setPrepared(false);
            try {
                scenarioBuilder.prepare(this);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            simulation.setAndInitPreparedScenario(this);

            if (!isPaused)
                simulation.run();
        }
    }


    public boolean isLooping() {
        return isLooping;
    }

    public void setLooping(boolean isLooping) {
        this.isLooping = isLooping;
    }

    @Override
    public RouteContainer getRoutes() {
        return routeContainers.get(curIdx);
    }

    @Override
    public Supplier<ShortestPathAlgorithm<Node, DirectedEdge>> getScoutFactory() {
        return () -> scout;
    }
}
