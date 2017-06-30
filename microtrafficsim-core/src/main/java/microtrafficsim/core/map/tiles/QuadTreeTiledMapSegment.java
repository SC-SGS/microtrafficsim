package microtrafficsim.core.map.tiles;

import microtrafficsim.core.map.*;
import microtrafficsim.core.map.features.MultiLine;
import microtrafficsim.core.map.features.Point;
import microtrafficsim.core.map.features.Polygon;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.math.Rect2d;
import microtrafficsim.utils.collections.Grid;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import java.lang.reflect.Array;
import java.util.*;


/**
 * Tiled map-segment using a {@code QuadTreeTilingScheme}. This segment stores only a single set of tiles for a
 * specified zoom level, other zoom-levels are generated using the existing level.
 *
 * @author Maximilian Luz
 */
public class QuadTreeTiledMapSegment implements TileFeatureProvider, SegmentFeatureProvider {

    private MapProperties properties;
    private TilingScheme scheme;
    private Bounds       bounds;
    private TileRect     leafs;
    private Map<String, FeatureGrid<?>> featureset;
    private List<SegmentFeatureProvider.FeatureChangeListener> segmentListeners;
    private List<TileFeatureProvider.FeatureChangeListener>    tileListeners;

    /**
     * Constructs a new {@code QuadTreeTiledMapSegment}.
     *
     * @param properties the map-properties for this segment.
     * @param scheme     the tiling-scheme used for this segment.
     * @param bounds     the bounds of the segment contained in this map.
     * @param leafs      the rectangle describing the provided leaf tiles.
     * @param featureset the set of features provided by this tiled map-segment.
     */
    public QuadTreeTiledMapSegment(
            MapProperties properties,
            TilingScheme scheme,
            Bounds bounds,
            TileRect leafs,
            Map<String, FeatureGrid<?>> featureset) {
        this.properties       = properties;
        this.scheme           = scheme;
        this.bounds           = bounds;
        this.leafs            = leafs;
        this.featureset       = featureset;
        this.segmentListeners = new ArrayList<>();
        this.tileListeners    = new ArrayList<>();
    }

    @Override
    public MapProperties getProperties() {
        return properties;
    }

