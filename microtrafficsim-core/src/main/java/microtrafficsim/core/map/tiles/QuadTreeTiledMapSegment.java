package microtrafficsim.core.map.tiles;

import microtrafficsim.core.map.*;
import microtrafficsim.core.map.features.MultiLine;
import microtrafficsim.core.map.features.Point;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.math.Rect2d;

import java.lang.reflect.Array;
import java.util.*;


public class QuadTreeTiledMapSegment implements TileFeatureProvider, SegmentFeatureProvider {

    public static class Generator {

        private HashMap<Class<? extends FeaturePrimitive>, TileIntersector<? extends FeaturePrimitive>> intersectors;


        public Generator() {
            this(true);
        }

        public Generator(boolean defaultInit) {
            this.intersectors = new HashMap<>();

            if (defaultInit) {
                intersectors.put(Point.class,       (TileIntersector<Point>)     TileIntersectors::intersect);
                intersectors.put(MultiLine.class,   (TileIntersector<MultiLine>) TileIntersectors::intersect);
                intersectors.put(Street.class,      (TileIntersector<MultiLine>) TileIntersectors::intersect);
            }
        }


        public QuadTreeTiledMapSegment generate(SegmentFeatureProvider segment, QuadTreeTilingScheme scheme) {
            Rect2d bounds = scheme.getProjection().project(segment.getBounds());
            TileRect leafs = scheme.getTiles(bounds, scheme.getMaximumZoomLevel());

            Map<String, TileGroup<?>> featureset = new HashMap<>();

            for (Map.Entry<String, Feature<?>> entry : segment.getFeatures().entrySet())
                featureset.put(entry.getKey(), createTileGroup(scheme, bounds, entry.getValue()));

            return new QuadTreeTiledMapSegment(scheme, segment.getBounds(), leafs, featureset);
        }

        @SuppressWarnings("unchecked")
        private <T extends FeaturePrimitive> TileGroup<T> createTileGroup(
                QuadTreeTilingScheme scheme,
                Rect2d bounds,
                Feature<T> feature)
        {
            TileIntersector<? super T> intersector = (TileIntersector<? super T>) intersectors.get(feature.getType());
            TileId root = scheme.getTile(bounds);

            TileRect parentBounds = new TileRect(root.x, root.y, root.x, root.y, root.z);
            TileData<T> parentData = new TileData<>(1, 1);
            parentData.addAll(0, 0, feature.getData());
            int px = 1, py = 1;

            for (int z = root.z; z < scheme.getMaximumZoomLevel(); z++) {
                TileRect childBounds = scheme.getTiles(bounds, z + 1);
                int cx = childBounds.xmax - childBounds.xmin + 1;
                int cy = childBounds.ymax - childBounds.ymin + 1;
                TileData<T> childData = new TileData<>(cx, cy);

                for (int x = 0; x < px; x++) {
                    for (int y = 0; y < py; y++) {
                        TileRect c = scheme.getTiles(x + parentBounds.xmin, y + parentBounds.ymin, z, z + 1);

                        // top left
                        if (c.xmin >= childBounds.xmin && c.ymin >= childBounds.ymin) {
                            Rect2d b = scheme.getBounds(c.xmin, c.ymin, z + 1);

                            for (T t : parentData.get(x, y))
                                if (intersector.intersect(t, b, scheme.getProjection()))
                                    childData.add(c.xmin - childBounds.xmin, c.ymin - childBounds.ymin, t);
                        }

                        // top right
                        if (c.xmax <= childBounds.xmax && c.ymin >= childBounds.ymin) {
                            Rect2d b = scheme.getBounds(c.xmax, c.ymin, z + 1);

                            for (T t : parentData.get(x, y))
                                if (intersector.intersect(t, b, scheme.getProjection()))
                                    childData.add(c.xmax - childBounds.xmin, c.ymin - childBounds.ymin, t);
                        }

                        // bottom left
                        if (c.xmin >= childBounds.xmin && c.ymax <= childBounds.ymax) {
                            Rect2d b = scheme.getBounds(c.xmin, c.ymax, z + 1);

                            for (T t : parentData.get(x, y))
                                if (intersector.intersect(t, b, scheme.getProjection()))
                                    childData.add(c.xmin - childBounds.xmin, c.ymax - childBounds.ymin, t);
                        }

                        // bottom right
                        if (c.xmax <= childBounds.xmax && c.ymax <= childBounds.ymax) {
                            Rect2d b = scheme.getBounds(c.xmax, c.ymax, z + 1);

                            for (T t : parentData.get(x, y))
                                if (intersector.intersect(t, b, scheme.getProjection()))
                                    childData.add(c.xmax - childBounds.xmin, c.ymax - childBounds.ymin, t);
                        }
                    }
                }

                px = cx;
                py = cy;
                parentBounds = childBounds;
                parentData = childData;
            }

            return new TileGroup<>(feature.getName(), feature.getType(), parentData);
        }
    }


    private QuadTreeTilingScheme scheme;

    private Bounds bounds;
    private TileRect leafs;
    private Map<String, TileGroup<?>> featureset;
    private List<SegmentFeatureProvider.FeatureChangeListener> segmentListeners;
    private List<TileFeatureProvider.FeatureChangeListener> tileListeners;

