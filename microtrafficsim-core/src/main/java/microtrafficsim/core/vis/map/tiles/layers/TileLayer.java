package microtrafficsim.core.vis.map.tiles.layers;

import microtrafficsim.core.map.layers.TileLayerSource;
import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.vis.context.RenderContext;

import java.util.List;


public abstract class TileLayer {
    private TileId tile;
    private Layer layer;

    public TileLayer(TileId tile, Layer layer, TileLayerSource source) {
        this.tile = tile;
        this.layer = layer;
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
}
