package logic.validation.scenarios.impl;

import logic.validation.scenarios.ValidationScenario;
import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.logic.vehicles.impl.BlockingCar;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.vis.opengl.utils.Color;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public class PlusCrossroadScenario extends ValidationScenario {

    private Node             bottomLeft;
    private Node             topLeft;
    private Node             mid;
    private Node             topRight;
    private Node             bottomRight;
    private ScenarioBuilder scenarioBuilder;
    private NextScenarioState state;

    public PlusCrossroadScenario(SimulationConfig config,
                              StreetGraph graph,
                              Supplier<VisualizationVehicleEntity> visVehicleFactory) {
        super(config, graph);

        /* get nodes sorted by lon */
        ArrayList<Node> sortedNodes = new ArrayList<>(graph.getNodes());
        sortedNodes.sort((n1, n2) -> n1.getCoordinate().lon > n2.getCoordinate().lon ? 1 : -1);

        bottomLeft  = sortedNodes.get(0);
        topLeft     = sortedNodes.get(1);
        mid         = sortedNodes.get(2);
        topRight    = sortedNodes.get(3);
        bottomRight = sortedNodes.get(4);

        scenarioBuilder = new VehicleScenarioBuilder(
                config.seedGenerator.next(),
                visVehicleFactory,
                (scenario, route) -> {
                    long ID        = scenario.getConfig().longIDGenerator.next();
                    long seed      = scenario.getConfig().seedGenerator.next();
                    int spawnDelay = spawnDelayMatrix.get(route.getStart(), route.getEnd());
                    BlockingCar vehicle = new BlockingCar(ID, seed, route, spawnDelay, getVehicleContainer());
                    if (state == NextScenarioState.PRIORITY_TO_THE_RIGHT && route.getStart() == mid)
                        vehicle.toggleBlockMode();
                    return vehicle;
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

        config.maxVehicleCount                         = 4;
        config.crossingLogic.friendlyStandingInJamEnabled = true;

        return config;
    }

    /*
    |==================|
    | (i) StepListener |
    |==================|
    */
    @Override
    public void didOneStep(Simulation simulation) {
        if (state == NextScenarioState.PRIORITY_TO_THE_RIGHT && getVehicleContainer().getVehicleCount() == 2)
            for (AbstractVehicle vehicle : getVehicleContainer().getVehicles()) {
                BlockingCar blockingCar = (BlockingCar) vehicle;
                if (blockingCar.isBlocking())
                    blockingCar.toggleBlockMode();
            }

        super.didOneStep(simulation);
    }

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
                odMatrix.add(1, topRight, bottomRight);
                odMatrix.add(1, topLeft, bottomRight);

                spawnDelayMatrix.add(0, topRight, bottomRight);
                spawnDelayMatrix.add(2, topLeft, bottomRight);

                state = NextScenarioState.NO_INTERSECTION;
                break;
            case NO_INTERSECTION:
                odMatrix.add(1, topRight, bottomLeft);
                odMatrix.add(1, bottomLeft, topRight);

                spawnDelayMatrix.add(0, topRight, bottomLeft);
                spawnDelayMatrix.add(1, bottomLeft, topRight);

                state = NextScenarioState.LEFT_TURNER_MUST_WAIT;
                break;
            case LEFT_TURNER_MUST_WAIT:
                odMatrix.add(1, topLeft, bottomRight);
                odMatrix.add(1, bottomRight, bottomLeft);

                spawnDelayMatrix.add(5, topLeft, bottomRight);
                spawnDelayMatrix.add(0, bottomRight, bottomLeft);

                state = NextScenarioState.ALL_LEFT;
                break;
            case ALL_LEFT:
                odMatrix.add(1, bottomLeft, topLeft);
                odMatrix.add(1, bottomRight, bottomLeft);
                odMatrix.add(1, topRight, bottomRight);
                odMatrix.add(1, topLeft, topRight);

                spawnDelayMatrix.add(4, bottomLeft, topLeft);
                spawnDelayMatrix.add(0, bottomRight, bottomLeft);
                spawnDelayMatrix.add(3, topRight, bottomRight);
                spawnDelayMatrix.add(5, topLeft, topRight);

                state = NextScenarioState.GO_WITHOUT_PRIORITY;
                break;
            case GO_WITHOUT_PRIORITY:
                odMatrix.add(1, mid, bottomLeft);
                odMatrix.add(1, topRight, bottomLeft);
                odMatrix.add(1, bottomRight, topLeft);

                spawnDelayMatrix.add(0, mid, bottomLeft);
                spawnDelayMatrix.add(0, topRight, bottomLeft);
                spawnDelayMatrix.add(0, bottomRight, topLeft);

                state = NextScenarioState.PRIORITY_TO_THE_RIGHT;
                break;
        }

        try {
            scenarioBuilder.prepare(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private enum NextScenarioState {
        PRIORITY_TO_THE_RIGHT,
        NO_INTERSECTION,
        LEFT_TURNER_MUST_WAIT,
        ALL_LEFT,
        GO_WITHOUT_PRIORITY
    }
}