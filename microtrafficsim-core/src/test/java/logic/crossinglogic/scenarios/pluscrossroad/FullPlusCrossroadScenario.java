package logic.crossinglogic.scenarios.pluscrossroad;

import microtrafficsim.core.logic.streetgraph.Graph;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.simulation.builder.LogicVehicleFactory;
import microtrafficsim.core.simulation.builder.impl.VehicleScenarioBuilder;
import microtrafficsim.core.simulation.builder.impl.VisVehicleFactory;
import microtrafficsim.core.simulation.configs.SimulationConfig;

/**
 * @author Dominic Parga Cacheiro
 */
public class FullPlusCrossroadScenario extends AbstractPlusCrossroadScenario {

    public FullPlusCrossroadScenario(SimulationConfig config, Graph graph, VisVehicleFactory visVehicleFactory) {
        super(config, graph, new VehicleScenarioBuilder(
                config.seed,
                (id, seed, scenario, metaRoute) -> {
                    Vehicle vehicle = LogicVehicleFactory.defaultCreation(id, seed, scenario, metaRoute);
                    vehicle.getDriver().setDawdleFactor(0);
                    return vehicle;
                },
                visVehicleFactory
        ));
    }

    /**
     * @return the given config updated; just for practical purpose
     */
    public static SimulationConfig setupConfig(SimulationConfig config) {

        AbstractPlusCrossroadScenario.setupConfig(config);

        config.maxVehicleCount                            = 2;
        config.crossingLogic.friendlyStandingInJamEnabled = true;

        return config;
    }

    @Override
    protected void init() {
        /* priority to the right/left */
        addPriorityToTheRight(topLeft);
        addPriorityToTheRight(bottomLeft);
        addPriorityToTheRight(bottomRight);
        addPriorityToTheRight(topRight);

        /* left/right turning */
        addPriorityTurning(topLeft);
        addPriorityTurning(bottomLeft);
        addPriorityTurning(bottomRight);
        addPriorityTurning(topRight);
    }
}
