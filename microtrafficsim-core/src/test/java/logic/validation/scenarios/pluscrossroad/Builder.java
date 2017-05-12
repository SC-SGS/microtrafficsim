package logic.validation.scenarios.pluscrossroad;

import logic.validation.scenarios.VehicleQueueScenarioBuilder;
import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.routes.Route;
import microtrafficsim.core.logic.vehicles.machines.impl.BlockingCar;
import microtrafficsim.core.simulation.scenarios.Scenario;

import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
class Builder extends VehicleQueueScenarioBuilder {

    public Builder(long seed, Supplier<VisualizationVehicleEntity> visVehicleFactory) {
        super(seed, visVehicleFactory);
    }

    public Builder(long seed, Supplier<VisualizationVehicleEntity> visVehicleFactory, int maxVelocity) {
        super(seed, visVehicleFactory, maxVelocity);
    }

    @Override
    protected BlockingCar createLogicVehicle(Scenario scenario, Route route) {

        AbstractPlusCrossroadScenario plusCrossroadScenario = (AbstractPlusCrossroadScenario) scenario;

        BlockingCar vehicle = super.createLogicVehicle(scenario, route);
        if (route.getOrigin() == plusCrossroadScenario.mid)
            vehicle.toggleBlockMode();

        return vehicle;
    }
}