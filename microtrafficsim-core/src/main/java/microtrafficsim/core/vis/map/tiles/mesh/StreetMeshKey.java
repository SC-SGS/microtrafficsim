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
public class StreetMeshKey implements FeatureMeshGenerator.FeatureMeshKey {
    private final RenderContext context;
    private final TileRect tiles;
    private final Rect2d target;
    private final TileFeatureProvider provider;
    private final String feature;
    private final TilingScheme scheme;
    private final long         revision;
    private final boolean adjacency;
    private final boolean forceJoins;

    /**
     * Creates a new {@code StreetMeshKey}.
     *
     * @param context    the context on which the mesh is being displayed.
     * @param tiles      the tile-rectangle this mesh covers.
     * @param target     the target this mesh is projected to.
     * @param provider   the provider which provided this mesh.
     * @param feature    the feature from which this mesh was created.
     * @param scheme     the tiling-scheme used for the mesh.
     * @param revision   the revision of this mesh.
     * @param adjacency  {@code true} if the mesh is generated as adjacency-mesh.
     * @param forceJoins {@code true} if joins should be created whenever possible.
     */
    public StreetMeshKey(RenderContext       context,
                         TileRect            tiles,
                         Rect2d              target,
                         TileFeatureProvider provider,
                         String              feature,
                         TilingScheme        scheme,
                         long                revision,
                         boolean             adjacency,
                         boolean             forceJoins) {
        this.context    = context;
        this.tiles      = tiles;
        this.target     = target;
        this.provider   = provider;
        this.feature    = feature;
        this.scheme     = scheme;
        this.revision   = revision;
        this.adjacency  = adjacency;
        this.forceJoins = forceJoins;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StreetMeshKey)) return false;

        StreetMeshKey other = (StreetMeshKey) obj;
        return this.context == other.context
                && this.tiles.equals(other.tiles)
                && this.target.equals(other.target)
                && this.provider == other.provider
                && this.feature.equals(other.feature)
                && this.scheme.equals(other.scheme)
                && this.revision == other.revision
                && this.adjacency == other.adjacency
                && this.forceJoins == other.forceJoins;
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
                .add(adjacency)
                .add(forceJoins)
                .getHash();
    }
}
