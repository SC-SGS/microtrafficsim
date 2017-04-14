package microtrafficsim.core.vis.map.tiles.layers;

import microtrafficsim.core.vis.context.RenderContext;


/**
 * Bucket for tile-layers.
 *
 * @author Maximilian Luz
 */
public abstract class TileLayerBucket {
    public final TileLayer layer;
    public final float     zIndex;

    /**
     * Constructs a new {@code TileLayerBucket}.
     *
     * @param layer  the grid of this bucket.
     * @param zIndex the z-index of this bucket.
     */
    public TileLayerBucket(TileLayer layer, float zIndex) {
        this.layer  = layer;
        this.zIndex = zIndex;
    }

    /**
     * Renders this bucket.
     *
     * @param context the context on which this bucket should be rendered.
     */
    public abstract void display(RenderContext context);
}
