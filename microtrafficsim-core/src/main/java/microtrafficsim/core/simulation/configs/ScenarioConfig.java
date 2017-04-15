package microtrafficsim.core.simulation.configs;


import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.simulation.scenarios.impl.RandomRouteScenario;

import java.util.ArrayList;

/**
 * This class isolates the scenario attributes from the other config
 * parameters to guarantee better overview.
 *
 * @author Dominic Parga Cacheiro
 */
public final class ScenarioConfig {
    public boolean showAreasWhileSimulating;
    public final ArrayList<Class<? extends Scenario>> classes;
    public Class<? extends Scenario> selectedClass;

    /**
     * Just calls {@link #setup()}.
     */
    public ScenarioConfig() {
        classes = new ArrayList<>();
        setup();
    }

    /**
     * Setup the parameters of this config file.
     */
    public void setup() {
        showAreasWhileSimulating = false;
    }

    /**
     * Updates the parameter of this config file.
     *
     * @param config All values of the new config instance are set to this config-values.
     */
    public void update(ScenarioConfig config) {
        showAreasWhileSimulating = config.showAreasWhileSimulating;

        classes.clear();
        classes.addAll(config.classes);

        selectedClass = config.selectedClass;
    }
}