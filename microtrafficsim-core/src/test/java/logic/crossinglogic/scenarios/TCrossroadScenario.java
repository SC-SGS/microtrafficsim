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
import microtrafficsim.core.simulation.utils.RouteContainer;
import microtrafficsim.core.simulation.utils.SortedRouteContainer;

import java.util.ArrayList;

/**
 * @author Dominic Parga Cacheiro
 */
public class TCrossroadScenario extends QueueScenarioSmall {
    /**
     * @see QueueScenarioSmall#QueueScenarioSmall(SimulationConfig, Graph, ScenarioBuilder)
     */
    public TCrossroadScenario(SimulationConfig config, Graph graph, VisVehicleFactory visVehicleFactory) {
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
        Node topLeft  = sortedNodes.get(0);
        Node bottom   = sortedNodes.get(2);
        Node topRight = sortedNodes.get(3);


        /* setup scenario matrices */
        RouteContainer routeContainer;


        /* PRIORITY_TO_THE_RIGHT */
        routeContainer = new SortedRouteContainer();
        routeContainer.add( topRight, topLeft, 0);
        routeContainer.add( bottom,   topLeft, 1);
        addSubScenario(routeContainer);


        /* NO_INTERCEPTION */
        routeContainer = new SortedRouteContainer();
        routeContainer.add( topRight, topLeft,  0);
        routeContainer.add( bottom,   topRight, 1);
        addSubScenario(routeContainer);


        /* DEADLOCK */
        routeContainer = new SortedRouteContainer();
        routeContainer.add( topRight, topLeft,  0);
        routeContainer.add( topLeft,  topRight, 1);
        routeContainer.add( bottom,   topLeft,  1);
        addSubScenario(routeContainer);


        /* LEFT TURNER MUST WAIT */
        routeContainer = new SortedRouteContainer();
        routeContainer.add( topRight, bottom,   0);
        routeContainer.add( topLeft,  topRight, 1);
        addSubScenario(routeContainer);
    }


    /**
     * @return the given config updated; just for practical purpose
     */
    public static SimulationConfig setupConfig(SimulationConfig config) {
        QueueScenarioSmall.setupConfig(config);

        config.maxVehicleCount                         = 3;
        config.crossingLogic.friendlyStandingInJamEnabled = false;

        return config;
    }
}
