package logic.old_validation;

import logic.old_validation.cars.ValidationCar;
import logic.old_validation.cars.ValidationBlockingCar;
import microtrafficsim.core.shortestpath.ShortestPathAlgorithm;
import microtrafficsim.core.shortestpath.astar.impl.LinearDistanceAStar;
import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.Node;
import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.AbstractVehicle;
import microtrafficsim.core.logic.vehicles.VehicleState;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.vis.opengl.utils.Color;

import java.util.function.Supplier;


/**
 * @author Dominic Parga Cacheiro
 */
public abstract class ValidationScenario extends AbstractSimulation {

    protected boolean               justInitialized;
    protected ShortestPathAlgorithm scout;

    /**
     * Default constructor.
     *
     * @param config         The used config file for this scenarios.
     * @param graph          The streetgraph used for this scenarios.
     * @param vehicleFactory This creates vehicles.
     */
    public ValidationScenario(SimulationConfig config, StreetGraph graph,
                              Supplier<VisualizationVehicleEntity> vehicleFactory) {
        super(config, graph, vehicleFactory);
        justInitialized = true;
        scout           = new LinearDistanceAStar(config.metersPerCell);
    }

    protected abstract void updateScenarioState(VehicleState vehicleState);

    protected final void createAndAddCar(Node start, Node end, int delay) {
        Route<Node> route = new Route<>(start, end);
        scout.findShortestPath(start, end, route);
        createAndAddVehicle(new ValidationCar(
                getConfig(), this, route,
                delay));
    }

    protected final void createAndAddCar(Node start, Node end, int delay, Color color) {
        Route<Node> route = new Route<>(start, end);
        scout.findShortestPath(start, end, route);
        createAndAddVehicle(
                new ValidationCar(getConfig(), this,
                                  route,
                                  delay),
                color);
    }

    protected final void createAndAddBlockingCar(Node start, Node end, Color color) {
        Route<Node> route = new Route<>(start, end);
        scout.findShortestPath(start, end, route);
        ValidationBlockingCar blockingCar = new ValidationBlockingCar(
                getConfig(), this, route);
        blockingCar.setBlockMode(true);
        createAndAddVehicle(blockingCar, color);
    }

    @Override
    protected void prepareScenario() {
        // empty
        System.out.println("asdfasdfasdfasdfasdfs");
        graph.getNodeIterator().forEachRemaining((Node node) -> System.out.println(node.getCoordinate()));
    }

    @Override
    public void stateChanged(AbstractVehicle vehicle) {
        super.stateChanged(vehicle);
        updateScenarioState(vehicle.getState());
    }
}