package microtrafficsim.core.vis.map.tiles.layers;

import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.math.Rect2d;


/**
 * Generator for {@code TileLayer}s.
 *
 * @author Maximilian Luz
 */
public interface TileLayerGenerator {

    /**
     * Generates the requested tile layer.
     *
     * @param context the context for which the layer should be generated.
     * @param layer   the name of the layer that should be generated.
     * @param tile    the tile for which the layer should be generated.
     * @param target  the target to which the tile-layer should be projected.
     * @return the generated tile-layer.
     * @throws InterruptedException if this call has been interrupted.
     */
    TileLayer generate(RenderContext context, Layer layer, TileId tile, Rect2d target) throws InterruptedException;
}
