package logic.validation.scenarios.pluscrossroad;

import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.logic.vehicles.machines.Vehicle;
import microtrafficsim.core.logic.vehicles.machines.impl.BlockingCar;
import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.utils.Tuple;
import microtrafficsim.utils.collections.Triple;

import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public class PartialPlusCrossroadScenario extends AbstractPlusCrossroadScenario {

    public PartialPlusCrossroadScenario(ScenarioConfig config,
                                        StreetGraph graph,
                                        Supplier<VisualizationVehicleEntity> visVehicleFactory) {
        super(config, graph, visVehicleFactory);
    }

    /**
     * @param config
     * @return the given config updated; just for practical purpose
     */
    public static ScenarioConfig setupConfig(ScenarioConfig config) {

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
                new Tuple<>(bottomLeft,  topLeft),
                new Tuple<>(bottomRight, bottomLeft),
                new Tuple<>(topRight,    bottomRight),
                new Tuple<>(topLeft,     topRight)
        );

        /* go without priority / friendly standing in jam */
        addSubScenario(
                new Triple<>(mid,         bottomLeft, 0),
                new Triple<>(topRight,    bottomLeft, null),
                new Triple<>(bottomRight, topLeft,    null)
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