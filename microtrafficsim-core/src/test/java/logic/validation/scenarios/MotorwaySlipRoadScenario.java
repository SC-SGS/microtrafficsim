package logic.validation.scenarios;

import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.OldRoute;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.impl.OldQueueScenarioSmall;
import microtrafficsim.core.simulation.utils.ODMatrix;
import microtrafficsim.core.simulation.utils.SparseODMatrix;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * Validates the crossing logic in a scenario where a street crosses a motorway.
 *
 * @author Dominic Parga Cacheiro
 */
public class MotorwaySlipRoadScenario extends OldQueueScenarioSmall {

    private Node bottomMotorway, topMotorway, bottomRight;

    /**
     * Initializes the matrices (for routes and spawn delays). For this, it has to sort the few nodes to guarantee
     * determinism independent of complicated coordinate calculations.
     *
     * @see OldQueueScenarioSmall#OldQueueScenarioSmall(SimulationConfig, Graph)
     */
    public MotorwaySlipRoadScenario(SimulationConfig config,
                                    Graph graph,
                                    Supplier<VisualizationVehicleEntity> visVehicleFactory) {
        super(config, graph);
        init();

        setScenarioBuilder(new Builder(config.seed, visVehicleFactory));
    }

    /**
     * @param config
     * @return the given config updated; just for practical purpose
     */
    public static SimulationConfig setupConfig(SimulationConfig config) {

        OldQueueScenarioSmall.setupConfig(config);

        config.maxVehicleCount                            = 3;
        config.crossingLogic.friendlyStandingInJamEnabled = false;

        return config;
    }

    private void init() {
        /* get nodes sorted by lon */
        ArrayList<Node> sortedNodes = new ArrayList<>(getGraph().getNodes());
        sortedNodes.sort((n1, n2) -> n1.getCoordinate().lon > n2.getCoordinate().lon ? 1 : -1);

        bottomMotorway = sortedNodes.get(0);
        // interception = sortedNodes.get(1);
        topMotorway = sortedNodes.get(2);
        bottomRight = sortedNodes.get(3);


        /* setup scenario matrices */
        ODMatrix odMatrix = new SparseODMatrix();
        odMatrix.add(2, bottomMotorway, topMotorway);
        odMatrix.add(2, bottomRight, topMotorway);

        ODMatrix spawnDelayMatrix = new SparseODMatrix();
        spawnDelayMatrix.add(0, bottomMotorway, topMotorway);
        spawnDelayMatrix.add(9, bottomRight, topMotorway);
        addSubScenario(odMatrix, spawnDelayMatrix);
    }
}