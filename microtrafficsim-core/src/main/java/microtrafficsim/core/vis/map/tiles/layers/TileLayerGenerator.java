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
     * Generates the requested tile grid.
     *
     * @param context the context for which the grid should be generated.
     * @param layer   the name of the grid that should be generated.
     * @param tile    the tile for which the grid should be generated.
     * @param target  the target to which the tile-grid should be projected.
     * @return the generated tile-grid.
     * @throws InterruptedException if this call has been interrupted.
     */
    TileLayer generate(RenderContext context, Layer layer, TileId tile, Rect2d target) throws InterruptedException;
}
