package microtrafficsim.core.vis.map.tiles.mesh;

import microtrafficsim.core.map.TileFeatureProvider;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.math.Rect2d;
import microtrafficsim.utils.hashing.FNVHashBuilder;


/**
 * Key for {@code StreetMesh}es.
 *
 * @author Maximilian Luz
 */
public class PolygonMeshKey implements FeatureMeshGenerator.FeatureMeshKey {
    private final RenderContext       context;
    private final TileRect            tiles;
    private final Rect2d              target;
    private final TileFeatureProvider provider;
    private final String              feature;
    private final TilingScheme        scheme;
    private final long                revision;

    /**
     * Creates a new {@code PolygonMeshKey}.
     *
     * @param context    the context on which the mesh is being displayed.
     * @param tiles      the tile-rectangle this mesh covers.
     * @param target     the target this mesh is projected to.
     * @param provider   the provider which provided this mesh.
     * @param feature    the feature from which this mesh was created.
     * @param scheme     the tiling-scheme used for the mesh.
     * @param revision   the revision of this mesh.
     */
    public PolygonMeshKey(RenderContext       context,
                          TileRect            tiles,
                          Rect2d              target,
                          TileFeatureProvider provider,
                          String              feature,
                          TilingScheme        scheme,
                          long                revision) {
        this.context    = context;
        this.tiles      = tiles;
        this.target     = target;
        this.provider   = provider;
        this.feature    = feature;
        this.scheme     = scheme;
        this.revision   = revision;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PolygonMeshKey)) return false;

        PolygonMeshKey other = (PolygonMeshKey) obj;
        return this.context == other.context
                && this.tiles.equals(other.tiles)
                && this.target.equals(other.target)
                && this.provider == other.provider
                && this.feature.equals(other.feature)
                && this.scheme.equals(other.scheme)
                && this.revision == other.revision;
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder()
                .add(context)
                .add(tiles)
                .add(provider)
                .add(feature)
                .add(scheme)
                .add(revision)
                .getHash();
    }
}
