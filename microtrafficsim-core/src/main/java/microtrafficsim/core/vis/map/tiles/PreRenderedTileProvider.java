package microtrafficsim.core.vis.map.tiles;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.map.tiles.layers.TileLayerProvider;
import microtrafficsim.math.Rect2d;


public class PreRenderedTileProvider implements TileProvider {
    // TODO: sync load tile and pre-render, handle init/dispose etc.
    // TODO: implement basic caching / resource re-using

    private TileLayerProvider provider;


    @Override
    public Bounds getBounds() {
        return null;    // TODO
    }

    @Override
    public Rect2d getProjectedBounds() {
        return null;    // TODO
    }

    @Override
    public Projection getProjection() {
        return null;    // TODO
    }

    @Override
    public TilingScheme getTilingScheme() {
        return null;    // TODO
    }


    @Override
    public void initialize(RenderContext context) {
        // TODO
    }

    @Override
    public void dispose(RenderContext context) {
        // TODO
    }


    @Override
    public Tile require(RenderContext context, TileId tile) {
        return null;    // TODO
        // NOTE: make exception-safe (interrupt)
    }

    @Override
    public void release(RenderContext context, Tile tile) {
        // TODO
    }


    @Override
    public boolean addTileChangeListener(TileChangeListener listener) {
        return false;   // TODO
    }

    @Override
    public boolean removeTileChangeListener(TileChangeListener listener) {
        return false;   // TODO
    }

    @Override
    public boolean hasTileChangeListener(TileChangeListener listener) {
        return false;   // TODO
    }
}
