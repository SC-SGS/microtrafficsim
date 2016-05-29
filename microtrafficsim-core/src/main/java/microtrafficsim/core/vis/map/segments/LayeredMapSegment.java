package microtrafficsim.core.vis.map.segments;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.map.layers.LayerSource;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.math.Rect2d;

import java.util.*;
import java.util.stream.Collectors;


public class LayeredMapSegment implements SegmentLayerProvider {

	private Projection projection;
	private Bounds bounds;

	private HashMap<String, LayerDefinition> layers;
	private HashMap<Class<? extends LayerSource>, SegmentLayerGenerator> generators;

	private LayerSource.LayerSourceChangeListener sourceListener;
	private List<LayerChangeListener> listeners;
	
	
	public LayeredMapSegment(Projection projection) {
		this.projection = projection;
		this.bounds = null;
		
		this.layers = new HashMap<>();
		this.generators = new HashMap<>();
		
		this.listeners = new ArrayList<>();
		this.sourceListener = new LayerSourceChangeListenerImpl();
	}
	
	
	@Override
	public void setProjection(Projection projection) {
		this.projection = projection;
		
		// notify listeners
		listeners.forEach(LayerChangeListener::segmentChanged);
	}
	
	@Override
	public Projection getProjection() {
		return projection;
	}


    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public Rect2d getProjectedBounds() {
        return (bounds != null) ? projection.project(bounds) : null;
    }

	
	public LayerDefinition addLayer(LayerDefinition layer) {
		LayerDefinition old = layers.put(layer.getName(), layer);
		
		layer.getSource().addLayerSourceChangeListener(sourceListener);
		
		if (old != null)
			old.getSource().removeLayerSourceChangeListener(sourceListener);
		
		if (layer.getSource().isAvailable() || (old != null && old.getSource().isAvailable())) {
			updateBounds();
			
			for (LayerChangeListener l : listeners)
				l.layerChanged(layer.getName());
		}
		
		return old;
	}
	
	public LayerDefinition removeLayer(String name) {
		LayerDefinition def = layers.remove(name);
		
		if (def != null) {
			def.getSource().removeLayerSourceChangeListener(sourceListener);
			
			if (def.getSource().isAvailable()) {
				updateBounds();
				
				for (LayerChangeListener l : listeners)
					l.layerChanged(name);
			}
		}
		
		return def;
	}
	
	
	public SegmentLayerGenerator putGenerator(Class<? extends LayerSource> source, SegmentLayerGenerator generator) {
		return generators.put(source, generator);
	}
	
	public SegmentLayerGenerator removeGenerator(Class<? extends LayerSource> type) {
		return generators.remove(type);
	}


	@Override
	public Set<String> getLayers() {
		return layers.keySet();
	}

	@Override
	public Set<String> getAvailableLayers() {
		return layers.values().stream()
				.filter(d -> d.getSource().isAvailable())
				.map(LayerDefinition::getName)
				.collect(Collectors.toCollection(HashSet::new));
	}

	
	@Override
	public SegmentLayer require(RenderContext context, String layer) throws InterruptedException {
		LayerDefinition def = layers.get(layer);
		if (def == null) return null;
		
		SegmentLayerGenerator gen = generators.get(def.getSource().getType());
		if (gen == null) return null;
		
		return gen.generate(context, def, projection);
	}

	@Override
	public void release(SegmentLayer layer) {}


	@Override
	public boolean addLayerChangeListener(LayerChangeListener listener) {
		return listeners.add(listener);
	}

	@Override
	public boolean removeLayerChangeListener(LayerChangeListener listener) {
		return listeners.remove(listener);
	}

	@Override
	public boolean hasLayerChangeListener(LayerChangeListener listener) {
		return listeners.contains(listener);
	}


    private void updateBounds() {
        Bounds max = null;

        for (LayerDefinition def : layers.values()) {
            Bounds b = def.getSource().getBounds();
            if (b == null) continue;

            if (max != null) {
                if (b.minlat < max.minlat) max.minlat = b.minlat;
                if (b.minlon < max.minlon) max.minlon = b.minlon;
                if (b.maxlat > max.maxlat) max.maxlat = b.maxlat;
                if (b.maxlon > max.maxlon) max.maxlon = b.maxlon;
            } else {
                max = new Bounds(b);
            }
        }

        this.bounds = max;
    }


	private class LayerSourceChangeListenerImpl implements LayerSource.LayerSourceChangeListener {

		@Override
		public void sourceChanged(LayerSource src) {
			updateBounds();

			HashSet<String> changed = layers.values().stream()
					.filter(d -> d.getSource().equals(src))
					.map(LayerDefinition::getName)
					.collect(Collectors.toCollection(HashSet::new));

			listeners.forEach(s -> changed.forEach(s::layerChanged));
		}
	}
}
