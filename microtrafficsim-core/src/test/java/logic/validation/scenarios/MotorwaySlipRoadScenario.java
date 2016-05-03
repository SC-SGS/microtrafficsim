package logic.validation.scenarios;

import logic.validation.Main;
import logic.validation.ValidationScenario;
import microtrafficsim.core.frameworks.vehicle.IVisualizationVehicle;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.logic.vehicles.impl.Car;
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
public class MotorwaySlipRoadScenario extends ValidationScenario {

    public static class Config extends ValidationScenario.Config {

        public int ageForPause = -1;

        {
            // super attributes
            longIDGenerator = new ConcurrentLongIDGenerator();
            msPerTimeStep = 200;
            maxVehicleCount = 3;
            crossingLogic.drivingOnTheRight = true;
            crossingLogic.edgePriorityEnabled = true;
            crossingLogic.priorityToTheRightEnabled = true;
            crossingLogic.setOnlyOneVehicle(false);
            // own attributes
            ageForPause = -1;
            // vehicle
            Car.maxVelocity = 1;
        }
    }

    private static final String OSM_FILENAME = "motorway_slip-road.osm";

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
                MotorwaySlipRoadScenario.class);
    }

    private enum NextScenarioState {
        PRIORITY_TO_THE_MOTORWAY;
    }

    private Node bottomMotorway = null, interception = null, topMotorway = null, bottomRight = null;
    private NextScenarioState nextScenarioState;

    /**
     * Default constructor.
     *
     * @param config         The used config file for this scenarios.
     * @param graph          The streetgraph used for this scenarios.
     * @param vehicleFactory This creates vehicles.
     */
    public MotorwaySlipRoadScenario(Config config, StreetGraph graph,
                                    Supplier<IVisualizationVehicle> vehicleFactory) {
        super(config,
                graph,
                vehicleFactory);
        nextScenarioState = NextScenarioState.PRIORITY_TO_THE_MOTORWAY;
    }

    @Override
    protected void updateScenarioState(VehicleState vehicleState) {

        int updateGraphDelay = justInitialized ? 0 : 1;

        if (vehicleState == VehicleState.DESPAWNED && getVehiclesCount() == 0) {
            switch (nextScenarioState) {
                case PRIORITY_TO_THE_MOTORWAY:
                    createAndAddCar(bottomMotorway, topMotorway, 0);
                    createAndAddCar(bottomRight, topMotorway, 9 + updateGraphDelay);
            }
        }
    }

    @Override
    protected void createAndAddVehicles() {
        /*
        bottom motorway microtrafficsim.core.map.Coordinate (48.883076, 9.144673)
        top motorway microtrafficsim.core.map.Coordinate (48.885483, 9.145947)
        interception microtrafficsim.core.map.Coordinate (48.884697, 9.145529)
        bottom right microtrafficsim.core.map.Coordinate (48.88397, 9.146404)
        */
        Iterator<Node> iter = graph.getNodeIterator();
        while (iter.hasNext()) {

            Node node = iter.next();
            Coordinate c = node.getCoordinate();
            // bottom motorway
            if (c.equals(new Coordinate(48.883076f, 9.144673f))) bottomMotorway = node;
            // interception
            else if (c.equals(new Coordinate(48.885483f, 9.145947f))) topMotorway = node;
            // top motorway
            else if (c.equals(new Coordinate(48.884697f, 9.145529f))) interception = node;
            // bottom right
            else if (c.equals(new Coordinate(48.88397f, 9.146404f))) bottomRight = node;
        }

        updateScenarioState(VehicleState.DESPAWNED);
        justInitialized = false;
    }
}