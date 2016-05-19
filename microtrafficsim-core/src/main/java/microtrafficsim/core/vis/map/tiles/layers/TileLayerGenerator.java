package microtrafficsim.core.vis.map.tiles.layers;

import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.math.Rect2d;


public interface TileLayerGenerator {
    TileLayer generate(RenderContext context, Layer layer, TileId tile, Rect2d target);
}
