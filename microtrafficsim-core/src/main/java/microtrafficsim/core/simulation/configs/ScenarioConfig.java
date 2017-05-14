package microtrafficsim.core.simulation.configs;

import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.utils.Descriptor;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * This class isolates the scenario attributes from the other config
 * parameters to guarantee better overview.
 *
 * @author Dominic Parga Cacheiro
 */
public final class ScenarioConfig {
    public boolean showAreasWhileSimulating;
    public boolean nodesAreWeightedUniformly;
    public final HashMap<Class<? extends Scenario>, Descriptor<Class<? extends Scenario>>> supportedClasses;
    public Descriptor<Class<? extends Scenario>> selectedClass;

    /**
     * Just calls {@link #setup()}.
     */
    public ScenarioConfig() {
        supportedClasses = new HashMap<>();
        setup();
    }

    /**
     * Setup the parameters of this config file.
     */
    private void setup() {
        showAreasWhileSimulating = false;
        nodesAreWeightedUniformly = true;
    }

    /**
     * Updates the parameter of this config file.
     *
     * @param config All values of the new config instance are set to this config-values.
     */
    public void update(ScenarioConfig config) {
        showAreasWhileSimulating = config.showAreasWhileSimulating;
        nodesAreWeightedUniformly = config.nodesAreWeightedUniformly;

        supportedClasses.clear();
        supportedClasses.putAll(config.supportedClasses);

        selectedClass = config.selectedClass;
    }
}