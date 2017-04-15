package microtrafficsim.core.simulation.configs;


/**
 * This class isolates the scenario attributes from the other config
 * parameters to guarantee better overview.
 *
 * @author Dominic Parga Cacheiro
 */
public final class ScenarioConfig {
    public boolean showAreasWhileSimulating;

    /**
     * Just calls {@link #setup()}.
     */
    public ScenarioConfig() {
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
    }
}