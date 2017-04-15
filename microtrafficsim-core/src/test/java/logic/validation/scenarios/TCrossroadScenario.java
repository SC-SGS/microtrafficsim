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
public class TCrossroadScenario extends QueueScenarioSmall {

    /**
     * @see QueueScenarioSmall#QueueScenarioSmall(SimulationConfig, Graph)
     */
    public TCrossroadScenario(SimulationConfig config,
                              Graph graph,
                              Supplier<VisualizationVehicleEntity> visVehicleFactory) {
        super(config, graph);
        init();
        setScenarioBuilder(new VehicleQueueScenarioBuilder(config.seed, visVehicleFactory, 1));
    }

    /**
     * @param config
     * @return the given config updated; just for practical purpose
     */
    public static SimulationConfig setupConfig(SimulationConfig config) {

        QueueScenarioSmall.setupConfig(config);

        config.maxVehicleCount                         = 3;
        config.crossingLogic.friendlyStandingInJamEnabled = false;

        return config;
    }

    private void init() {

        /* get nodes sorted by lon */
        ArrayList<Node> sortedNodes = new ArrayList<>(getGraph().getNodes());
        sortedNodes.sort((n1, n2) -> n1.getCoordinate().lon > n2.getCoordinate().lon ? 1 : -1);
        Node topLeft  = sortedNodes.get(0);
        Node bottom   = sortedNodes.get(2);
        Node topRight = sortedNodes.get(3);


        /* setup scenario matrices */
        ODMatrix odMatrix, spawnDelayMatrix;


        /* PRIORITY_TO_THE_RIGHT */
        odMatrix = new SparseODMatrix();
        odMatrix.add(1, topRight, topLeft);
        odMatrix.add(1, bottom, topLeft);

        spawnDelayMatrix = new SparseODMatrix();
        spawnDelayMatrix.add(0, topRight, topLeft);
        spawnDelayMatrix.add(1, bottom, topLeft);

        addSubScenario(odMatrix, spawnDelayMatrix);


        /* NO_INTERCEPTION */
        odMatrix = new SparseODMatrix();
        odMatrix.add(1, topRight, topLeft);
        odMatrix.add(1, bottom, topRight);

        spawnDelayMatrix = new SparseODMatrix();
        spawnDelayMatrix.add(0, topRight, topLeft);
        spawnDelayMatrix.add(1, bottom, topRight);

        addSubScenario(odMatrix, spawnDelayMatrix);


        /* DEADLOCK */
        odMatrix = new SparseODMatrix();
        odMatrix.add(1, topRight, topLeft);
        odMatrix.add(1, topLeft, topRight);
        odMatrix.add(1, bottom, topLeft);

        spawnDelayMatrix = new SparseODMatrix();
        spawnDelayMatrix.add(0, topRight, topLeft);
        spawnDelayMatrix.add(1, topLeft, topRight);
        spawnDelayMatrix.add(1, bottom, topLeft);

        addSubScenario(odMatrix, spawnDelayMatrix);


        /* LEFT TURNER MUST WAIT */
        odMatrix = new SparseODMatrix();
        odMatrix.add(1, topRight, bottom);
        odMatrix.add(1, topLeft, topRight);

        spawnDelayMatrix = new SparseODMatrix();
        spawnDelayMatrix.add(0, topRight, bottom);
        spawnDelayMatrix.add(1, topLeft, topRight);

        addSubScenario(odMatrix, spawnDelayMatrix);
    }
}