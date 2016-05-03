package logic.validation;

import microtrafficsim.core.frameworks.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.frameworks.shortestpath.astar.impl.LinearDistanceAStar;
import microtrafficsim.core.frameworks.vehicle.IVisualizationVehicle;
import microtrafficsim.core.logic.DirectedEdge;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.logic.vehicles.impl.BlockingCar;
import microtrafficsim.core.logic.vehicles.impl.Car;
import microtrafficsim.core.simulation.controller.AbstractSimulation;
import microtrafficsim.core.simulation.controller.configs.SimulationConfig;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.utils.id.ConcurrentLongIDGenerator;

import java.util.Queue;
import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public abstract class ValidationScenario extends AbstractSimulation {

    public static class Config extends SimulationConfig {

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
            crossingLogic.goWithoutPriorityEnabled = false;
            // own attributes
            ageForPause = -1;
        }
    }

    protected boolean justInitialized;
    protected ShortestPathAlgorithm scout;

    /**
     * Default constructor.
     *
     * @param config         The used config file for this scenarios.
     * @param graph          The streetgraph used for this scenarios.
     * @param vehicleFactory This creates vehicles.
     */
    public ValidationScenario(Config config, StreetGraph graph,
                              Supplier<IVisualizationVehicle> vehicleFactory) {
        super(config,
                graph,
                vehicleFactory);
        justInitialized = true;
        scout = new LinearDistanceAStar(config.metersPerCell);
    }

    protected abstract void updateScenarioState(VehicleState vehicleState);

    protected final void createAndAddCar(Node start, Node end, int delay) {
        createAndAddVehicle(
                new Car(config.longIDGenerator,
                        this, new Route(start, end,
                        (Queue<DirectedEdge>) scout.findShortestPath(start, end)),
                        delay
                ));
    }

    protected final void createAndAddCar(Node start, Node end, int delay, Color color) {
        createAndAddVehicle(
                new Car(config.longIDGenerator,
                        this, new Route(start, end,
                        (Queue<DirectedEdge>) scout.findShortestPath(start, end)),
                        delay
                ),
                color
        );
    }

    protected final void createAndAddBlockingCar(Node start, Node end, Color color) {
        BlockingCar blockingCar = new BlockingCar(
                config.longIDGenerator,
                this,
                new Route(start, end, (Queue<DirectedEdge>) scout.findShortestPath(start, end))
        );
        blockingCar.setBlockMode(true);
        createAndAddVehicle(blockingCar, color);
    }

    @Override
    protected void prepareScenario() {
        // empty
        graph.getNodeIterator().forEachRemaining((Node node) -> System.out.println(node.getCoordinate()));
    }

    @Override
    public void stateChanged(AbstractVehicle vehicle) {
        super.stateChanged(vehicle);
        updateScenarioState(vehicle.getState());
    }
}