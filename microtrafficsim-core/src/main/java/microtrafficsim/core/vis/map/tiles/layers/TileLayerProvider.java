package microtrafficsim.core.vis.map.tiles.layers;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.math.Rect2d;

import java.util.Set;


public interface TileLayerProvider {

    interface LayerChangeListener {
        void layersChanged();
        void layersChanged(TileId tile);
        void layerChanged(String name);
        void layerChanged(String name, TileId tile);
        void layerStateChanged(String name);
    }

    Bounds getBounds();
    Rect2d getProjectedBounds();

    Projection getProjection();
    TilingScheme getTilingScheme();

    Layer getLayer(String name);
    Set<String> getLayers();
    Set<String> getAvailableLayers();
    Set<String> getAvailableLayers(TileId tile);

    TileLayer require(RenderContext context, String layer, TileId tile, Rect2d target) throws InterruptedException;
    void release(RenderContext context, TileLayer layer);

    boolean addLayerChangeListener(LayerChangeListener listener);
    boolean removeLayerChangeListener(LayerChangeListener listener);
    boolean hasLayerChangeListener(LayerChangeListener listener);
}
