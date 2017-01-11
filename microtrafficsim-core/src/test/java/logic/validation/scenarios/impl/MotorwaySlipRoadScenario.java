package logic.validation.scenarios.impl;

import logic.validation.scenarios.ValidationScenario;
import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.impl.Car;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.impl.BasicScenario;
import microtrafficsim.core.simulation.utils.ODMatrix;
import microtrafficsim.core.simulation.utils.SparseODMatrix;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * Validates the crossing logic in a scenario where a street crosses a motorway.
 *
 * @author Dominic Parga Cacheiro
 */
public class MotorwaySlipRoadScenario extends ValidationScenario {

    private ScenarioBuilder scenarioBuilder;

    /**
     * Initializes the matrices (for routes and spawn delays). For this, it has to sort the few nodes to guarantee
     * determinism independent of complicated coordinate calculations.
     *
     * @see ValidationScenario#ValidationScenario(SimulationConfig, StreetGraph)
     */
    public MotorwaySlipRoadScenario(SimulationConfig config,
                              StreetGraph graph,
                              Supplier<VisualizationVehicleEntity> visVehicleFactory) {
        super(config, graph);

        /* get nodes sorted by lon */
        ArrayList<Node> sortedNodes = new ArrayList<>(graph.getNodes());
        sortedNodes.sort((n1, n2) -> n1.getCoordinate().lon > n2.getCoordinate().lon ? 1 : -1);

        Node bottomMotorway = sortedNodes.get(0);
        Node interception = sortedNodes.get(1);
        Node topMotorway = sortedNodes.get(2);
        Node bottomRight = sortedNodes.get(3);

        /* prepare */
        odMatrix.add(2, bottomMotorway, topMotorway);
        odMatrix.add(2, bottomRight, topMotorway);

        spawnDelayMatrix.add(0, bottomMotorway, topMotorway);
        spawnDelayMatrix.add(9, bottomRight, topMotorway);

        ODMatrix secondSpawnDelayMatrix = new SparseODMatrix();
        secondSpawnDelayMatrix.add(5, bottomMotorway, topMotorway);
        secondSpawnDelayMatrix.add(13, bottomRight, topMotorway);

        final boolean[] toggle = { false, false };
        scenarioBuilder = new VehicleScenarioBuilder(
                config.seedGenerator.next(),
                visVehicleFactory,
                (scenario, route) -> {
                    long ID        = scenario.getConfig().longIDGenerator.next();
                    long seed      = scenario.getConfig().seedGenerator.next();

                    // toggle spawns for two scenarios in one run
                    int spawnDelay;
                    if (route.getStart() == bottomMotorway) {
                        if (toggle[0])
                            spawnDelay = spawnDelayMatrix.get(route.getStart(), route.getEnd());
                        else
                            spawnDelay = secondSpawnDelayMatrix.get(route.getStart(), route.getEnd());
                        toggle[0] = !toggle[0];
                    } else {  // route.getStart() == bottomRight
                        if (toggle[1])
                            spawnDelay = spawnDelayMatrix.get(route.getStart(), route.getEnd());
                        else
                            spawnDelay = secondSpawnDelayMatrix.get(route.getStart(), route.getEnd());
                        toggle[1] = !toggle[1];
                    }

                    Car car = new Car(ID, seed, route, spawnDelay);
                    car.addStateListener(getVehicleContainer());
                    return car;
                }
        );
    }

    /**
     * @param config
     * @return the given config updated; just for practical purpose
     */
    public static SimulationConfig setupConfig(SimulationConfig config) {

        ValidationScenario.setupConfig(config);

        config.maxVehicleCount                            = 3;
        config.crossingLogic.friendlyStandingInJamEnabled = false;
        Car.maxVelocity = 1;

        return config;
    }

    /*
    |========================|
    | (c) ValidationScenario |
    |========================|
    */
    @Override
    public void prepare() {
        try {
            scenarioBuilder.prepare(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}