package logic.validation.scenarios;

import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.logic.vehicles.driver.BasicDriver;
import microtrafficsim.core.logic.vehicles.driver.Driver;
import microtrafficsim.core.logic.vehicles.machines.impl.BlockingCar;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.simulation.scenarios.impl.QueueScenarioSmall;

import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public class VehicleQueueScenarioBuilder extends VehicleScenarioBuilder {

    private final Integer maxVelocity;

    public VehicleQueueScenarioBuilder(long seed, Supplier<VisualizationVehicleEntity> visVehicleFactory) {
        super(seed, visVehicleFactory);
        maxVelocity = null;
    }

    public VehicleQueueScenarioBuilder(long seed,
                                       Supplier<VisualizationVehicleEntity> visVehicleFactory,
                                       int maxVelocity) {
        super(seed, visVehicleFactory);
        this.maxVelocity = maxVelocity;
    }

    @Override
    protected BlockingCar createLogicVehicle(Scenario scenario, Route<Node> route) {

        QueueScenarioSmall queueScenario = (QueueScenarioSmall) scenario;

        long id        = idGenerator.next();
        long seed      = seedGenerator.next();
        int spawnDelay = queueScenario.getSpawnDelayMatrix().get(route.getStart(), route.getEnd());

        BlockingCar vehicle;
        if (maxVelocity == null)
            vehicle = new BlockingCar(id, scenario.getConfig().visualization.style);
        else
            vehicle = new BlockingCar(id, maxVelocity, scenario.getConfig().visualization.style);
        Driver driver = new BasicDriver(seed, 0, spawnDelay);
        driver.setRoute(route);
        driver.setVehicle(vehicle);
        vehicle.setDriver(driver);
        vehicle.addStateListener(scenario.getVehicleContainer());

        return vehicle;
    }
}