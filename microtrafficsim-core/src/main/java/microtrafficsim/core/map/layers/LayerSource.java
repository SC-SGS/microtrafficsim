package microtrafficsim.core.map.layers;

import microtrafficsim.core.map.Bounds;


public interface LayerSource {
	
	interface LayerSourceChangeListener {
		void sourceChanged(LayerSource src);
	}


	Class<? extends LayerSource> getType();

	boolean isAvailable();

	Bounds getBounds();

	boolean addLayerSourceChangeListener(LayerSourceChangeListener listener);
	boolean removeLayerSourceChangeListener(LayerSourceChangeListener listener);
	boolean hasLayerSourceChangeListener(LayerSourceChangeListener listener);
}
