package logic.validation.scenarios;

import logic.validation.Main;
import logic.validation.ValidationScenario;
import microtrafficsim.core.frameworks.vehicle.IVisualizationVehicle;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.simulation.controller.configs.SimulationConfig;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.utils.id.ConcurrentLongIDGenerator;
import microtrafficsim.utils.resources.PackagedResource;

import java.io.File;
import java.util.Iterator;
import java.util.function.Supplier;

/**
 * This class defines a concrete scenarios of a simulation. This means it
 * contains an extension of {@link SimulationConfig} to define simulation
 * parameters. Furthermore, this class serves with a list of uniformly randomly
 * chosen start and end nodes for the vehicles and an age that counts the
 * finished simulation steps.
 *
 * @author Dominic Parga Cacheiro, Jan-Oliver Schmidt
 */
public class TCrossroadScenario extends ValidationScenario {

    public static class Config extends ValidationScenario.Config {

        {
            // super attributes
            longIDGenerator = new ConcurrentLongIDGenerator();
            msPerTimeStep = 200;
            maxVehicleCount = 3;
            crossingLogic.drivingOnTheRight = true;
            crossingLogic.edgePriorityEnabled = true;
            crossingLogic.priorityToTheRightEnabled = true;
            crossingLogic.setOnlyOneVehicle(false);
            crossingLogic.goWithoutPriorityEnabled = false;
            // own attributes
            ageForPause = -1;
        }
    }

    private static final String OSM_FILENAME = "T_crossroad.osm";

    public static void main(String[] args) throws Exception {
        File file;

        if (args.length == 1) {
            switch(args[0]) {
                case "-h":
                case "--help":
                    Main.printUsage();
                    return;
                default:
                    file = new File(args[0]);
            }
        } else {
            file = new PackagedResource(Main.class, OSM_FILENAME).asTemporaryFile();
        }

        Main.show(
                new MercatorProjection(),
                file,
                new Config(),
                TCrossroadScenario.class);
    }

    private enum NextScenarioState {
        PRIORITY_TO_THE_RIGHT, NO_INTERCEPTION, DEADLOCK
    }

    private Node bottom = null;
    private Node topRight = null;
    private Node topLeft = null;
    private NextScenarioState nextScenarioState;

    /**
     * Default constructor.
     *
     * @param config         The used config file for this scenarios.
     * @param graph          The streetgraph used for this scenarios.
     * @param vehicleFactory This creates vehicles.
     */
    public TCrossroadScenario(Config config, StreetGraph graph,
                              Supplier<IVisualizationVehicle> vehicleFactory) {
        super(config,
                graph,
                vehicleFactory);
        nextScenarioState = NextScenarioState.PRIORITY_TO_THE_RIGHT;
    }

    @Override
    protected void updateScenarioState(VehicleState vehicleState) {

        int updateGraphDelay = justInitialized ? 0 : 1;

        if (vehicleState == VehicleState.DESPAWNED && getVehiclesCount() == 0) {
            switch (nextScenarioState) {
                case PRIORITY_TO_THE_RIGHT:
                    createAndAddCar(topRight, topLeft, 0);
                    createAndAddCar(bottom, topLeft, 1 + updateGraphDelay);
                    nextScenarioState = NextScenarioState.NO_INTERCEPTION;
                    break;
                case NO_INTERCEPTION:
                    createAndAddCar(topRight, topLeft, 0);
                    createAndAddCar(bottom, topRight, 1 + updateGraphDelay);
                    nextScenarioState = NextScenarioState.DEADLOCK;
                    break;
                case DEADLOCK:
                    createAndAddCar(topRight, topLeft, 0);
                    createAndAddCar(topLeft, topRight, 1 + updateGraphDelay);
                    createAndAddCar(bottom, topLeft, 1 + updateGraphDelay);
                    nextScenarioState = NextScenarioState.PRIORITY_TO_THE_RIGHT;
            }
        }
    }

    @Override
    protected void createAndAddVehicles() {
        /*
        top mid microtrafficsim.core.map.Coordinate (48.78925, 9.260648)
        bottom microtrafficsim.core.map.Coordinate (48.788826, 9.260972)
        top right microtrafficsim.core.map.Coordinate (48.78989, 9.262516)
        top left microtrafficsim.core.map.Coordinate (48.78928, 9.258616)
        */
        Iterator<Node> iter = graph.getNodeIterator();
        while (iter.hasNext()) {

            Node node = iter.next();
            Coordinate c = node.getCoordinate();
            // bottom
            if (c.equals(new Coordinate(48.788826f, 9.260972f))) bottom = node;
            // top right
            else if (c.equals(new Coordinate(48.78952f, 9.261424f))) topRight = node;
            // top left
            else if (c.equals(new Coordinate(48.78901f, 9.259969f))) topLeft = node;
        }

        updateScenarioState(VehicleState.DESPAWNED);
        justInitialized = false;
    }
}