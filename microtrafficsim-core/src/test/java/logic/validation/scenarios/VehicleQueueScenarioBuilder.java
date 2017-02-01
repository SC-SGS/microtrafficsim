package logic.validation.scenarios;

import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.Route;
import microtrafficsim.core.logic.nodes.Node;
import microtrafficsim.core.logic.vehicles.impl.BlockingCar;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.simulation.scenarios.impl.QueueScenarioSmall;

import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public class VehicleQueueScenarioBuilder extends VehicleScenarioBuilder {

    public VehicleQueueScenarioBuilder(long seed, Supplier<VisualizationVehicleEntity> visVehicleFactory) {
        super(seed, visVehicleFactory);
    }

    @Override
    protected BlockingCar createLogicVehicle(Scenario scenario, Route<Node> route) {

        QueueScenarioSmall queueScenario = (QueueScenarioSmall) scenario;

        long id        = idGenerator.next();
        long seed      = seedGenerator.next();
        int spawnDelay = queueScenario.getSpawnDelayMatrix().get(route.getStart(), route.getEnd());

        BlockingCar vehicle = new BlockingCar(id, seed, route, spawnDelay, scenario.getConfig().visualization.style);
        vehicle.addStateListener(scenario.getVehicleContainer());

        return vehicle;
    }
}