package microtrafficsim.core.simulation.builder;

import microtrafficsim.core.logic.routes.Route;
import microtrafficsim.core.logic.vehicles.driver.BasicDriver;
import microtrafficsim.core.logic.vehicles.driver.Driver;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.logic.vehicles.machines.impl.Car;
import microtrafficsim.core.logic.vehicles.machines.impl.MonitoredCar;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.scenarios.Scenario;

/**
 * @author Dominic Parga Cacheiro
 */
public interface LogicVehicleFactory {
    Vehicle create(long id, long seed, Scenario scenario, Route metaRoute);

    static Vehicle defaultCreation(long id, long seed, Scenario scenario, Route metaRoute) {
        SimulationConfig config = scenario.getConfig();

        Vehicle vehicle;
        if (metaRoute.isMonitored()) {
            vehicle = new MonitoredCar(id, config.visualization.style);
        } else {
            vehicle = new Car(id, config.visualization.style);
        }
        BasicDriver.InitSetup setup = new BasicDriver.InitSetup(seed);
        setup.spawnDelay = metaRoute.getSpawnDelay();
        Driver driver = new BasicDriver(setup);
        driver.setRoute(metaRoute);
        driver.setVehicle(vehicle);
        vehicle.setDriver(driver);

        vehicle.addStateListener(scenario.getVehicleContainer());

        return vehicle;
    }
}
