package microtrafficsim.core.simulation.controller.configs;

import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.utils.valuewrapper.LazyFinalValue;

/**
 * This class isolates the visualization configs from the other config
 * parameters to guarantee better overview.
 * 
 * @author Dominic Parga Cacheiro
 */
public final class VisualizationConfig {

    private LazyFinalValue<Projection> projection;

	public VisualizationConfig() {
        reset();
	}

    void reset() {
        projection = new LazyFinalValue<>(new MercatorProjection(256)); // tiles will be 512x512 pixel
    }

    void reset(VisualizationConfig config) {
        projection = new LazyFinalValue<>(config.projection.get());
    }

    public LazyFinalValue<Projection> projection() { return projection; }
}