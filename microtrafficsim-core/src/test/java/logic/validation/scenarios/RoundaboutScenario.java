package logic.validation.scenarios;

import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.impl.QueueScenarioSmall;
import microtrafficsim.core.simulation.utils.ODMatrix;
import microtrafficsim.core.simulation.utils.SparseODMatrix;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public class RoundaboutScenario extends QueueScenarioSmall {

    /**
     * @see QueueScenarioSmall#QueueScenarioSmall(SimulationConfig, Graph)
     */
    public RoundaboutScenario(SimulationConfig config,
                              Graph graph,
                              Supplier<VisualizationVehicleEntity> visVehicleFactory) {
        super(config, graph);
        init();
        setScenarioBuilder(new VehicleQueueScenarioBuilder(config.seed, visVehicleFactory));
    }

    /**
     * @param config
     * @return the given config updated; just for practical purpose
     */
    public static SimulationConfig setupConfig(SimulationConfig config) {

        QueueScenarioSmall.setupConfig(config);

        config.maxVehicleCount                         = 2;
        config.crossingLogic.friendlyStandingInJamEnabled = true;

        return config;
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
        ODMatrix odMatrix, spawnDelayMatrix;


        /* TOP_RIGHT */
        odMatrix = new SparseODMatrix();
        odMatrix.add(1, topRight, right);
        odMatrix.add(1, topLeft, left);

        spawnDelayMatrix = new SparseODMatrix();
        spawnDelayMatrix.add(0, topRight, right);
        spawnDelayMatrix.add(13, topLeft, left);

        addSubScenario(odMatrix, spawnDelayMatrix);


        /* TOP_LEFT */
        odMatrix = new SparseODMatrix();
        odMatrix.add(1, topLeft, topRight);
        odMatrix.add(1, bottom, left);

        spawnDelayMatrix = new SparseODMatrix();
        spawnDelayMatrix.add(5, topLeft, topRight);
        spawnDelayMatrix.add(0, bottom, left);

        addSubScenario(odMatrix, spawnDelayMatrix);


        /* BOTTOM */
        odMatrix = new SparseODMatrix();
        odMatrix.add(1, bottom, left);
        odMatrix.add(1, right, topRight);

        spawnDelayMatrix = new SparseODMatrix();
        spawnDelayMatrix.add(0, bottom, left);
        spawnDelayMatrix.add(5, right, topRight);

        addSubScenario(odMatrix, spawnDelayMatrix);


        /* RIGHT */
        // maybe useless, because checked crossroad is same as in TOP_RIGHT
        odMatrix = new SparseODMatrix();
        odMatrix.add(1, right, bottom);
        odMatrix.add(1, topLeft, left);

        spawnDelayMatrix = new SparseODMatrix();
        spawnDelayMatrix.add(0, right, bottom);
        spawnDelayMatrix.add(7, topLeft, left);

        addSubScenario(odMatrix, spawnDelayMatrix);
    }
}