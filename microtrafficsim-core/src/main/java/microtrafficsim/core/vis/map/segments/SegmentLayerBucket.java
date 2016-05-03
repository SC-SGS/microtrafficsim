package microtrafficsim.core.vis.map.segments;

import microtrafficsim.core.vis.context.RenderContext;


public abstract class SegmentLayerBucket {
    public final SegmentLayer layer;
    public final float zIndex;

    public SegmentLayerBucket(SegmentLayer layer, float zIndex) {
        this.layer = layer;
        this.zIndex = zIndex;
    }

    public abstract void display(RenderContext context);
}
