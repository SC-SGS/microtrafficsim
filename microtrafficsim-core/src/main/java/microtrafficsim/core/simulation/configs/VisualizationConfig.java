package microtrafficsim.core.simulation.configs;

import microtrafficsim.core.map.style.StyleSheet;
import microtrafficsim.core.map.style.impl.DarkMonochromeStyleSheet;


/**
 * This class isolates the visualization configs from the other config
 * parameters to guarantee better overview.
 *
 * @author Dominic Parga Cacheiro
 */
public final class VisualizationConfig {

    public StyleSheet style;
    public boolean showConnectorOverlay;

    /**
     * Just calls {@link #setup()}.
     */
    public VisualizationConfig() {
        setup();
    }

    /**
     * Resets the parameter of this config file.
     */
    public void setup() {
        style = new DarkMonochromeStyleSheet();
        showConnectorOverlay = false;
    }

    /**
     * Updates the parameter of this config file.
     *
     * @param config All values of the new config instance are set to this config-values.
     */
    public void update(VisualizationConfig config) {
        style = config.style;
        showConnectorOverlay = config.showConnectorOverlay;
    }
}