package microtrafficsim.core.vis.map.tiles.layers;

import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.math.Mat4f;

import java.util.List;


public abstract class TileLayer {
    private TileId tile;
    private Layer  layer;
    private Mat4f  transform;

    public TileLayer(TileId tile, Layer layer, Mat4f transform) {
        this.tile      = tile;
        this.layer     = layer;
        this.transform = transform;
    }


    public abstract void initialize(RenderContext context);
    public abstract void dispose(RenderContext context);
    public abstract void display(RenderContext context);

    public abstract List<? extends TileLayerBucket> getBuckets();


    public TileId getTile() {
        return tile;
    }
    public Layer getLayer() {
        return layer;
    }

    public Mat4f getTransform() {
        return transform;
    }
}