    @Override
    public TilingScheme getTilingScheme() {
        return scheme;
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public Rect2d getProjectedBounds() {
        return scheme.getProjection().project(bounds);
    }

    @Override
    public Class<? extends FeaturePrimitive> getFeatureType(String name) {
        FeatureGrid<?> feature = featureset.get(name);
        return feature != null ? feature.getType() : null;
    }

    @Override
    public <T extends FeaturePrimitive> Feature<T> require(String name) throws InterruptedException {
        TileFeature<T> feature = getFeature(name, leafs);
        if (feature != null)
            return new Feature<>(feature.getName(), feature.getType(), feature.getData());
        else
            return null;
    }

    @Override
    public void release(Feature<?> feature) {}

    @Override
    public Map<String, Feature<?>> getFeatures() throws InterruptedException {
        Map<String, Feature<?>> features = new HashMap<>();

        for (String str : featureset.keySet())
            features.put(str, require(str));

        return features;
    }

    public Map<String, FeatureGrid<?>> getFeatureSet() {
        return featureset;
    }

    @Override
    public <T extends FeaturePrimitive> TileFeature<T> require(String name, TileRect bounds)
            throws InterruptedException {
        return getFeature(name, scheme.getTiles(bounds, leafs.zoom));
    }

    @Override
    public <T extends FeaturePrimitive> TileFeature<T> require(String name, TileId tile) throws InterruptedException {
        return getFeature(name, scheme.getTiles(tile, leafs.zoom));
    }

    @Override
    public void release(TileFeature<?> feature) {}

    @Override
    public void releaseAll() {}

    @Override
    public TileRect getFeatureBounds(String name, TileId tile) {
        return getFeatureBounds(name, new TileRect(tile.x, tile.y, tile.x, tile.y, tile.z));
    }

    @Override
    public TileRect getFeatureBounds(String name, TileRect bounds) {
        bounds = scheme.getTiles(bounds, leafs.zoom);
        return new TileRect(Math.max(bounds.xmin, this.leafs.xmin),
                            Math.max(bounds.ymin, this.leafs.ymin),
                            Math.min(bounds.xmax, this.leafs.xmax),
                            Math.min(bounds.ymax, this.leafs.ymax),
                            this.leafs.zoom);
    }

    /**
     * Returns the TileRectangle describing on which level and in which tiles the data is stored, in respect to the
     * TilingScheme used..
     *
     * @return the TileRectangle describing on which level and in which tiles the data is stored.
     */
    public TileRect getLeafTiles() {
        return leafs;
    }

    @SuppressWarnings("unchecked")
    private <T extends FeaturePrimitive> TileFeature<T> getFeature(String name, TileRect leafs)
            throws InterruptedException {
        FeatureGrid<T> tiles = (FeatureGrid<T>) featureset.get(name);
        if (tiles == null) return null;

        HashSet<T> data = new HashSet<>();

        int xl = Math.max(leafs.xmin - this.leafs.xmin, 0);
        int xr = Math.min(leafs.xmax - this.leafs.xmin, this.leafs.xmax - this.leafs.xmin);

        int yl = Math.max(leafs.ymin - this.leafs.ymin, 0);
        int yr = Math.min(leafs.ymax - this.leafs.ymin, this.leafs.ymax - this.leafs.ymin);

        for (int x = xl; x <= xr; x++) {
            for (int y = yl; y <= yr; y++) {
                if (Thread.interrupted()) throw new InterruptedException();
                data.addAll(tiles.getData().get(x, y));
            }
        }

        return new TileFeature<>(name, tiles.getType(),
                new TileRect(
                        Math.max(leafs.xmin, this.leafs.xmin),
                        Math.max(leafs.ymin, this.leafs.ymin),
                        Math.min(leafs.xmax, this.leafs.xmax),
                        Math.min(leafs.ymax, this.leafs.ymax),
                        this.leafs.zoom
                ), data.toArray((T[]) Array.newInstance(tiles.getType(), data.size())));
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

    /**
     * Generator to construct a {@code QuadTreeTiledMapSegment} from a map-segment ({@code SegmentFeatureProvider}).
     */
    public static class Generator {
        private static Logger logger = new EasyMarkableLogger(Generator.class);

        private HashMap<Class<? extends FeaturePrimitive>, TileIntersector<? extends FeaturePrimitive>> intersectors;


        /**
         * Constructs a new, default-initialized {@code Generator}. This call is equivalent to
         * {@link #Generator(boolean) Generator(true)}
         */
        public Generator() {
            this(true);
        }

        /**
         * Constructs a new {@code Generator} and default-initializes it if specified.
         *
         * @param defaultInit set to {@code true} to default-initialize this generator. Default-initialization will
         *                    automatically add {@code TileIntersector}s for various geometry (see
         *                    {@link TileIntersectors}).
         */
        public Generator(boolean defaultInit) {
            this.intersectors = new HashMap<>();

            if (defaultInit) {
                intersectors.put(Point.class, (TileIntersector<Point>) TileIntersectors::intersect);
                intersectors.put(MultiLine.class, (TileIntersector<MultiLine>) TileIntersectors::intersect);
                intersectors.put(Street.class, (TileIntersector<MultiLine>) TileIntersectors::intersect);
                intersectors.put(Polygon.class, (TileIntersector<Polygon>) TileIntersectors::intersect);
            }
        }


        /**
         * Generates a new {@code QuadTreeTiledMapSegment} from the given segment usign the given tiling scheme.
         *
         * @param segment   the segment from which the tiled map should be created.
         * @param scheme    the tiling-scheme to be used for tiling.
         * @param gridlevel the level at which the actual tile-grid should be stored.
         * @return the generated tiled map-segment.
         * @throws InterruptedException if this call has been interrupted.
         */
        public QuadTreeTiledMapSegment generate(SegmentFeatureProvider segment, TilingScheme scheme,
                                                int gridlevel) throws InterruptedException {
            logger.debug("begin tiling process");
            Rect2d bounds  = scheme.getProjection().project(segment.getBounds());
            gridlevel      = Math.max(scheme.getTile(bounds).z + 1, gridlevel);
            TileRect leafs = scheme.getTiles(bounds, gridlevel);

            Map<String, FeatureGrid<?>> featureset = new HashMap<>();

            for (Map.Entry<String, Feature<?>> entry : segment.getFeatures().entrySet())
                featureset.put(entry.getKey(), createFeatureGrid(scheme, bounds, entry.getValue(), gridlevel));

            logger.debug("finished tiling process");
            return new QuadTreeTiledMapSegment(segment.getProperties(), scheme, segment.getBounds(), leafs, featureset);
        }

        /**
         * Creates a {@code TileGroup} with the given parameters.
         *
         * @param scheme    the tiling-scheme to be used.
         * @param bounds    the bounds of the map-segment to be tiled.
         * @param feature   the feature to be tiled.
         * @param gridlevel the level at which the tile-grid should be created.
         * @param <T>       the type of the feature.
         * @return the created {@code TileGroup}.
         */
        @SuppressWarnings("unchecked")
        private <T extends FeaturePrimitive> FeatureGrid<T>
        createFeatureGrid(TilingScheme scheme, Rect2d bounds, Feature<T> feature, int gridlevel) {
            TileIntersector<? super T> intersector = (TileIntersector<? super T>) intersectors.get(feature.getType());
            TileId root = scheme.getTile(bounds);

            TileRect parentBounds = new TileRect(root.x, root.y, root.x, root.y, root.z);
            Grid<List<T>> parentGrid = new Grid<>(1, 1);
            parentGrid.set(0, 0, Arrays.asList(feature.getData()));
            int px = 1, py = 1;

            for (int z = root.z; z < gridlevel; z++) {
                TileRect childBounds = scheme.getTiles(bounds, z + 1);
                int cx = (childBounds.xmax - childBounds.xmin) + 1;
                int cy = (childBounds.ymax - childBounds.ymin) + 1;
                Grid<List<T>> childGrid = new Grid<>(cx, cy);

                for (int x = 0; x < px; x++) {
                    for (int y = 0; y < py; y++) {
                        TileRect c = scheme.getTiles(x + parentBounds.xmin, y + parentBounds.ymin, z, z + 1);

                        // top left
                        if (c.xmin >= childBounds.xmin && c.ymin >= childBounds.ymin) {
                            Rect2d b = scheme.getBounds(c.xmin, c.ymin, z + 1);

                            ArrayList<T> data = new ArrayList();
                            childGrid.set(c.xmin - childBounds.xmin, c.ymin - childBounds.ymin, data);

                            for (T t : parentGrid.get(x, y))
                                if (intersector.intersect(t, b, scheme.getProjection()))
                                    data.add(t);
                        }

                        // top right
                        if (c.xmax <= childBounds.xmax && c.ymin >= childBounds.ymin) {
                            Rect2d b = scheme.getBounds(c.xmax, c.ymin, z + 1);

                            ArrayList<T> data = new ArrayList();
                            childGrid.set(c.xmax - childBounds.xmin, c.ymin - childBounds.ymin, data);

                            for (T t : parentGrid.get(x, y))
                                if (intersector.intersect(t, b, scheme.getProjection()))
                                    data.add(t);
                        }

                        // bottom left
                        if (c.xmin >= childBounds.xmin && c.ymax <= childBounds.ymax) {
                            Rect2d b = scheme.getBounds(c.xmin, c.ymax, z + 1);

                            ArrayList<T> data = new ArrayList();
                            childGrid.set(c.xmin - childBounds.xmin, c.ymax - childBounds.ymin, data);

                            for (T t : parentGrid.get(x, y))
                                if (intersector.intersect(t, b, scheme.getProjection()))
                                    data.add(t);
                        }

                        // bottom right
                        if (c.xmax <= childBounds.xmax && c.ymax <= childBounds.ymax) {
                            Rect2d b = scheme.getBounds(c.xmax, c.ymax, z + 1);

                            ArrayList<T> data = new ArrayList();
                            childGrid.set(c.xmax - childBounds.xmin, c.ymax - childBounds.ymin, data);

                            for (T t : parentGrid.get(x, y))
                                if (intersector.intersect(t, b, scheme.getProjection()))
                                    data.add(t);
                        }
                    }
                }

                px           = cx;
                py           = cy;
                parentBounds = childBounds;
                parentGrid   = childGrid;

                logger.debug("tiling: finished level " + (z + 1) + " for feature '" + feature.getName() + "'");
            }

            return new FeatureGrid<>(feature.getName(), feature.getType(), parentGrid);
        }
    }
}
