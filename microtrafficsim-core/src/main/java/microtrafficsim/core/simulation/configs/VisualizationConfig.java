package microtrafficsim.core.simulation.configs;

import microtrafficsim.core.map.style.StyleSheet;
import microtrafficsim.core.map.style.impl.DarkStyleSheet;
import microtrafficsim.utils.Resettable;

/**
 * This class isolates the visualization configs from the other config
 * parameters to guarantee better overview.
 *
 * @author Dominic Parga Cacheiro
 */
public final class VisualizationConfig implements Resettable {

    public StyleSheet style;

    /**
     * Just calls {@link #reset()}.
     */
    public VisualizationConfig() {
        reset();
    }

    /**
     * Resets the parameter of this config file.
     */
    @Override
    public void reset() {
        style = new DarkStyleSheet();
    }

    /**
     * Updates the parameter of this config file.
     *
     * @param config All values of the new config instance are set to this config-values.
     */
    public void update(VisualizationConfig config) {
        style = config.style;
    }
}