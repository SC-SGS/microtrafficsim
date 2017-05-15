package logic.crossinglogic.scenarios;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.simulation.builder.LogicVehicleFactory;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.builder.impl.VisVehicleFactory;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.impl.QueueScenarioSmall;

import java.util.ArrayList;

/**
 * @author Dominic Parga Cacheiro
 */
public class MultilaneScenario extends QueueScenarioSmall {
    /**
     * @see QueueScenarioSmall#QueueScenarioSmall(SimulationConfig, Graph, ScenarioBuilder)
     */
    public MultilaneScenario(SimulationConfig config,
                             Graph graph,
                             VisVehicleFactory visVehicleFactory) {
        super(config, graph, new VehicleScenarioBuilder(
                config.seed,
                (id, seed, scenario, metaRoute) -> {
                    Vehicle vehicle = LogicVehicleFactory.defaultCreation(id, seed, scenario, metaRoute);
                    vehicle.getDriver().setDawdleFactor(0);
                    return vehicle;
                },
                visVehicleFactory
        ));
        init();
    }


    private void init() {
        /* get nodes sorted by lon */
        ArrayList<Node> sortedNodes = new ArrayList<>(getGraph().getNodes());
        sortedNodes.sort((n1, n2) -> n1.getCoordinate().lon > n2.getCoordinate().lon ? 1 : -1);
        // todo
    }


    /**
     * @param config
     * @return the given config updated; just for practical purpose
     */
    public static SimulationConfig setupConfig(SimulationConfig config) {
        config.maxVehicleCount                            = 100;
        config.speedup                                    = 5;
        config.seed                                       = 42;
        config.crossingLogic.drivingOnTheRight            = true;
        config.crossingLogic.edgePriorityEnabled          = true;
        config.crossingLogic.priorityToTheRightEnabled    = true;
        config.crossingLogic.friendlyStandingInJamEnabled = true;
        config.crossingLogic.onlyOneVehicleEnabled        = false;

        return config;
    }
}
