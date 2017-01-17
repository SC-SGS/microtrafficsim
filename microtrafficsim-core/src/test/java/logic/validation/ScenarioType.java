package logic.validation;

import logic.validation.scenarios.impl.MotorwaySlipRoadScenario;
import logic.validation.scenarios.impl.PlusCrossroadScenario;
import logic.validation.scenarios.impl.RoundaboutScenario;
import logic.validation.scenarios.impl.TCrossroadScenario;
import microtrafficsim.core.simulation.configs.ScenarioConfig;

import java.util.function.Function;

/**
 * Just a collection of important information, so the programmer has only to choose an enum value.
 *
 * @author Dominic Parga Cacheiro
 */
public enum ScenarioType {
    T_CROSSROAD("T_crossroad.osm", TCrossroadScenario::setupConfig),
    ROUNDABOUT("roundabout.osm", RoundaboutScenario::setupConfig),
    PLUS_CROSSROAD("plus_crossroad.osm", PlusCrossroadScenario::setupConfig),
    MOTORWAY_SLIP_ROAD("motorway_slip-road.osm", MotorwaySlipRoadScenario::setupConfig), ;

    private String osmFilename;
    private Function<ScenarioConfig, ScenarioConfig> configSetup;

    ScenarioType(String osmFilename, Function<ScenarioConfig, ScenarioConfig> configSetup) {
        this.osmFilename = osmFilename;
        this.configSetup = configSetup;
    }

    public String getOSMFilename() {
        return osmFilename;
    }

    public ScenarioConfig setupConfig(ScenarioConfig config) {
        return configSetup.apply(config);
    }
}