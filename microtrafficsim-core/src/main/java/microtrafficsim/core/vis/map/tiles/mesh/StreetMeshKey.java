package microtrafficsim.core.vis.map.tiles.mesh;

import microtrafficsim.core.map.TileFeatureProvider;
import microtrafficsim.core.map.tiles.TileRect;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.mesh.builder.LineMeshBuilder;
import microtrafficsim.math.Rect2d;
import microtrafficsim.utils.hashing.FNVHashBuilder;

import java.util.Arrays;


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
    private final long revision;
    private final double lanewidth;
    private final double outline;
    private final LineMeshBuilder.CapType cap;
    private final LineMeshBuilder.JoinType join;
    private final StreetMeshGenerator.LineType type;
    private final double[] dasharray;
    private final double miterAngleLimit;
    private final boolean useJoinsWhenPossible;
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
                         double              lanewidth,
                         double              outline,
                         LineMeshBuilder.CapType      cap,
                         LineMeshBuilder.JoinType     join,
                         StreetMeshGenerator.LineType type,
                         double[]            dasharray,
                         double              miterAngleLimit,
                         boolean             useJoinsWhenPossible,
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
        this.cap = cap;
        this.join = join;
        this.type = type;
        this.dasharray = dasharray;
        this.miterAngleLimit = miterAngleLimit;
        this.useJoinsWhenPossible = useJoinsWhenPossible;
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
                && this.cap == other.cap
                && this.join == other.join
                && this.type == other.type
                && Arrays.equals(this.dasharray, other.dasharray)
                && this.miterAngleLimit == other.miterAngleLimit
                && this.useJoinsWhenPossible == other.useJoinsWhenPossible
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
                .add(cap)
                .add(join)
                .add(type)
                .add(dasharray)
                .add(miterAngleLimit)
                .add(useJoinsWhenPossible)
                .add(drivingOnTheRight)
                .getHash();
    }
}
