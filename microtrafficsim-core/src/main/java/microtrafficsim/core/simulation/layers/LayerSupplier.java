package microtrafficsim.core.simulation.layers;

import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.simulation.controller.configs.SimulationConfig;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.map.segments.SegmentLayerProvider;

import java.util.Set;

/**
 * @author Dominic Parga Cacheiro
 */
public interface LayerSupplier {

    OSMParser getParser(SimulationConfig config);

    /**
     * @return the layer definitions (precalculated!)
     */
    Set<LayerDefinition> getLayerDefinitions();

    SegmentLayerProvider getSegmentLayerProvider(Projection projection, Set<LayerDefinition> layers);
}