    private QuadTreeTiledMapSegment(
            QuadTreeTilingScheme scheme,
            Bounds bounds,
            TileRect leafs,
            Map<String, TileGroup<?>> featureset)
    {
        this.scheme = scheme;
        this.bounds = bounds;
        this.leafs = leafs;
        this.featureset = featureset;
        this.segmentListeners = new ArrayList<>();
        this.tileListeners = new ArrayList<>();
    }


    @Override
    public QuadTreeTilingScheme getTilingScheme() {
        return scheme;
    }


    @Override
    public Bounds getBounds() {
        return scheme.getUnprojectedBounds(leafs);
    }

    @Override
    public Rect2d getProjectedBounds() {
        return scheme.getBounds(leafs);
    }


    @Override
    public Class<? extends FeaturePrimitive> getFeatureType(String name) {
        TileGroup<?> feature = featureset.get(name);
        return feature != null ? feature.type : null;
    }


    @Override
    public <T extends FeaturePrimitive> Feature<T> require(String name) {
        return getFeature(name, leafs);
    }

    @Override
    public void release(Feature<?> feature) {}


    @Override
    public Map<String, Feature<?>> getFeatures() {
        Map<String, Feature<?>> features = new HashMap<>();

        for (String str : featureset.keySet())
            features.put(str, require(str));

        return features;
    }


    @Override
    public <T extends FeaturePrimitive> TileFeature<T> require(String name, TileRect bounds) {
        Feature<T> feature = getFeature(name, scheme.getLeafTiles(bounds));
        return new TileFeature<>(feature.getName(), feature.getType(), bounds, feature.getData());
    }

    @Override
    public <T extends FeaturePrimitive> TileFeature<T> require(String name, TileId tile) {
        Feature<T> feature = getFeature(name, scheme.getLeafTiles(tile));
        return new TileFeature<>(feature.getName(), feature.getType(), new TileRect(tile), feature.getData());
    }

    @Override
    public void release(TileFeature<?> feature) {}


    @Override
    public void releaseAll() {}


    @SuppressWarnings("unchecked")
    private <T extends FeaturePrimitive> Feature<T> getFeature(String name, TileRect leafs) {
        TileGroup<T> tiles = (TileGroup<T>) featureset.get(name);
        if (tiles == null) return null;

        HashSet<T> data = new HashSet<>();

        int xl = Math.max(leafs.xmin - this.leafs.xmin, 0);
        int xr = Math.min(leafs.xmax - this.leafs.xmin, this.leafs.xmax - this.leafs.xmin);

        int yl = Math.max(leafs.ymin - this.leafs.ymin, 0);
        int yr = Math.min(leafs.ymax - this.leafs.ymin, this.leafs.ymax - this.leafs.ymin);

        for (int x = xl; x <= xr; x++)
            for (int y = yl; y <= yr; y++)
                data.addAll(tiles.data.get(x, y));

        return new Feature<>(name, tiles.type, data.toArray((T[]) Array.newInstance(tiles.type, data.size())));
    }


    @Override
    public Set<String> getAvailableFeatures() {
        return Collections.unmodifiableSet(featureset.keySet());
    }

    @Override
    public boolean hasFeature(String name) {
        return featureset.containsKey(name);
    }


    @Override
    public boolean hasTile(int x, int y, int z) {
        TileRect tiles = scheme.getTiles(bounds, z);
        return !(x < tiles.xmin || x > tiles.xmax) && !(y < tiles.ymin || y > tiles.ymax);
    }


    @Override
    public boolean addFeatureChangeListener(SegmentFeatureProvider.FeatureChangeListener listener) {
        return segmentListeners.add(listener);
    }

    @Override
    public boolean removeFeatureChangeListener(SegmentFeatureProvider.FeatureChangeListener listener) {
        return segmentListeners.remove(listener);
    }

    @Override
    public boolean hasFeatureChangeListener(SegmentFeatureProvider.FeatureChangeListener listener) {
        return segmentListeners.contains(listener);
    }

    @Override
    public boolean addFeatureChangeListener(TileFeatureProvider.FeatureChangeListener listener) {
        return tileListeners.add(listener);
    }

    @Override
    public boolean removeFeatureChangeListener(TileFeatureProvider.FeatureChangeListener listener) {
        return tileListeners.remove(listener);
    }

    @Override
    public boolean hasFeatureChangeListener(TileFeatureProvider.FeatureChangeListener listener) {
        return tileListeners.contains(listener);
    }


    protected static class TileGroup<T extends FeaturePrimitive> {
        public String name;
        public Class<T> type;
        public TileData<T> data;

        public TileGroup(String name, Class<T> type, TileData<T> data) {
            this.name = name;
            this.type = type;
            this.data = data;
        }
    }

    protected static class TileData<T extends FeaturePrimitive> {
        private List<List<List<T>>> data;

        public TileData(int nx, int ny) {
            this.data = new ArrayList<>(nx);

            for (int x = 0; x < nx; x++) {
                List<List<T>> xlist = new ArrayList<>(ny);
                this.data.add(xlist);

                for (int y = 0; y < ny; y++)
                    xlist.add(new ArrayList<>());
            }
        }

        public void add(int x, int y, T data) {
            this.data.get(x).get(y).add(data);
        }

        public void addAll(int x, int y, T[] data) {
            List<T> list = this.data.get(x).get(y);
            for (T t : data) list.add(t);
        }

        @SuppressWarnings("unchecked")
        public List<T> get(int x, int y) {
            return data.get(x).get(y);
        }
    }
}
