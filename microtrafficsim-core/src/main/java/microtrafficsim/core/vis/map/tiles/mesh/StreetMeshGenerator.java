package microtrafficsim.core.vis.map.tiles.mesh;

import microtrafficsim.core.map.tiles.TileId;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerSource;
import microtrafficsim.core.vis.mesh.Mesh;
import microtrafficsim.core.vis.mesh.style.Style;


public class StreetMeshGenerator implements FeatureMeshGenerator {

    @Override
    public FeatureMeshKey getKey(RenderContext context, FeatureTileLayerSource source, TileId tile) {
        return new StreetMeshKey(
                context,
                tile,
                source.getFeatureProvider(),
                source.getFeatureName(),
                source.getTilingScheme(),
                source.getRevision(),
                getPropAdjacency(source.getStyle()),
                getPropJoinsWhenPossible(source.getStyle())
        );
    }

    @Override
    public Mesh generate(RenderContext context, FeatureTileLayerSource source, TileId tile) {
        // NOTE: use tile-relative positions
        return null;    // TODO
    }


    private static boolean getPropAdjacency(Style style) {
        return style.getProperty("adjacency_primitives", false);
    }

    private static boolean getPropJoinsWhenPossible(Style style) {
        return style.getProperty("use_joins_when_possible", false);
    }
}
