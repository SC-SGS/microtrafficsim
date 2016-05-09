package microtrafficsim.core.simulation.layers;

import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.map.segments.SegmentLayerProvider;

import java.util.Set;

/**
 * @author Dominic Parga Cacheiro
 */
public interface LayerSupplier {

    public OSMParser getParser();

    public Set<LayerDefinition> getLayerDefinitions();

    public SegmentLayerProvider getSegmentLayerProvider(Projection projection, Set<LayerDefinition> layers);
}