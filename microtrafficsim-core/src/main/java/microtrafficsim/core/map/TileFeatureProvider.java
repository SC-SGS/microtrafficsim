package microtrafficsim.core.map;

import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.math.Rect2d;

import java.util.Set;


public interface TileFeatureProvider {

    interface FeatureChangeListener {
        void featuresChanged();
        void featuresChanged(TileId tile);
        void featureChanged(String name);
        void featureChanged(String name, TileId tile);
    }

    TilingScheme getTilingScheme();

    Bounds getBounds();
    Rect2d getProjectedBounds();

    Class<? extends FeaturePrimitive> getFeatureType(String name);

    TileRect getFeatureBounds(String name, TileId tile);
    TileRect getFeatureBounds(String name, TileRect bounds);

    <T extends FeaturePrimitive> TileFeature<T> require(String name, TileId tile) throws InterruptedException;
    <T extends FeaturePrimitive> TileFeature<T> require(String name, TileRect bounds) throws InterruptedException;
    void release(TileFeature<?> feature);

    Set<String> getAvailableFeatures();
    boolean hasFeature(String name);
    boolean hasTile(int x, int y, int z);

    boolean addFeatureChangeListener(FeatureChangeListener listener);
    boolean removeFeatureChangeListener(FeatureChangeListener listener);
    boolean hasFeatureChangeListener(FeatureChangeListener listener);
}
