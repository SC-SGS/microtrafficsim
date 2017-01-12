package logic.validation.scenarios.impl;

import logic.validation.scenarios.QueueScenario;
import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.logic.vehicles.impl.BlockingCar;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.utils.ODMatrix;
import microtrafficsim.core.simulation.utils.SparseODMatrix;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public class PlusCrossroadScenario extends QueueScenario {

    private enum ScenarioState {

        PRIORITY_TO_THE_RIGHT,
        NO_INTERSECTION,
        LEFT_TURNER_MUST_WAIT,
        ALL_LEFT,
        GO_WITHOUT_PRIORITY
    }

    private ScenarioState state;
    private Node mid;

    public PlusCrossroadScenario(SimulationConfig config,
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

                    BlockingCar vehicle = new BlockingCar(ID, seed, route, spawnDelay);
                    vehicle.addStateListener(getVehicleContainer());

                    if (state == ScenarioState.GO_WITHOUT_PRIORITY && route.getStart() == mid)
                        vehicle.toggleBlockMode();
                    return vehicle;
                }
        ));

        state = ScenarioState.PRIORITY_TO_THE_RIGHT;
    }

    /**
     * @param config
     * @return the given config updated; just for practical purpose
     */
    public static SimulationConfig setupConfig(SimulationConfig config) {

        QueueScenario.setupConfig(config);

        config.maxVehicleCount                            = 4;
        config.crossingLogic.friendlyStandingInJamEnabled = true;

        return config;
    }

    private void init() {

        /* get nodes sorted by lon */
        ArrayList<Node> sortedNodes = new ArrayList<>(getGraph().getNodes());
        sortedNodes.sort((n1, n2) -> n1.getCoordinate().lon > n2.getCoordinate().lon ? 1 : -1);

        Node bottomLeft  = sortedNodes.get(0);
        Node topLeft     = sortedNodes.get(1);
        mid              = sortedNodes.get(2);
        Node topRight    = sortedNodes.get(3);
        Node bottomRight = sortedNodes.get(4);


        /* setup scenario matrices */
        ODMatrix odMatrix, spawnDelayMatrix;

        /* PRIORITY_TO_THE_RIGHT */
        odMatrix = new SparseODMatrix();
        odMatrix.add(1, topRight, bottomRight);
        odMatrix.add(1, topLeft, bottomRight);

        spawnDelayMatrix = new SparseODMatrix();
        spawnDelayMatrix.add(0, topRight, bottomRight);
        spawnDelayMatrix.add(2, topLeft, bottomRight);

        addSubScenario(odMatrix, spawnDelayMatrix);


        /* NO_INTERSECTION */
        odMatrix = new SparseODMatrix();
        odMatrix.add(1, topRight, bottomLeft);
        odMatrix.add(1, bottomLeft, topRight);

        spawnDelayMatrix = new SparseODMatrix();
        spawnDelayMatrix.add(0, topRight, bottomLeft);
        spawnDelayMatrix.add(1, bottomLeft, topRight);

        addSubScenario(odMatrix, spawnDelayMatrix);


        /* LEFT_TURNER_MUST_WAIT */
        odMatrix = new SparseODMatrix();
        odMatrix.add(1, topLeft, bottomRight);
        odMatrix.add(1, bottomRight, bottomLeft);

        spawnDelayMatrix = new SparseODMatrix();
        spawnDelayMatrix.add(5, topLeft, bottomRight);
        spawnDelayMatrix.add(0, bottomRight, bottomLeft);

        addSubScenario(odMatrix, spawnDelayMatrix);


        /* ALL_LEFT */
        odMatrix = new SparseODMatrix();
        odMatrix.add(1, bottomLeft, topLeft);
        odMatrix.add(1, bottomRight, bottomLeft);
        odMatrix.add(1, topRight, bottomRight);
        odMatrix.add(1, topLeft, topRight);

        spawnDelayMatrix = new SparseODMatrix();
        spawnDelayMatrix.add(4, bottomLeft, topLeft);
        spawnDelayMatrix.add(0, bottomRight, bottomLeft);
        spawnDelayMatrix.add(3, topRight, bottomRight);
        spawnDelayMatrix.add(5, topLeft, topRight);

        addSubScenario(odMatrix, spawnDelayMatrix);


        /* GO_WITHOUT_PRIORITY */
        odMatrix = new SparseODMatrix();
        odMatrix.add(1, mid, bottomLeft);
        odMatrix.add(1, topRight, bottomLeft);
        odMatrix.add(1, bottomRight, topLeft);

        addSubScenario(odMatrix);
    }

    private void switchState() {
        switch (state) {
            case PRIORITY_TO_THE_RIGHT:
                state = ScenarioState.NO_INTERSECTION;
                break;
            case NO_INTERSECTION:
                state = ScenarioState.LEFT_TURNER_MUST_WAIT;
                break;
            case LEFT_TURNER_MUST_WAIT:
                state = ScenarioState.ALL_LEFT;
                break;
            case ALL_LEFT:
                state = ScenarioState.GO_WITHOUT_PRIORITY;
                break;
            case GO_WITHOUT_PRIORITY:
                state = ScenarioState.PRIORITY_TO_THE_RIGHT;
                break;
        }
    }

    /*
    |==================|
    | (i) StepListener |
    |==================|
    */
    @Override
    public void didOneStep(Simulation simulation) {
        if (state == ScenarioState.GO_WITHOUT_PRIORITY && getVehicleContainer().getVehicleCount() == 2) {
            for (AbstractVehicle vehicle : getVehicleContainer().getVehicles()) {
                BlockingCar blockingCar = (BlockingCar) vehicle;
                if (blockingCar.isBlocking())
                    blockingCar.toggleBlockMode();
            }
        }

        switchState();

        super.didOneStep(simulation);
    }
}