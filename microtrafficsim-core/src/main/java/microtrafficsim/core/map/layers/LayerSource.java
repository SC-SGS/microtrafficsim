package microtrafficsim.core.map.layers;

import microtrafficsim.core.map.Bounds;


public interface LayerSource {
    Class<? extends LayerSource> getType();

    boolean isAvailable();

    Bounds getBounds();

    boolean addLayerSourceChangeListener(LayerSourceChangeListener listener);
    boolean removeLayerSourceChangeListener(LayerSourceChangeListener listener);
    boolean hasLayerSourceChangeListener(LayerSourceChangeListener listener);

    interface LayerSourceChangeListener {
        void sourceChanged(LayerSource src);
    }
}
