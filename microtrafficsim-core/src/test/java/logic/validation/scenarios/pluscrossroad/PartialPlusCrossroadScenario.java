package logic.validation.scenarios.pluscrossroad;

import microtrafficsim.core.logic.routes.MetaRoute;
import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.vehicles.driver.BasicDriver;
import microtrafficsim.core.logic.vehicles.driver.Driver;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.logic.vehicles.machines.impl.BlockingCar;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.builder.impl.VisVehicleFactory;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;

/**
 * @author Dominic Parga Cacheiro
 */
public class PartialPlusCrossroadScenario extends AbstractPlusCrossroadScenario {
    public PartialPlusCrossroadScenario(SimulationConfig config,
                                        Graph graph,
                                        VisVehicleFactory visVehicleFactory) {
        super(config, graph, new VehicleScenarioBuilder(
                config.seed,
                (id, seed, scenario, metaRoute) -> {
                    BlockingCar vehicle = new BlockingCar(id, scenario.getConfig().visualization.style);
                    Driver driver = new BasicDriver(seed, 0, metaRoute.getSpawnDelay());
                    driver.setRoute(metaRoute.clone());
                    driver.setVehicle(vehicle);
                    vehicle.setDriver(driver);

                    vehicle.addStateListener(scenario.getVehicleContainer());
                    return vehicle;
                },
                visVehicleFactory
        ));
    }

    /**
     * @param config
     * @return the given config updated; just for practical purpose
     */
    public static SimulationConfig setupConfig(SimulationConfig config) {
        AbstractPlusCrossroadScenario.setupConfig(config);

        config.maxVehicleCount                            = 4;
        config.crossingLogic.friendlyStandingInJamEnabled = true;

        return config;
    }


    @Override
    protected void init() {
        /* priority to the right */
        addOneTurning(topLeft, topRight, true);

        /* no intersection */
        addBothStraight(topRight, bottomLeft);

        /* left turner must wait */
        addOneTurning(topLeft, bottomRight, true);

        /* all turn left */
        addSubScenario(
                new MetaRoute(bottomLeft,  topLeft,     -1),
                new MetaRoute(bottomRight, bottomLeft,  -1),
                new MetaRoute(topRight,    bottomRight, -1),
                new MetaRoute(topLeft,     topRight,    -1)
        );

        /* go without priority / friendly standing in jam */
        addSubScenario(
                new MetaRoute(mid,         bottomLeft, 4),
                new MetaRoute(mid,         bottomLeft, 4),
                new MetaRoute(mid,         bottomLeft, 4),
                new MetaRoute(mid,         bottomLeft, 4),

                new MetaRoute(topRight,    bottomLeft, 0),
                new MetaRoute(topRight,    bottomLeft, 0),

                new MetaRoute(bottomRight, topLeft,    0)
        );
    }


    @Override
    public void didOneStep(Simulation simulation) {
        if (getVehicleContainer().getVehicleCount() == 2) {
            for (Vehicle vehicle : getVehicleContainer().getVehicles()) {
                BlockingCar blockingCar = (BlockingCar) vehicle;
                if (blockingCar.isBlocking())
                    blockingCar.toggleBlockMode();
            }
        }

        super.didOneStep(simulation);
    }
}