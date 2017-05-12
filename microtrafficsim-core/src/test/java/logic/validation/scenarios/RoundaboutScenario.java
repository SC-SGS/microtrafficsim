package logic.validation.scenarios;

import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.impl.QueueScenarioSmall;
import microtrafficsim.core.simulation.utils.RouteContainer;
import microtrafficsim.core.simulation.utils.SortedRouteContainer;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public class RoundaboutScenario extends QueueScenarioSmall {
    /**
     * @see QueueScenarioSmall#QueueScenarioSmall(SimulationConfig, Graph, Supplier)
     */
    public RoundaboutScenario(SimulationConfig config,
                              Graph graph,
                              Supplier<VisualizationVehicleEntity> visVehicleFactory) {
        super(config, graph, visVehicleFactory);
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
        //      System.out.println("Node(" + n.id + ").coord = " + n.getCoordinate());
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


        /* TOP_RIGHT */
        routeContainer = new SortedRouteContainer();
        routeContainer.add(topRight, right, 0);
        routeContainer.add(topLeft, left, 13);
        addSubScenario(routeContainer);


        /* TOP_LEFT */
        routeContainer = new SortedRouteContainer();
        routeContainer.add(topLeft, topRight, 5);
        routeContainer.add(bottom, left, 0);
        addSubScenario(routeContainer);


        /* BOTTOM */
        routeContainer = new SortedRouteContainer();
        routeContainer.add(bottom, left, 0);
        routeContainer.add(right, topRight, 5);
        addSubScenario(routeContainer);


        /* RIGHT */
        // maybe useless, because checked crossroad is same as in TOP_RIGHT
        routeContainer = new SortedRouteContainer();
        routeContainer.add(right, bottom, 0);
        routeContainer.add(topLeft, left, 7);
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
