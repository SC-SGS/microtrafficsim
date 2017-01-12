package logic.validation.scenarios.impl;

import logic.validation.scenarios.QueueScenario;
import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.impl.Car;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.utils.ODMatrix;
import microtrafficsim.core.simulation.utils.SparseODMatrix;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public class TCrossroadScenario extends QueueScenario {

    public TCrossroadScenario(SimulationConfig config,
                              StreetGraph graph,
                              Supplier<VisualizationVehicleEntity> visVehicleFactory) {
        super(config, graph);
        init();

        setScenarioBuilder(new VehicleScenarioBuilder(
                config.seedGenerator.next(),
                visVehicleFactory,
                (scenario, route) -> {
                    long ID        = scenario.getConfig().longIDGenerator.next();
                    long seed      = scenario.getConfig().seedGenerator.next();
                    int spawnDelay = getSpawnDelayMatrix().get(route.getStart(), route.getEnd());

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
    public static SimulationConfig setupConfig(SimulationConfig config) {

        QueueScenario.setupConfig(config);

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
    }
}