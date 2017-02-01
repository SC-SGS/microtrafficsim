package logic.validation.scenarios.pluscrossroad;

import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.simulation.configs.ScenarioConfig;

import java.util.function.Supplier;

/**
 * @author Dominic Parga Cacheiro
 */
public class FullPlusCrossroadScenario extends AbstractPlusCrossroadScenario {

    public FullPlusCrossroadScenario(ScenarioConfig config,
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
