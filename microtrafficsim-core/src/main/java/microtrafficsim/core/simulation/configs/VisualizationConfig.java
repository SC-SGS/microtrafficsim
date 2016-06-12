package microtrafficsim.core.simulation.configs;

import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.core.vis.map.projections.Projection;

/**
 * This class isolates the visualization configs from the other config
 * parameters to guarantee better overview.
 * 
 * @author Dominic Parga Cacheiro
 */
public final class VisualizationConfig {
  public Projection projection;

  /**
   * Just calls {@link #reset()}.
   */
	public VisualizationConfig() {
        reset();
	}

  /**
   * Resets the parameter of this config file.
   */
  public void reset() {
      projection = new MercatorProjection(256); // tiles will be 512x512 pixel
  }

  /**
   * Updates the parameter of this config file.
   *
   * @param config All values of the new config instance are set to this config-values.
   */
  public void update(VisualizationConfig config) {
    projection = config.projection;
  }
}