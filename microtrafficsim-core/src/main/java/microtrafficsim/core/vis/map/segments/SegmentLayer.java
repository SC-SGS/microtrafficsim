package microtrafficsim.core.vis.map.segments;

import microtrafficsim.core.map.layers.LayerSource;
import microtrafficsim.core.vis.context.RenderContext;

import java.util.List;


public abstract class SegmentLayer extends Layer {

    public SegmentLayer(String name, int index, LayerSource source) {
        super(name, index, source);
    }

    public abstract void initialize(RenderContext context);
    public abstract void dispose(RenderContext context);
    public abstract void display(RenderContext context);

    public abstract List<? extends SegmentLayerBucket> getBuckets();
}
