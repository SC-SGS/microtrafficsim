package logic.validation.scenarios.impl;

import logic.validation.scenarios.ValidationScenario;
import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.impl.Car;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.utils.ODMatrix;
import microtrafficsim.core.simulation.utils.SparseODMatrix;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * Created by Dominic on 29.12.16.
 */
public class MotorwaySlipRoadScenario extends ValidationScenario {

    private Node            bottomMotorway;
    private Node            interception;
    private Node            topMotorway;
    private Node            bottomRight;
    private ScenarioBuilder scenarioBuilder;
    private ODMatrix secondSpawnDelayMatrix;

    public MotorwaySlipRoadScenario(SimulationConfig config,
                              StreetGraph graph,
                              Supplier<VisualizationVehicleEntity> visVehicleFactory) {
        super(config, graph);

        /* get nodes sorted by lon */
        ArrayList<Node> sortedNodes = new ArrayList<>(graph.getNodes());
        sortedNodes.sort((n1, n2) -> n1.getCoordinate().lon > n2.getCoordinate().lon ? 1 : -1);

        bottomMotorway = sortedNodes.get(0);
        interception   = sortedNodes.get(1);
        topMotorway    = sortedNodes.get(2);
        bottomRight    = sortedNodes.get(3);

        /* prepare */
        odMatrix.add(2, bottomMotorway, topMotorway);
        odMatrix.add(2, bottomRight, topMotorway);

        spawnDelayMatrix.add(0, bottomMotorway, topMotorway);
        spawnDelayMatrix.add(9, bottomRight, topMotorway);

        secondSpawnDelayMatrix = new SparseODMatrix();
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
                    return new Car(ID, seed, route, spawnDelay, getVehicleContainer());
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
        scenarioBuilder.prepare(this);
    }
}