package microtrafficsim.core.vis.map.tiles.layers;

import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.layers.LayerDefinition;
import microtrafficsim.core.map.layers.LayerSource;
import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.math.Rect2d;

import java.util.*;
import java.util.stream.Collectors;


public class LayeredTileMap implements TileLayerProvider {

    private TilingScheme scheme;
    private Projection   projection;

    private Rect2d bounds;

    private HashMap<String, Layer> layers;
    private HashMap<Class<? extends LayerSource>, TileLayerGenerator> generators;

    private LayerSource.LayerSourceChangeListener sourceListener;
    private Layer.LayerStateChangeListener            layerListener;
    private List<LayerChangeListener>                 listeners;


    public LayeredTileMap(TilingScheme scheme) {
        this.scheme     = scheme;
        this.projection = scheme.getProjection();

        this.bounds = null;

        this.layers     = new HashMap<>();
        this.generators = new HashMap<>();

        this.listeners      = new ArrayList<>();
        this.sourceListener = new LayerSourceChangeListenerImpl();
        this.layerListener  = new LayerStateChangeListenerImpl();
    }


    @Override
    public TilingScheme getTilingScheme() {
        return scheme;
    }

    @Override
    public Projection getProjection() {
        return scheme.getProjection();
    }


    @Override
    public Bounds getBounds() {
        return bounds != null ? projection.unproject(bounds) : null;
    }

    @Override
    public Rect2d getProjectedBounds() {
        return bounds;
    }


    public LayerDefinition addLayer(LayerDefinition def) {
        Layer layer = new Layer(def.getName(), def.getIndex(), def.getMinimumZoomLevel(), def.getMaximumZoomLevel(),
                                def.getSource());
        Layer old = layers.put(layer.getName(), layer);

        layer.getSource().addLayerSourceChangeListener(sourceListener);
        layer.addLayerStateChangeListener(layerListener);

        if (old != null) old.getSource().removeLayerSourceChangeListener(sourceListener);

        if (layer.getSource().isAvailable() || (old != null && old.getSource().isAvailable())) {
            updateBounds();

            for (LayerChangeListener l : listeners)
                l.layerChanged(layer.getName());
        }

        if (old == null)
            return null;
        else
            return new LayerDefinition(old.getName(), old.getIndex(), old.getMinimumZoomLevel(),
                                           old.getMaximumZoomLevel(), old.getSource());
    }

    public LayerDefinition removeLayer(String name) {
        Layer layer = layers.remove(name);

        if (layer != null) {
            layer.getSource().removeLayerSourceChangeListener(sourceListener);
            layer.removeLayerStateChangeListener(layerListener);

            if (layer.getSource().isAvailable()) {
                updateBounds();

                for (LayerChangeListener l : listeners)
                    l.layerChanged(name);
            }

            return new LayerDefinition(layer.getName(), layer.getIndex(), layer.getMinimumZoomLevel(),
                                           layer.getMaximumZoomLevel(), layer.getSource());
        } else {
            return null;
        }
    }


    public TileLayerGenerator putGenerator(Class<? extends LayerSource> source, TileLayerGenerator generator) {
        return generators.put(source, generator);
    }

    public TileLayerGenerator removeGenerator(Class<? extends LayerSource> source) {
        return generators.remove(source);
    }


    @Override
    public Set<String> getLayers() {
        return layers.keySet();
    }

    @Override
    public Set<String> getAvailableLayers() {
        return layers.values()
                .stream()
                .filter(d -> d.getSource().isAvailable())
                .map(Layer::getName)
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public Set<String> getAvailableLayers(TileId tile) {
        return layers.values()
                .stream()
                .filter(d -> d.isAvailableFor(tile))
                .filter(d -> d.getSource().isAvailable())
                .map(Layer::getName)
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public Layer getLayer(String name) {
        return layers.get(name);
    }


    @Override
    public TileLayer require(RenderContext context, String name, TileId tile, Rect2d target)
            throws InterruptedException {
        Layer layer = layers.get(name);
        if (layer == null) return null;

        TileLayerGenerator gen = generators.get(layer.getSource().getType());
        if (gen == null) return null;

        return gen.generate(context, layer, tile, target);
    }

    @Override
    public void release(RenderContext context, TileLayer layer) {}


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
        Rect2d max = null;

        for (Layer layer : layers.values()) {
            Rect2d b = layer.getSource().getProjectedBounds();
            if (b == null) continue;

            if (max != null) {
                if (b.xmin < max.xmin) max.xmin = b.xmin;
                if (b.ymin < max.ymin) max.ymin = b.ymin;
                if (b.xmax > max.xmax) max.xmax = b.xmax;
                if (b.ymax > max.ymax) max.ymax = b.ymax;
            } else {
                max = new Rect2d(b);
            }
        }

        this.bounds = max;
    }


    private class LayerSourceChangeListenerImpl implements LayerSource.LayerSourceChangeListener {

        @Override
        public void sourceChanged(LayerSource source) {
            updateBounds();

            HashSet<String> changed = layers.values()
                    .stream()
                    .filter(d -> d.getSource().equals(source))
                    .map(Layer::getName)
                    .collect(Collectors.toCollection(HashSet::new));

            listeners.forEach(listener -> changed.forEach(listener::layerChanged));
        }

        @Override
        public void sourceChanged(LayerSource source, TileId tile) {
            updateBounds();

            HashSet<String> changed = layers.values()
                    .stream()
                    .filter(d -> d.getSource().equals(source))
                    .map(Layer::getName)
                    .collect(Collectors.toCollection(HashSet::new));

            listeners.forEach(listener -> changed.forEach(layer -> listener.layerChanged(layer, tile)));
        }
    }

    private class LayerStateChangeListenerImpl implements Layer.LayerStateChangeListener {
        @Override
        public void layerStateChanged(String name) {
            listeners.forEach(listener -> listener.layerStateChanged(name));
        }
    }
}
