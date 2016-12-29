package logic.validation.scenarios.impl;

import logic.validation.scenarios.ValidationScenario;
import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.logic.vehicles.VehicleStateListener;
import microtrafficsim.core.logic.vehicles.impl.Car;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.core.StepListener;
import microtrafficsim.core.simulation.utils.ODMatrix;
import microtrafficsim.core.simulation.utils.SparseODMatrix;
import microtrafficsim.core.vis.opengl.utils.Color;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public class TCrossroadScenario extends ValidationScenario {

    private Node              topLeft;
    private Node              bottom;
    private Node              topRight;
    private ScenarioBuilder   scenarioBuilder;
    private NextScenarioState state;

    public TCrossroadScenario(SimulationConfig config,
                              StreetGraph graph,
                              Supplier<VisualizationVehicleEntity> visVehicleFactory) {
        super(config, graph);

        /* get nodes sorted by lon */
        ArrayList<Node> sortedNodes = new ArrayList<>(graph.getNodes());
        sortedNodes.sort((n1, n2) -> n1.getCoordinate().lon > n2.getCoordinate().lon ? 1 : -1);
        topLeft  = sortedNodes.get(0);
        bottom   = sortedNodes.get(2);
        topRight = sortedNodes.get(3);

        /* build scenario */
        scenarioBuilder = new VehicleScenarioBuilder(
                config.seedGenerator.next(),
                visVehicleFactory,
                (scenario, route) -> {
                    long ID        = scenario.getConfig().longIDGenerator.next();
                    long seed      = scenario.getConfig().seedGenerator.next();
                    int spawnDelay = spawnDelayMatrix.get(route.getStart(), route.getEnd());
                    return new Car(ID, seed, route, spawnDelay, getVehicleContainer());
                }
        );

        state = NextScenarioState.PRIORITY_TO_THE_RIGHT;
    }

    /**
     * @param config
     * @return the given config updated; just for practical purpose
     */
    public static SimulationConfig setupConfig(SimulationConfig config) {

        ValidationScenario.setupConfig(config);

        config.maxVehicleCount                         = 3;
        config.crossingLogic.friendlyStandingInJamEnabled = false;

        return config;
    }

    private enum NextScenarioState { PRIORITY_TO_THE_RIGHT, NO_INTERCEPTION, DEADLOCK }

    /*
    |========================|
    | (c) ValidationScenario |
    |========================|
    */
    @Override
    public void prepare() {

        odMatrix.clear();
        spawnDelayMatrix.clear();

        switch (state) {
            case PRIORITY_TO_THE_RIGHT:
                odMatrix.add(1, topRight, topLeft);
                odMatrix.add(1, bottom, topLeft);

                spawnDelayMatrix.add(0, topRight, topLeft);
                spawnDelayMatrix.add(1, bottom, topLeft);

                state = NextScenarioState.NO_INTERCEPTION;
                break;
            case NO_INTERCEPTION:
                odMatrix.add(1, topRight, topLeft);
                odMatrix.add(1, bottom, topRight);

                spawnDelayMatrix.add(0, topRight, topLeft);
                spawnDelayMatrix.add(1, bottom, topRight);

                state = NextScenarioState.DEADLOCK;
                break;
            case DEADLOCK:
                odMatrix.add(1, topRight, topLeft);
                odMatrix.add(1, topLeft, topRight);
                odMatrix.add(1, bottom, topLeft);

                spawnDelayMatrix.add(0, topRight, topLeft);
                spawnDelayMatrix.add(1, topLeft, topRight);
                spawnDelayMatrix.add(1, bottom, topLeft);

                state = NextScenarioState.PRIORITY_TO_THE_RIGHT;
                break;
        }

        scenarioBuilder.prepare(this);
    }

}
