package microtrafficsim.core.vis.map.segments.mesh;

import microtrafficsim.core.map.SegmentFeatureProvider;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.map.segments.mesh.FeatureMeshGenerator.FeatureMeshKey;
import microtrafficsim.utils.hashing.FNVHashBuilder;


public class StreetMeshKey implements FeatureMeshKey {
    private final RenderContext context;
    private final SegmentFeatureProvider provider;
    private final String feature;
    private final long   revision;
    private final Projection projection;
    private final boolean adjacency;
    private final boolean joinsWhenPossible;

    protected StreetMeshKey(RenderContext          context,
                            SegmentFeatureProvider provider,
                            String                 feature,
                            long                   revision,
                            Projection             projection,
                            boolean                adjacency,
                            boolean                joinsWhenPossible) {
        this.context           = context;
        this.provider          = provider;
        this.feature           = feature;
        this.revision          = revision;
        this.projection        = projection;
        this.adjacency         = adjacency;
        this.joinsWhenPossible = joinsWhenPossible;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StreetMeshKey)) return false;

        StreetMeshKey other = (StreetMeshKey) obj;
        return this.context == other.context
                && this.provider.equals(other.provider)
                && this.feature.equals(other.feature)
                && this.revision == other.revision
                && this.projection.equals(other.projection)
                && this.adjacency == other.adjacency
                && this.joinsWhenPossible == other.joinsWhenPossible;
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder()
                .add(context)
                .add(provider)
                .add(feature)
                .add(revision)
                .add(projection)
                .add(adjacency)
                .add(joinsWhenPossible)
                .getHash();
    }
}
