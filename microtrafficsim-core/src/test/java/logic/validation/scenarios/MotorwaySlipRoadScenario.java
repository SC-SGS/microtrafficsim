package logic.validation.scenarios;

import microtrafficsim.core.simulation.scenarios.impl.QueueScenarioSmall;
import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.impl.Car;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.core.simulation.utils.ODMatrix;
import microtrafficsim.core.simulation.utils.SparseODMatrix;

import java.util.ArrayList;
import java.util.function.Supplier;

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
     * @see QueueScenarioSmall#QueueScenarioSmall(ScenarioConfig, StreetGraph)
     */
    public MotorwaySlipRoadScenario(ScenarioConfig config,
                                    StreetGraph graph,
                                    Supplier<VisualizationVehicleEntity> visVehicleFactory) {
        super(config, graph);
        init();

        ODMatrix secondSpawnDelayMatrix = new SparseODMatrix();
        secondSpawnDelayMatrix.add(5, bottomMotorway, topMotorway);
        secondSpawnDelayMatrix.add(13, bottomRight, topMotorway);

        final boolean[] toggle = { false, false };
        setScenarioBuilder(new VehicleScenarioBuilder(
                visVehicleFactory,
                (scenario, route) -> {
                    long ID        = scenario.getConfig().longIDGenerator.next();
                    long seed      = scenario.getConfig().seedGenerator.next();

                    // toggle spawns for two scenarios in one run
                    int spawnDelay;
                    if (route.getStart() == bottomMotorway) {
                        if (toggle[0])
                            spawnDelay = getSpawnDelayMatrix().get(route.getStart(), route.getEnd());
                        else
                            spawnDelay = secondSpawnDelayMatrix.get(route.getStart(), route.getEnd());
                        toggle[0] = !toggle[0];
                    } else {  // route.getStart() == bottomRight
                        if (toggle[1])
                            spawnDelay = getSpawnDelayMatrix().get(route.getStart(), route.getEnd());
                        else
                            spawnDelay = secondSpawnDelayMatrix.get(route.getStart(), route.getEnd());
                        toggle[1] = !toggle[1];
                    }

                    Car car = new Car(ID, seed, route, spawnDelay);
                    car.addStateListener(getVehicleContainer());
                    return car;
                }
        ));
    }

    /**
     * @param config
     * @return the given config updated; just for practical purpose
     */
    public static ScenarioConfig setupConfig(ScenarioConfig config) {

        QueueScenarioSmall.setupConfig(config);

        config.maxVehicleCount                            = 3;
        config.crossingLogic.friendlyStandingInJamEnabled = false;
        Car.maxVelocity = 1;

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