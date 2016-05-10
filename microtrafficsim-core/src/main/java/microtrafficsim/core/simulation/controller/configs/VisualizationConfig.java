package microtrafficsim.core.simulation.controller.configs;

import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.core.vis.map.projections.Projection;

/**
 * This class isolates the visualization configs from the other config
 * parameters to guarantee better overview.
 * 
 * @author Dominic Parga Cacheiro
 */
public class VisualizationConfig {

    public Projection projection;

	{
        projection = new MercatorProjection(256); // tiles will be 512x512 pixel
	}
}