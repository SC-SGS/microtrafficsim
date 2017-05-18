package logic.crossinglogic.scenarios;

import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.vehicles.driver.BasicDriver;
import microtrafficsim.core.logic.vehicles.driver.Driver;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.logic.vehicles.machines.impl.Car;
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
public class RoundaboutScenario extends QueueScenarioSmall {
    /**
     * @see QueueScenarioSmall#QueueScenarioSmall(SimulationConfig, Graph)
     */
    public RoundaboutScenario(SimulationConfig config, Graph graph, VisVehicleFactory visVehicleFactory) {
        super(config, graph);
        setScenarioBuilder(new VehicleScenarioBuilder(
                config.seed,
                (id, seed, scenario, metaRoute) -> {
                    Vehicle vehicle = new Car(id, 1, scenario.getConfig().visualization.style);
                    Driver driver = new BasicDriver(seed, 0f, metaRoute.getSpawnDelay());
                    driver.setRoute(metaRoute.clone());
                    driver.setVehicle(vehicle);
                    vehicle.setDriver(driver);

                    vehicle.addStateListener(scenario.getVehicleContainer());

                    return vehicle;
                },
                visVehicleFactory
        ));
        init();
    }

    private void init() {
        /* get nodes sorted by lon */
        ArrayList<Node> sortedNodes = new ArrayList<>(getGraph().getNodes());
        sortedNodes.sort((n1, n2) -> {
            boolean sortByLon = false;
            //noinspection ConstantConditions
            if (sortByLon) {
                if (n1.getCoordinate().lon > n2.getCoordinate().lon)
                    return 1;
                else
                    return -1;
            } else {
                if (n1.getCoordinate().lat > n2.getCoordinate().lat)
                    return 1;
                else
                    return -1;
            }
        });

        //    for (Node n : sortedNodes) {
        //      System.out.println(n);
        //    }

        // node IDs in processing-file, sorted by lat ascending:
        // 243.679.734 bottom
        // 242.539.693 right
        //  27.281.851
        // 498.201.336
        // 260.334.121 left
        //  43.055.934
        //  27.281.850
        //  27.281.852
        // 271.387.772 top left
        // 254.636.138 top right

        Node bottom   = sortedNodes.get(0);
        Node right    = sortedNodes.get(1);
        Node left     = sortedNodes.get(4);
        Node topLeft  = sortedNodes.get(8);
        Node topRight = sortedNodes.get(9);


        /* setup scenario matrices */
        RouteContainer routeContainer;


        routeContainer = new SortedRouteContainer();
        routeContainer.add( topLeft,  topRight, 11);
        routeContainer.add( bottom,   left,     2);
        routeContainer.add( right,    left,     12);
        routeContainer.add( topRight, left,     0);
        addSubScenario(routeContainer);
    }


    /**
     * @param config
     * @return the given config updated; just for practical purpose
     */
    public static SimulationConfig setupConfig(SimulationConfig config) {
        QueueScenarioSmall.setupConfig(config);

        config.maxVehicleCount = 2;
        config.crossingLogic.friendlyStandingInJamEnabled = true;

        return config;
    }
}
