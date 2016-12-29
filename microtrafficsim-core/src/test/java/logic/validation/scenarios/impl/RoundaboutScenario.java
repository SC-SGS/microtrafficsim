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
import microtrafficsim.core.vis.opengl.utils.Color;

import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public class RoundaboutScenario extends ValidationScenario {

    private Node             bottom;
    private Node             right;
    private Node             left;
    private Node             topLeft;
    private Node             topRight;
    private ScenarioBuilder scenarioBuilder;
    private NextScenarioState state;

    public RoundaboutScenario(SimulationConfig config,
                              StreetGraph graph,
                              Supplier<VisualizationVehicleEntity> visVehicleFactory) {
        super(config, graph);

        /* get nodes sorted by lon */
        ArrayList<Node> sortedNodes = new ArrayList<>(graph.getNodes());
        sortedNodes.sort((n1, n2) -> {
            boolean sortByLon = false;
            //noinspection ConstantConditions
            if (sortByLon) {
                if (n1.getCoordinate().lon > n2.getCoordinate().lon)
                    return 1;
                else
                    return -1;
            } else {
                if (n1.getCoordinate().lat > n2.getCoordinate().lat)
                    return 1;
                else
                    return -1;
            }
        });

        //    for (Node n : sortedNodes) {
        //      System.out.println("Node(" + n.ID + ").coord = " + n.getCoordinate());
        //    }

        // node IDs in processing-file, sorted by lat ascending:
        // 243.679.734 bottom
        // 242.539.693 right
        //  27.281.851
        // 498.201.336
        // 260.334.121 left
        //  43.055.934
        //  27.281.850
        //  27.281.852
        // 271.387.772 top left
        // 254.636.138 top right

        bottom   = sortedNodes.get(0);
        right    = sortedNodes.get(1);
        left     = sortedNodes.get(4);
        topLeft  = sortedNodes.get(8);
        topRight = sortedNodes.get(9);

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

        state = NextScenarioState.TOP_RIGHT;
    }

    /**
     * @param config
     * @return the given config updated; just for practical purpose
     */
    public static SimulationConfig setupConfig(SimulationConfig config) {

        ValidationScenario.setupConfig(config);

        config.maxVehicleCount                         = 2;
        config.crossingLogic.friendlyStandingInJamEnabled = true;

        return config;
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
            case TOP_RIGHT:
                odMatrix.add(1, topRight, right);
                odMatrix.add(1, topLeft, left);

                spawnDelayMatrix.add(0, topRight, right);
                spawnDelayMatrix.add(13, topLeft, left);

                state = NextScenarioState.TOP_LEFT;
                break;
            case TOP_LEFT:
                odMatrix.add(1, topLeft, topRight);
                odMatrix.add(1, bottom, left);

                spawnDelayMatrix.add(5, topLeft, topRight);
                spawnDelayMatrix.add(0, bottom, left);

                state = NextScenarioState.BOTTOM;
                break;
            case BOTTOM:
                odMatrix.add(1, bottom, left);
                odMatrix.add(1, right, topRight);

                spawnDelayMatrix.add(0, bottom, left);
                spawnDelayMatrix.add(5, right, topRight);

                state = NextScenarioState.RIGHT;
                break;
            case RIGHT:    // maybe useless, because checked crossroad is same as in TOP_RIGHT
                odMatrix.add(1, right, bottom);
                odMatrix.add(1, topLeft, left);

                spawnDelayMatrix.add(0, right, bottom);
                spawnDelayMatrix.add(7, topLeft, left);

                state = NextScenarioState.TOP_RIGHT;
                break;
        }

        scenarioBuilder.prepare(this);
    }

    private enum NextScenarioState { TOP_RIGHT, TOP_LEFT, BOTTOM, RIGHT }
}