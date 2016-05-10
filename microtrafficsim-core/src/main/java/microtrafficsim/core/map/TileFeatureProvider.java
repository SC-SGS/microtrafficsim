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

    <T extends FeaturePrimitive> TileFeature<T> require(String name, TileId tile);
    <T extends FeaturePrimitive> TileFeature<T> require(String name, TileRect bounds);
    void release(TileFeature<?> feature);

    Set<String> getAvailableFeatures();
    boolean hasFeature(String name);
    boolean hasTile(int x, int y, int z);

    boolean addFeatureChangeListener(FeatureChangeListener listener);
    boolean removeFeatureChangeListener(FeatureChangeListener listener);
    boolean hasFeatureChangeListener(FeatureChangeListener listener);
}
