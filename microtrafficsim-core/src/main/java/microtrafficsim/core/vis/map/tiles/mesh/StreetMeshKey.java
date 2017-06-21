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
    private final float   lanewidth;
    private final float   outline;
    private final boolean drivingOnTheRight;

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
     * @param lanewidth  the width of one lane.
     * @param drivingOnTheRight {@code true} if the forward edge of a street is on the right.
     */
    public StreetMeshKey(RenderContext       context,
                         TileRect            tiles,
                         Rect2d              target,
                         TileFeatureProvider provider,
                         String              feature,
                         TilingScheme        scheme,
                         long                revision,
                         float               lanewidth,
                         float               outline,
                         boolean             drivingOnTheRight) {
        this.context    = context;
        this.tiles      = tiles;
        this.target     = target;
        this.provider   = provider;
        this.feature    = feature;
        this.scheme     = scheme;
        this.revision   = revision;
        this.lanewidth  = lanewidth;
        this.outline    = outline;
        this.drivingOnTheRight = drivingOnTheRight;
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
                && this.lanewidth == other.lanewidth
                && this.outline == other.outline
                && this.drivingOnTheRight == other.drivingOnTheRight;
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
                .add(lanewidth)
                .add(outline)
                .add(drivingOnTheRight)
                .getHash();
    }
}
