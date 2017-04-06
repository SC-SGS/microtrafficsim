package microtrafficsim.core.map.tiles;

import microtrafficsim.core.map.*;
import microtrafficsim.core.map.features.MultiLine;
import microtrafficsim.core.map.features.Point;
import microtrafficsim.core.map.features.Polygon;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.math.Rect2d;
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

    private TilingScheme scheme;
    private Bounds       bounds;
    private TileRect     leafs;
    private Map<String, TileGroup<?>> featureset;
    private transient List<SegmentFeatureProvider.FeatureChangeListener> segmentListeners;
    private transient List<TileFeatureProvider.FeatureChangeListener>    tileListeners;

    /**
     * Constructs a new {@code QuadTreeTiledMapSegment}.
     *
     * @param scheme     the tiling-scheme used for this segment.
     * @param bounds     the bounds of the segment contained in this map.
     * @param leafs      the rectangle describing the provided leaf tiles.
     * @param featureset the set of features provided by this tiled map-segment.
     */
    public QuadTreeTiledMapSegment(
            TilingScheme scheme,
            Bounds bounds,
            TileRect leafs,
            Map<String, TileGroup<?>> featureset) {
        this.scheme           = scheme;
        this.bounds           = bounds;
        this.leafs            = leafs;
        this.featureset       = featureset;
        this.segmentListeners = new ArrayList<>();
        this.tileListeners    = new ArrayList<>();
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
        TileGroup<?> feature = featureset.get(name);
        return feature != null ? feature.type : null;
    }

    @Override
    public <T extends FeaturePrimitive> Feature<T> require(String name) throws InterruptedException {
        TileFeature<T> feature = getFeature(name, leafs);
        return new Feature<>(feature.getName(), feature.getType(), feature.getData());
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

    public Map<String, TileGroup<?>> getFeatureSet() {
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

    public TileRect getLeafTiles() {
        return leafs;
    }

    @SuppressWarnings("unchecked")
    private <T extends FeaturePrimitive> TileFeature<T> getFeature(String name, TileRect leafs)
            throws InterruptedException {
        TileGroup<T> tiles = (TileGroup<T>) featureset.get(name);
        if (tiles == null) return null;

        HashSet<T> data = new HashSet<>();

        int xl = Math.max(leafs.xmin - this.leafs.xmin, 0);
        int xr = Math.min(leafs.xmax - this.leafs.xmin, this.leafs.xmax - this.leafs.xmin);

        int yl = Math.max(leafs.ymin - this.leafs.ymin, 0);
        int yr = Math.min(leafs.ymax - this.leafs.ymin, this.leafs.ymax - this.leafs.ymin);

        for (int x = xl; x <= xr; x++) {
            for (int y = yl; y <= yr; y++) {
                if (Thread.interrupted()) throw new InterruptedException();
                data.addAll(tiles.data.get(x, y));
            }
        }

        return new TileFeature<>(name, tiles.type,
                new TileRect(
                        Math.max(leafs.xmin, this.leafs.xmin),
                        Math.max(leafs.ymin, this.leafs.ymin),
                        Math.min(leafs.xmax, this.leafs.xmax),
                        Math.min(leafs.ymax, this.leafs.ymax),
                        this.leafs.zoom
                ), data.toArray((T[]) Array.newInstance(tiles.type, data.size())));
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

            Map<String, TileGroup<?>> featureset = new HashMap<>();

            for (Map.Entry<String, Feature<?>> entry : segment.getFeatures().entrySet())
                featureset.put(entry.getKey(), createTileGroup(scheme, bounds, entry.getValue(), gridlevel));

            logger.debug("finished tiling process");
            return new QuadTreeTiledMapSegment(scheme, segment.getBounds(), leafs, featureset);
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
        private <T extends FeaturePrimitive> TileGroup<T>
        createTileGroup(TilingScheme scheme, Rect2d bounds, Feature<T> feature, int gridlevel) {
            TileIntersector<? super T> intersector = (TileIntersector<? super T>) intersectors.get(feature.getType());
            TileId                                                            root = scheme.getTile(bounds);

            TileRect    parentBounds = new TileRect(root.x, root.y, root.x, root.y, root.z);
            TileData<T> parentData   = new TileData<>(1, 1);
            parentData.addAll(0, 0, feature.getData());
            int px = 1, py = 1;

            for (int z = root.z; z < gridlevel; z++) {
                TileRect    childBounds = scheme.getTiles(bounds, z + 1);
                int         cx          = (childBounds.xmax - childBounds.xmin) + 1;
                int         cy          = (childBounds.ymax - childBounds.ymin) + 1;
                TileData<T> childData   = new TileData<>(cx, cy);

                for (int x = 0; x < px; x++) {
                    for (int y = 0; y < py; y++) {
                        TileRect c = scheme.getTiles(x + parentBounds.xmin, y + parentBounds.ymin, z, z + 1);

                        // top left
                        if (c.xmin >= childBounds.xmin && c.ymin >= childBounds.ymin) {
                            Rect2d b = scheme.getBounds(c.xmin, c.ymin, z + 1);

                            try {
                                for (T t : parentData.get(x, y))
                                    if (intersector.intersect(t, b, scheme.getProjection()))
                                        childData.add(c.xmin - childBounds.xmin, c.ymin - childBounds.ymin, t);
                            } catch (Throwable t) {
                                System.err.println("px: " + px + ", py: " + py + ", x: " + x + ", y: " + y);
                                t.printStackTrace();
                            }
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

                px           = cx;
                py           = cy;
                parentBounds = childBounds;
                parentData   = childData;

                logger.debug("tiling: finished level " + (z + 1) + " for feature '" + feature.getName() + "'");
            }

            return new TileGroup<>(feature.getName(), feature.getType(), parentData);
        }
    }

    /**
     * Group storing a single tiled feature.
     *
     * @param <T> the type of the feature.
     */
    public final static class TileGroup<T extends FeaturePrimitive> {
        public String      name;
        public Class<T>    type;
        public TileData<T> data;

        /**
         * Constructs a new {@code TileGroup}.
         *
         * @param name the name of the feature.
         * @param type the type of the feature.
         * @param data the tiled feature data.
         */
        public TileGroup(String name, Class<T> type, TileData<T> data) {
            this.name = name;
            this.type = type;
            this.data = data;
        }
    }

    /**
     * Feature-data.
     *
     * @param <T> the type of the feature.
     */
    public final static class TileData<T extends FeaturePrimitive> {
        public int xlen;
        public int ylen;
        public ArrayList<ArrayList<T>> data;

        /**
         * Constructs a new {@code TileData}.
         *
         * @param xlen the number of tiles in x-direction.
         * @param ylen the number of tiles in y-direction.
         */
        public TileData(int xlen, int ylen) {
            this.xlen = xlen;
            this.ylen = ylen;
            this.data = new ArrayList<>(xlen * ylen);

            for (int i = 0; i < (xlen * ylen); i++) {
                this.data.add(new ArrayList<T>());
            }
        }

        /**
         * Constructs a new {@code TileData}.
         *
         * @param xlen the number of tiles in x-direction.
         * @param ylen the number of tiles in y-direction.
         * @param data the data-array.
         */
        public TileData(int xlen, int ylen, ArrayList<ArrayList<T>> data) {
            assert xlen * ylen == data.size();

            this.xlen = xlen;
            this.ylen = ylen;
            this.data = data;
        }

        /**
         * Adds the given {@code FeaturePrimitive} to the specified tile.
         *
         * @param x    the x-id of the tile.
         * @param y    the y-id of the tile.
         * @param data the data to be added.
         */
        public void add(int x, int y, T data){
            this.data.get(y * xlen + x).add(data);
        }

        /**
         * Adds the given {@code FeaturePrimitive}s to the specified tile.
         *
         * @param x    the x-id of the tile.
         * @param y    the y-id of the tile.
         * @param data the data to be added.
         */
        public void addAll(int x, int y, T[] data) {
            ArrayList<T> list = this.data.get(y * xlen + x);
            Collections.addAll(list, data);
        }

        /**
         * Return the feature-data for the given tile.
         *
         * @param x the x-id of the tile.
         * @param y the y-id of the tile.
         * @return the data stored for the given tile.
         */
        public List<T> get(int x, int y) {
            return data.get(y * xlen + x);
        }
    }
}
