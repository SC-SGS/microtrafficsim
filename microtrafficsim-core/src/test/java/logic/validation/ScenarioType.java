package logic.validation;

import logic.validation.scenarios.impl.TCrossroadScenario;
import microtrafficsim.core.simulation.configs.SimulationConfig;

import java.util.function.Function;

/**
 * @author Dominic Parga Cacheiro
 */
public enum ScenarioType {
    T_CROSSROAD("T_crossroad.osm", TCrossroadScenario::setupConfig),
    ROUNDABOUT("", null),
    PLUS_CROSSROAD("", null),
    MOTORWAY_SLIP_ROAD("", null);

    private String osmFilename;
    private Function<SimulationConfig, SimulationConfig> configSetup;

    ScenarioType(String osmFilename, Function<SimulationConfig, SimulationConfig> configSetup) {
        this.osmFilename = osmFilename;
        this.configSetup = configSetup;
    }

    public String getOSMFilename() {
        return osmFilename;
    }

    public SimulationConfig setupConfig(SimulationConfig config) {
        return configSetup.apply(config);
    }
}