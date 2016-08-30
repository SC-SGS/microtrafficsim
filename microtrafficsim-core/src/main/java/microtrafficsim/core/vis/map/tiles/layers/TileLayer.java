package microtrafficsim.core.vis.map.tiles.layers;

import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.math.Mat4f;

import java.util.List;


/**
 * Layer for a tile.
 *
 * @author Maximilian Luz
 */
public abstract class TileLayer {
    private TileId tile;
    private Layer  layer;
    private Mat4f  transform;

    /**
     * Constructs a new {@code TileLayer}.
     *
     * @param tile      the tile-id for which this tile-layer is.
     * @param layer     the layer corresponding to this tile-layer.
     * @param transform the transformation-matrix to transform the tile to world-space.
     */
    public TileLayer(TileId tile, Layer layer, Mat4f transform) {
        this.tile      = tile;
        this.layer     = layer;
        this.transform = transform;
    }

    /**
     * Initializes this tile-layer.
     *
     * @param context the context on which this layer should be displayed.
     * @throws Exception if any exception occurs during initialization.
     */
    public abstract void initialize(RenderContext context) throws Exception;

    /**
     * Disposes this tile-layer.
     *
     * @param context the context on which this layer has been initialized.
     * @throws Exception if any exception occurs during disposal.
     */
    public abstract void dispose(RenderContext context) throws Exception;

    /**
     * Renders this tile-layer.
     *
     * @param context the context on which this tile-layer should be rendered.
     */
    public abstract void display(RenderContext context);

    /**
     * Returns all buckets of this tile-layer.
     *
     * @return the buckets of this tile-layer.
     */
    public abstract List<? extends TileLayerBucket> getBuckets();

    /**
     * Returns the id of this tile-layer.
     *
     * @return the id of this tile-layer.
     */
    public TileId getTile() {
        return tile;
    }

    /**
     * Returns the layer corresponding to this tile-layer.
     *
     * @return the layer corresponding to this tile-layer.
     */
    public Layer getLayer() {
        return layer;
    }

    /**
     * Returns the transform of this tile-layer.
     *
     * @return the transform of this tile-layer.
     */
    public Mat4f getTransform() {
        return transform;
    }
}
