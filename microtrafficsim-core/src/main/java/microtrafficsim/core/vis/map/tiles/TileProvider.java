package microtrafficsim.core.vis.map.tiles;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.math.Rect2d;

import java.util.concurrent.ExecutionException;


public interface TileProvider {

    interface TileChangeListener {
        void tilesChanged();
        void tileChanged(TileId tile);
    }

    Bounds getBounds();
    Rect2d getProjectedBounds();

    Projection getProjection();
    TilingScheme getTilingScheme();

    void initialize(RenderContext context);
    void dispose(RenderContext context);

    Tile require(RenderContext context, TileId tile) throws InterruptedException, ExecutionException;
    void release(RenderContext context, Tile tile);

    boolean addTileChangeListener(TileChangeListener listener);
    boolean removeTileChangeListener(TileChangeListener listener);
    boolean hasTileChangeListener(TileChangeListener listener);
}
