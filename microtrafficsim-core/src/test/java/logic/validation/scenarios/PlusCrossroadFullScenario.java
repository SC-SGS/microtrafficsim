package logic.validation.scenarios;

import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.impl.BlockingCar;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.core.simulation.scenarios.impl.QueueScenarioSmall;
import microtrafficsim.core.simulation.utils.ODMatrix;
import microtrafficsim.core.simulation.utils.SparseODMatrix;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public class PlusCrossroadFullScenario extends QueueScenarioSmall {

    private enum ScenarioState {

        PRIORITY_TO_THE_RIGHT,
        NO_INTERSECTION,
        LEFT_TURNER_MUST_WAIT,
        ALL_LEFT,
        GO_WITHOUT_PRIORITY
    }

    private ScenarioState state;
    private Node bottomLeft, topLeft, mid, topRight, bottomRight;

    public PlusCrossroadFullScenario(ScenarioConfig config,
                                     StreetGraph graph,
                                     Supplier<VisualizationVehicleEntity> visVehicleFactory) {
        super(config, graph);
        init();

        setScenarioBuilder(new VehicleScenarioBuilder(
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
    public static ScenarioConfig setupConfig(ScenarioConfig config) {

        QueueScenarioSmall.setupConfig(config);

        config.maxVehicleCount                            = 4;
        config.crossingLogic.friendlyStandingInJamEnabled = true;

        return config;
    }

    private void init() {

        /* get nodes sorted by lon */
        ArrayList<Node> sortedNodes = new ArrayList<>(getGraph().getNodes());
        sortedNodes.sort((n1, n2) -> n1.getCoordinate().lon > n2.getCoordinate().lon ? 1 : -1);

        bottomLeft  = sortedNodes.get(0); // length to mid: 8
        topLeft     = sortedNodes.get(1); // length to mid: 7
        mid              = sortedNodes.get(2); // length to mid: 0
        topRight    = sortedNodes.get(3); // length to mid: 9
        bottomRight = sortedNodes.get(4); // length to mid: 12

        initPrioToTheRight();
        if (getConfig().crossingLogic.drivingOnTheRight)
            initLeftTurning();
        else
            initRightTurning();
    }

    private void initPrioToTheRight() {

        /* setup scenario matrices */
        ODMatrix odMatrix, spawnDelayMatrix;

        /* top-right and top-left */
        odMatrix = new SparseODMatrix();
        odMatrix.add(1, topRight, bottomLeft);
        odMatrix.add(1, topLeft, bottomRight);

        spawnDelayMatrix = new SparseODMatrix();
        spawnDelayMatrix.add(0, topRight, bottomLeft);
        spawnDelayMatrix.add(2, topLeft, bottomRight);

        addSubScenario(odMatrix, spawnDelayMatrix);


        /* top-left and bottom-left */
        odMatrix = new SparseODMatrix();
        odMatrix.add(1, topLeft, bottomRight);
        odMatrix.add(1, bottomLeft, topRight);

        spawnDelayMatrix = new SparseODMatrix();
        spawnDelayMatrix.add(1, topLeft, bottomRight);
        spawnDelayMatrix.add(0, bottomLeft, topRight);

        addSubScenario(odMatrix, spawnDelayMatrix);


        /* bottom-left and bottom-right */
        odMatrix = new SparseODMatrix();
        odMatrix.add(1, bottomLeft, topRight);
        odMatrix.add(1, bottomRight, topLeft);

        spawnDelayMatrix = new SparseODMatrix();
        spawnDelayMatrix.add(4, bottomLeft, topRight);
        spawnDelayMatrix.add(0, bottomRight, topLeft);

        addSubScenario(odMatrix, spawnDelayMatrix);


        /* bottom-right and top-right */
        odMatrix = new SparseODMatrix();
        odMatrix.add(1, bottomRight, topLeft);
        odMatrix.add(1, topRight, bottomLeft);

        spawnDelayMatrix = new SparseODMatrix();
        spawnDelayMatrix.add(0, bottomRight, topLeft);
        spawnDelayMatrix.add(3, topRight, bottomLeft);

        addSubScenario(odMatrix, spawnDelayMatrix);
    }

    private void initLeftTurning() {
        /* setup scenario matrices */
        ODMatrix odMatrix, spawnDelayMatrix;

        /* top-left and bottom-right, bottom-right turns left */
        odMatrix = new SparseODMatrix();
        odMatrix.add(1, topLeft, bottomRight);
        odMatrix.add(1, bottomRight, bottomLeft);

        spawnDelayMatrix = new SparseODMatrix();
        spawnDelayMatrix.add(5, topLeft, bottomRight);
        spawnDelayMatrix.add(0, bottomRight, bottomLeft);

        addSubScenario(odMatrix, spawnDelayMatrix);
    }

    private void initRightTurning() {
        /* setup scenario matrices */
        ODMatrix odMatrix, spawnDelayMatrix;

        /* top-left and bottom-right, bottom-right turns left */
        odMatrix = new SparseODMatrix();
        odMatrix.add(1, topLeft, bottomRight);
        odMatrix.add(1, bottomRight, topRight);

        spawnDelayMatrix = new SparseODMatrix();
        spawnDelayMatrix.add(5, topLeft, bottomRight);
        spawnDelayMatrix.add(0, bottomRight, topRight);

        addSubScenario(odMatrix, spawnDelayMatrix);
    }

    public Node getMid() {
        return mid;
    }
}