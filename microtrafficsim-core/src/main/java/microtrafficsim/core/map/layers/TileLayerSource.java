package microtrafficsim.core.map.layers;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.math.Rect2d;


public interface TileLayerSource {

    interface LayerSourceChangeListener {
        void sourceChanged(TileLayerSource source);
        void sourceChanged(TileLayerSource source, TileId tile);
    }


    Class<? extends TileLayerSource> getType();

    boolean isAvailable();

    Bounds getBounds();
    Rect2d getProjectedBounds();

    TilingScheme getTilingScheme();

    boolean addLayerSourceChangeListener(LayerSourceChangeListener listener);
    boolean removeLayerSourceChangeListener(LayerSourceChangeListener listener);
    boolean hasLayerSourceChangeListener(LayerSourceChangeListener listener);
}
