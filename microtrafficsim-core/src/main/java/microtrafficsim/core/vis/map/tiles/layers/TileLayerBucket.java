package microtrafficsim.core.vis.map.tiles.layers;

import microtrafficsim.core.vis.context.RenderContext;


public abstract class TileLayerBucket {
    public final TileLayer layer;
    public final float     zIndex;

    public TileLayerBucket(TileLayer layer, float zIndex) {
        this.layer  = layer;
        this.zIndex = zIndex;
    }

    public abstract void display(RenderContext context);
}
