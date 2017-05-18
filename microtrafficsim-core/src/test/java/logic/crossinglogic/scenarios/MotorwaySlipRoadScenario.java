package logic.crossinglogic.scenarios;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.routes.MetaRoute;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.vehicles.driver.BasicDriver;
import microtrafficsim.core.logic.vehicles.driver.Driver;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.logic.vehicles.machines.impl.Car;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.builder.impl.VisVehicleFactory;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.impl.QueueScenarioSmall;
import microtrafficsim.core.simulation.utils.RouteContainer;
import microtrafficsim.core.simulation.utils.SortedRouteContainer;

import java.util.ArrayList;

/**
 * Validates the crossing logic in a scenario where a street crosses a motorway.
 *
 * @author Dominic Parga Cacheiro
 */
public class MotorwaySlipRoadScenario extends QueueScenarioSmall {
    private Node bottomMotorway, topMotorway, bottomRight;

    /**
     * Initializes the matrices (for routes and spawn delays). For this, it has to sort the few nodes to guarantee
     * determinism independent of complicated coordinate calculations.
     *
     * @see QueueScenarioSmall#QueueScenarioSmall(SimulationConfig, Graph, ScenarioBuilder)
     */
    public MotorwaySlipRoadScenario(SimulationConfig config, Graph graph, VisVehicleFactory visVehicleFactory) {
        super(config, graph, new VehicleScenarioBuilder(
                config.seed,
                (id, seed, scenario, metaRoute) -> {
                    Vehicle vehicle = new Car(id, 1, scenario.getConfig().visualization.style);
                    Driver driver = new BasicDriver(seed, 0, metaRoute.getSpawnDelay());
                    driver.setRoute(metaRoute.clone());
                    driver.setVehicle(vehicle);
                    vehicle.setDriver(driver);

                    vehicle.addStateListener(scenario.getVehicleContainer());

                    return vehicle;
                },
                visVehicleFactory
        ));


        /* get nodes sorted by lon */
        ArrayList<Node> sortedNodes = new ArrayList<>(getGraph().getNodes());
        sortedNodes.sort((n1, n2) -> n1.getCoordinate().lon > n2.getCoordinate().lon ? 1 : -1);

        bottomMotorway = sortedNodes.get(0);
        // interception = sortedNodes.get(1);
        topMotorway = sortedNodes.get(2);
        bottomRight = sortedNodes.get(3);


        /* setup scenario matrices */
        RouteContainer routes = new SortedRouteContainer();
        routes.add(new MetaRoute(bottomMotorway, topMotorway, 0));
        routes.add(new MetaRoute(bottomMotorway, topMotorway, 0));
        routes.add(new MetaRoute(bottomRight,    topMotorway, 9));
        routes.add(new MetaRoute(bottomRight,    topMotorway, 9));
        addSubScenario(routes);
    }


    /**
     * @param config
     * @return the given config updated; just for practical purpose
     */
    public static SimulationConfig setupConfig(SimulationConfig config) {
        QueueScenarioSmall.setupConfig(config);

        config.maxVehicleCount                            = 3;
        config.crossingLogic.friendlyStandingInJamEnabled = false;

        return config;
    }
}
